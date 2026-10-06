package com.formation.pharmacy_manager.services.drugService;

import com.formation.pharmacy_manager.dto.drugDto.DrugRequestDto;
import com.formation.pharmacy_manager.dto.drugDto.DrugResponseDto;
import com.formation.pharmacy_manager.entities.Drug;
import com.formation.pharmacy_manager.repository.CategoryRepository;
import com.formation.pharmacy_manager.repository.CommandeDrugRepository;
import com.formation.pharmacy_manager.repository.DistributorDrugRepository;
import com.formation.pharmacy_manager.repository.DrugRepository;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@AllArgsConstructor
@Transactional
public class DrugServiceImpl implements DrugService {

    private final DrugRepository drugRepository;
    private final CategoryRepository categoryRepository;
    private final CommandeDrugRepository commandeDrugRepository;
    private final DistributorDrugRepository distributorDrugRepository;

    @Override
    public DrugResponseDto createDrug(DrugRequestDto dto) {
        Drug drug = new Drug();
        drug.setDrugName(dto.getDrugName());
        drug.setDrugDescription(dto.getDrugDescription());
        drug.setPeremption(dto.getPeremption());
        drug.setPrice(dto.getPrice());
        drug.setCreation_date(new Date());
        drug.setUpdate_date(new Date());
        drug.setCategory(categoryRepository.findDistinctByCategoryType(dto.getType()));

        Drug dg = drugRepository.save(drug);
        return toDto(dg);
    }

    @Override
    public List<DrugResponseDto> getAllDrug() {
        return drugRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional
    public String deleteById(long id) {
        Drug drug = drugRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Médicament introuvable : " + id));

        //Refuser si le médicament est utilisé dans l'historique de commandes
        if (commandeDrugRepository.existsByDrug_DrugId(id)) {
            throw new RuntimeException(
                    "Suppression impossible : le médicament '" + drug.getDrugName()
                    + "' est utilisé dans des commandes existantes. L'historique commercial doit être préservé.");
        }

        // Refuser si le médicament est utilisé dans le stock d'un distributeur
        if (distributorDrugRepository.existsByDrug_DrugId(id)) {
            throw new RuntimeException(
                    "Suppression impossible : le médicament '" + drug.getDrugName()
                    + "' est référencé dans le stock d'un ou plusieurs distributeurs. "
                    + "Retirez d'abord les stocks associés.");
        }

        drugRepository.deleteById(id);
        return "Médicament supprimé avec succès";
    }

    @Override
    public boolean existById(long id) {
        return drugRepository.existsById(id);
    }

    @Override
    public DrugResponseDto updateDrug(long id, DrugRequestDto dto) {
        Drug drug = drugRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Médicament introuvable : " + id));

        drug.setDrugName(dto.getDrugName());
        drug.setDrugDescription(dto.getDrugDescription());
        drug.setPeremption(dto.getPeremption());
        drug.setPrice(dto.getPrice());
        drug.setUpdate_date(new Date());
        drug.setCategory(categoryRepository.findDistinctByCategoryType(dto.getType()));

        Drug dg = drugRepository.save(drug);
        return toDto(dg);
    }

    @Override
    public List<DrugResponseDto> searchByKeyWorld(String key) {
        return drugRepository.searchByKeyWorld(key).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public DrugResponseDto getById(long id) {
        Drug dg = drugRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Médicament introuvable : " + id));
        return toDto(dg);
    }

    // Méthode utilitaire

    private DrugResponseDto toDto(Drug dg) {
        return new DrugResponseDto(
                dg.getDrugId(),
                dg.getDrugName(),
                dg.getDrugDescription(),
                dg.getPeremption(),
                dg.getPrice(),
                dg.getCategory() != null ? dg.getCategory().getCategoryType() : null,
                dg.getCreation_date(),
                dg.getUpdate_date()
        );
    }
}