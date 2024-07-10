package org.switf.pixza.services;

import org.switf.pixza.request.CategoryRequest;
import org.switf.pixza.response.CategoryResponse;

import java.util.List;

public interface CategoryService {

    CategoryResponse createCategory(CategoryRequest categoryRequest);

    List<CategoryResponse> getAllCategories();

    CategoryResponse updateCategoryById(Long idCategory, CategoryRequest categoryRequest);

    void deleteCategoryById(Long idCategory);
}
