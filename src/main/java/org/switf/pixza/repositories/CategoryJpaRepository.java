package org.switf.pixza.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.switf.pixza.models.CategoryModel;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryJpaRepository extends JpaRepository <CategoryModel, Long> {
    List<CategoryModel> findAllByOrderByIdCategoryAsc();

    Optional<CategoryModel> findByCategory(String categoryName);
}
