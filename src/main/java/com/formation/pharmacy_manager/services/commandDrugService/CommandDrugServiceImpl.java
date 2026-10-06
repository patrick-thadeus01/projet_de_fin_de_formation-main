package com.formation.pharmacy_manager.services.commandDrugService;

import com.formation.pharmacy_manager.dto.commandeDrugDto.CommandeDrugRequestDto;
import com.formation.pharmacy_manager.dto.commandeDrugDto.CommandeDrugResponseDto;
import com.formation.pharmacy_manager.entities.Command;
import com.formation.pharmacy_manager.entities.CommandDrug;
import com.formation.pharmacy_manager.entities.DistributorDrug;
import com.formation.pharmacy_manager.entities.Drug;
import com.formation.pharmacy_manager.repository.CommandRepository;
import com.formation.pharmacy_manager.repository.CommandeDrugRepository;
import com.formation.pharmacy_manager.repository.DistributorDrugRepository;
import com.formation.pharmacy_manager.repository.DrugRepository;

import lombok.AllArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;

@Service
@AllArgsConstructor
@Transactional
public class CommandDrugServiceImpl implements CommandDrugService {

    private CommandeDrugRepository commandeDrugRepository;
    private DrugRepository drugRepository;
    private CommandRepository commandRepository;
    private DistributorDrugRepository distributorDrugRepository;

