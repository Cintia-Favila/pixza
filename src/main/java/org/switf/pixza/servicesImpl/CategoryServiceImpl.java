package org.switf.pixza.servicesImpl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryJpaRepository categoryJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @PersistenceContext
    private EntityManager entityManager;

    // Método para obtener el usuario autenticado
    private UserModel getAuthenticatedUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userJpaRepository.findByUsername(username)
                .orElseThrow(() -> new AuthenticationException("Please Login"));
    }

    // Método para crear una nueva categoria
    @Override
    public CategoryResponse createCategory(CategoryRequest categoryRequest) {
        UserModel user = getAuthenticatedUser();

        // Verificar si la categoría ya existe
        CategoryModel existingCategory = categoryJpaRepository.findByCategory(categoryRequest.getCategory());
        if (existingCategory != null) {
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

    // Método para listar categorias
    @Override
    public List<CategoryResponse> getAllCategories() {
        return categoryJpaRepository.findAllByOrderByIdCategoryAsc().stream()
                .map(category -> new CategoryResponse(
                        category.getIdCategory(),
                        category.getCategory(),
                        category.getUser().getUsername()))
                .collect(Collectors.toList());
    }

    // Método para actualizar una categoria
    @Override
    public CategoryResponse updateCategoryById(Long idCategory, CategoryRequest categoryRequest) {
        UserModel user = getAuthenticatedUser();

        // Buscar la categoría por su ID
        CategoryModel categoryModel = categoryJpaRepository.findById(idCategory)
                .orElseThrow(() -> new CategoryNotFoundException("ID " + idCategory + " no encontrado"));
        try {
            // Actualizar la categoría con la nueva información
            categoryModel.setCategory(categoryRequest.getCategory());
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

    // Método para eliminar una categoria
    @Override
    public void deleteCategoryById(Long idCategory) {
        CategoryModel categoryModel = categoryJpaRepository.findById(idCategory)
                .orElseThrow(() -> new CategoryNotFoundException("Categoria no encontrada"));
        categoryJpaRepository.delete(categoryModel);
}
}

