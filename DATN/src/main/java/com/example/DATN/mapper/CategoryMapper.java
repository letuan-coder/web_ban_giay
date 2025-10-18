
package com.example.DATN.mapper;

import com.example.DATN.dtos.request.CategoryRequest;
import com.example.DATN.dtos.respone.CategoryResponse;
import com.example.DATN.models.Category;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryResponse toCategoryResponse(Category category);

    Category toCategory(CategoryRequest request);

    void updateCategory(@MappingTarget Category category, CategoryRequest request);
}
