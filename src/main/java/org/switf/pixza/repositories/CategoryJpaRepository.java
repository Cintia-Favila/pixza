package org.switf.pixza.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.switf.pixza.models.CategoryModel;

import java.util.List;

@Repository
public interface CategoryJpaRepository extends JpaRepository <CategoryModel, Long> {
    List<CategoryModel> findAllByOrderByIdCategoryAsc();

    CategoryModel findByCategory(String category);
}