    @Override
    @Transactional
    public CommandeDrugResponseDto create(CommandeDrugRequestDto dto) {

        // CORRECTION 1 : Vérifier que la quantité est strictement positive
        if (dto.getQuantity() <= 0) {
            throw new RuntimeException("La quantité commandée doit être strictement positive");
        }

        Drug drug = drugRepository.findDistinctByDrugName(dto.getDrugName());
        if (drug == null) {
            throw new RuntimeException("Médicament introuvable : " + dto.getDrugName());
        }

        Command command = commandRepository.findDistinctByPseudo(dto.getPseudo());
        if (command == null) {
            throw new RuntimeException("Commande introuvable : " + dto.getPseudo());
        }

        // CORRECTION 2 : Récupérer le stock avec un VERROU PESSIMISTE
        // ⚠️ On utilise le userName du distributor, mais on doit d'abord le trouver.
        // Ici on suppose que le premier distributeur disponible dans la liste du drug
        // est celui qui fournit. Cette logique est à améliorer si plusieurs distributeurs.
        DistributorDrug dis = drug.getDistributorDrugList().stream()
                .filter(fil -> fil.getQte() >= dto.getQuantity())   // CORRECTION 3 : >= au lieu de >
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        "Stock insuffisant pour le médicament : " + dto.getDrugName()));

        // CORRECTION 4 : Re-charger avec verrou pour éviter les accès concurrents
        final String disUserName = dis.getDistributor().getUserName();
        DistributorDrug lockedDis = distributorDrugRepository
                .findByUserNameAndDrugNameForUpdate(disUserName, drug.getDrugName())
                .orElseThrow(() -> new RuntimeException("Stock introuvable (verrou)"));

        // Re-vérifier le stock APRÈS le verrou (car il a pu changer entre-temps)
        if (lockedDis.getQte() < dto.getQuantity()) {
            throw new RuntimeException("Stock insuffisant après verrouillage : "
                    + lockedDis.getQte() + " disponible, " + dto.getQuantity() + " demandé");
        }

        // Décrémenter le stock
        lockedDis.setQte(lockedDis.getQte() - dto.getQuantity());
        lockedDis.setUpdate_date(new Date());
        distributorDrugRepository.save(lockedDis);

        // Créer la ligne de commande
        CommandDrug cmdDrug = new CommandDrug();
        cmdDrug.setDrug(drug);
        cmdDrug.setCommand(command);
        cmdDrug.setTime(LocalTime.now());
        cmdDrug.setDate(LocalDate.now());
        cmdDrug.setQuantity(dto.getQuantity());
        cmdDrug.setUserDis(disUserName);
        CommandDrug cmd = commandeDrugRepository.save(cmdDrug);

        return new CommandeDrugResponseDto(
                cmd.getCommandDrugId(),
                cmd.getCommand().getPseudo(),
                cmd.getDrug().getDrugName(),
                cmd.getQuantity(),
                cmd.getDrug().getPrice(),
                cmd.getDate(),
                cmd.getTime(),
                cmd.getUserDis()
        );
    }

    @Override
    public List<CommandeDrugResponseDto> getAllCommandDrug() {
        return commandeDrugRepository.findAll().stream().map(
                cmd -> new CommandeDrugResponseDto(
                        cmd.getCommandDrugId(),
                        cmd.getCommand().getPseudo(),
                        cmd.getDrug().getDrugName(),
                        cmd.getQuantity(),
                        cmd.getDrug().getPrice(),
                        cmd.getDate(),
                        cmd.getTime(),
                        cmd.getUserDis()
                )).toList();
    }

    @Override
    public CommandeDrugResponseDto getById(long id) {
        return commandeDrugRepository.findById(id).map(
                cmd -> new CommandeDrugResponseDto(
                        cmd.getCommandDrugId(),
                        cmd.getCommand().getPseudo(),
                        cmd.getDrug().getDrugName(),
                        cmd.getQuantity(),
                        cmd.getDrug().getPrice(),
                        cmd.getDate(),
                        cmd.getTime(),
                        cmd.getUserDis()
                )).orElse(null);
    }

    @Override
    @Transactional
    public String deleteById(long id) {
        CommandDrug cmd = commandeDrugRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ligne de commande introuvable"));

        // CORRECTION 5 : Vérifier que le DistributorDrug existe
        DistributorDrug dis = distributorDrugRepository
                .getByUserNameAndDrugName(cmd.getDrug().getDrugName(), cmd.getUserDis());

        if (dis == null) {
            throw new RuntimeException("Impossible de restituer le stock : distributeur introuvable");
        }

        dis.setQte(dis.getQte() + cmd.getQuantity());
        dis.setUpdate_date(new Date());
        distributorDrugRepository.save(dis);

        commandeDrugRepository.deleteById(id);

        return "Ligne de commande supprimée avec succès";
    }

    @Override
    public boolean existById(long id) {
        // CORRECTION 6 : Utiliser le bon repository
        return commandeDrugRepository.existsById(id);
    }

    @Override
    @Transactional
    public CommandeDrugResponseDto update(long id, CommandeDrugRequestDto dto) {

        // CORRECTION 7 : Vérifier la nouvelle quantité
        if (dto.getQuantity() <= 0) {
            throw new RuntimeException("La quantité doit être strictement positive");
        }

        CommandDrug cmd = commandeDrugRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ligne de commande introuvable"));

        DistributorDrug dis = distributorDrugRepository
                .getByUserNameAndDrugName(cmd.getDrug().getDrugName(), cmd.getUserDis());

        if (dis == null) {
            throw new RuntimeException("Distributeur introuvable pour cette ligne de commande");
        }

        int ancienneQte = cmd.getQuantity();
        int nouvelleQte = dto.getQuantity();
        int difference = nouvelleQte - ancienneQte;

        // CORRECTION 8 : Si on augmente la quantité, vérifier le stock disponible
        if (difference > 0 && dis.getQte() < difference) {
            throw new RuntimeException("Stock insuffisant pour augmenter la quantité : "
                    + dis.getQte() + " disponible, " + difference + " demandé en plus");
        }

        // Ajuster le stock (on enlève si on augmente, on rajoute si on diminue)
        dis.setQte(dis.getQte() - difference);
        dis.setUpdate_date(new Date());
        distributorDrugRepository.save(dis);

        // Mettre à jour la ligne de commande
        cmd.setQuantity(nouvelleQte);
        cmd.setTime(LocalTime.now());
        CommandDrug cmde = commandeDrugRepository.save(cmd);

        return new CommandeDrugResponseDto(
                cmde.getCommandDrugId(),
                cmde.getCommand().getPseudo(),
                cmde.getDrug().getDrugName(),
                cmde.getQuantity(),
                cmde.getDrug().getPrice(),
                cmde.getDate(),
                cmde.getTime(),
                cmde.getUserDis()
        );
    }
}