package com.example.DATN.services;

import com.example.DATN.dtos.request.CategoryRequest;
import com.example.DATN.dtos.respone.CategoryResponse;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.mapper.CategoryMapper;
import com.example.DATN.models.Category;
import com.example.DATN.repositories.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryResponse createCategory(CategoryRequest request) {
        Category category = categoryMapper.toCategory(request);
        category = categoryRepository.save(category);
        return categoryMapper.toCategoryResponse(category);
    }

    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toCategoryResponse)
                .collect(Collectors.toList());
    }

    public List<CategoryResponse> searchCategoriesByName(String name) {
        return categoryRepository.findByNameContainingIgnoreCase(name).stream()
                .map(categoryMapper::toCategoryResponse)
                .collect(Collectors.toList());
    }

    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.CATEGORY_NOT_FOUND));
        return categoryMapper.toCategoryResponse(category);
    }

    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.CATEGORY_NOT_FOUND));
        categoryMapper.updateCategory(category, request);
        category = categoryRepository.save(category);
        return categoryMapper.toCategoryResponse(category);
    }

    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.CATEGORY_NOT_FOUND));
        categoryRepository.delete(category);
    }
}