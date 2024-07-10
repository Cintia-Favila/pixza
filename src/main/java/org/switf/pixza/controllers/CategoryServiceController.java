package org.switf.pixza.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.switf.pixza.exceptions.CategoryNotFoundException;
import org.switf.pixza.request.CategoryRequest;
import org.switf.pixza.response.CategoryResponse;
import org.switf.pixza.services.CategoryService;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryServiceController {

    @Autowired
    private CategoryService categoryService;

    @PostMapping("/newCategory")
    public ResponseEntity<?> createCategory(@RequestBody CategoryRequest categoryRequest) {
        try {
            CategoryResponse createdCategory = categoryService.createCategory(categoryRequest);
            return ResponseEntity.ok("Categoria creada: " + createdCategory.getCategory());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/allCategories")
    public ResponseEntity<?> getAllCategories() {
        try {
            List<CategoryResponse> categories = categoryService.getAllCategories();
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al cargar categorías: " + e.getMessage());
        }
    }

    @PutMapping("/edit/{idCategory}")
    public ResponseEntity<?> updateCategory(@PathVariable Long idCategory, @RequestBody CategoryRequest categoryRequest) {
        try {
            CategoryResponse updatedCategory = categoryService.updateCategoryById(idCategory, categoryRequest);
            return ResponseEntity.ok("ID editado: " + updatedCategory.getIdCategory() + ", Nueva categoria: " + updatedCategory.getCategory());
        } catch (CategoryNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/deleteCategory/{idCategory}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long idCategory) {
        try {
            categoryService.deleteCategoryById(idCategory);
            return ResponseEntity.ok("ID " + idCategory + " eliminado exitosamente");
        } catch (CategoryNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al eliminar la categoría: " + e.getMessage());
        }
    }
}
