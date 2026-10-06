package com.formation.pharmacy_manager.services.serviceCategory;

import com.formation.pharmacy_manager.dto.categoryDto.CategoryPriceDto;
import com.formation.pharmacy_manager.dto.categoryDto.CategoryRequestDto;
import com.formation.pharmacy_manager.dto.categoryDto.CategoryResponseDto;
import com.formation.pharmacy_manager.entities.Category;
import com.formation.pharmacy_manager.repository.CategoryRepository;
import com.formation.pharmacy_manager.repository.DrugRepository;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@AllArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final DrugRepository drugRepository;

    @Override
    public CategoryResponseDto createCategory(CategoryRequestDto dto) {
        Category category = new Category();
        category.setCategoryType(dto.getCategoryType());
        category.setCategoryName(dto.getCategoryName());
        category.setCreation_date(LocalDate.now());
        category.setUpdate_date(LocalDate.now());

        Category cat = categoryRepository.save(category);
        return new CategoryResponseDto(
                cat.getCategoryId(),
                cat.getCategoryType(),
                cat.getCategoryName()
        );
    }

    @Override
    public List<CategoryResponseDto> getAllCategory() {
        return categoryRepository.findAll().stream().map(
                category -> new CategoryResponseDto(
                        category.getCategoryId(),
                        category.getCategoryType(),
                        category.getCategoryName()
                )).toList();
    }

    @Override
    public CategoryResponseDto getById(long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Catégorie introuvable : " + id));
        return new CategoryResponseDto(
                category.getCategoryId(),
                category.getCategoryType(),
                category.getCategoryName()
        );
    }

    @Override
    @Transactional
    public String deleteById(long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Catégorie introuvable : " + id));

        // Refuser la suppression si la catégorie contient des médicaments
        if (drugRepository.existsByCategory_CategoryId(id)) {
            throw new RuntimeException(
                    "Suppression impossible : la catégorie '" + category.getCategoryName()
                    + "' contient encore des médicaments. Supprimez d'abord les médicaments associés.");
        }

        categoryRepository.deleteById(id);
        return "Catégorie supprimée avec succès";
    }

    @Override
    public CategoryResponseDto updateCategory(long id, CategoryRequestDto dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Catégorie introuvable : " + id));

        category.setCategoryName(dto.getCategoryName());
        category.setCategoryType(dto.getCategoryType());
        category.setUpdate_date(LocalDate.now());

        Category cat = categoryRepository.save(category);
        return new CategoryResponseDto(
                cat.getCategoryId(),
                cat.getCategoryType(),
                cat.getCategoryName()
        );
    }

    @Override
    public List<CategoryPriceDto> getCategoryBySumPrice() {
        return categoryRepository.getCategoryBySumPrice();
    }
}