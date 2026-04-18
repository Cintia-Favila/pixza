package org.switf.pixza.servicesImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.switf.pixza.exceptions.AuthenticationException;
import org.switf.pixza.exceptions.CategoryNotFoundException;
import org.switf.pixza.exceptions.DuplicateCategoryException;
import org.switf.pixza.models.CategoryModel;
import org.switf.pixza.models.UserModel;
import org.switf.pixza.repositories.CategoryJpaRepository;
import org.switf.pixza.repositories.UserJpaRepository;
import org.switf.pixza.request.CategoryRequest;
import org.switf.pixza.response.CategoryResponse;
import org.switf.pixza.services.CategoryService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryJpaRepository categoryJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    // Método para obtener el usuario autenticado
    private UserModel getAuthenticatedUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userJpaRepository.findByUsername(username)
                .orElseThrow(() -> new AuthenticationException("Please Login"));
    }

    // Método para crear una nueva categoría
    @Override
    public CategoryResponse createCategory(CategoryRequest categoryRequest) {
        UserModel user = getAuthenticatedUser();

        // Verificar si la categoría ya existe
        Optional<CategoryModel> existingCategory = categoryJpaRepository.findByCategory(categoryRequest.getCategory());
        if (existingCategory.isPresent()) {
            throw new DuplicateCategoryException("La categoría ya existe.");
        }

        CategoryModel categoryModel = new CategoryModel();
        categoryModel.setCategory(categoryRequest.getCategory());
        categoryModel.setUser(user);

        CategoryModel savedCategory = categoryJpaRepository.save(categoryModel);

        return new CategoryResponse(
                savedCategory.getIdCategory(),
                savedCategory.getCategory()
        );
    }

    // Método para listar categorías
    @Override
    public List<CategoryResponse> getAllCategories() {
        return categoryJpaRepository.findAllByOrderByIdCategoryAsc().stream()
                .map(category -> new CategoryResponse(
                        category.getIdCategory(),
                        category.getCategory(),
                        category.getUser().getUsername()))
                .collect(Collectors.toList());
    }

    // Método para actualizar una categoría
    @Override
    public CategoryResponse updateCategoryByName(String category, String newCategoryName) {
        UserModel user = getAuthenticatedUser();

        // Buscar la categoría por su nombre actual
        CategoryModel categoryModel = categoryJpaRepository.findByCategory(category)
                .orElseThrow(() -> new CategoryNotFoundException("Categoría " + category + " no encontrada"));

        // Verificar si el nuevo nombre es diferente del actual
        if (categoryModel.getCategory().equals(newCategoryName)) {
            throw new IllegalArgumentException("El nuevo nombre de la categoría es el mismo que el actual");
        }

        try {
            // Actualizar la categoría con la nueva información
            categoryModel.setCategory(newCategoryName);
            categoryModel.setUser(user);

            CategoryModel updatedCategory = categoryJpaRepository.save(categoryModel);

            return new CategoryResponse(
                    updatedCategory.getIdCategory(),
                    updatedCategory.getCategory()
            );
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateCategoryException("La categoría ya existe");
        }
    }

    // Método para eliminar una categoría
    @Override
    public void deleteCategoryByName(String categoryName) {
        // Buscar la categoría por nombre
        CategoryModel categoryModel = categoryJpaRepository.findByCategory(categoryName)
                .orElseThrow(() -> new CategoryNotFoundException("Categoría " + categoryName + " no encontrada"));
        // Eliminar la categoría
        categoryJpaRepository.delete(categoryModel);
    }

    //Método para encontrar el ID de una categoría
    @Override
    public Long getCategoryIdByName(String categoryName) {
            Optional<CategoryModel> categoryModel = categoryJpaRepository.findByCategory(categoryName);
            return categoryModel.get().getIdCategory();
    }
}

