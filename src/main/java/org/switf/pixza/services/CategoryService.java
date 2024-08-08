package org.switf.pixza.services;

import org.switf.pixza.request.CategoryRequest;
import org.switf.pixza.response.CategoryResponse;

import java.util.List;

public interface CategoryService {

    CategoryResponse createCategory(CategoryRequest categoryRequest);

    List<CategoryResponse> getAllCategories();

    // Método para actualizar una categoría
    CategoryResponse updateCategoryByName(String currentCategoryName, String newCategoryName);

    // Método para eliminar una categoría
    void deleteCategoryByName(String categoryName);

    Long getCategoryIdByName(String categoryName);
}
