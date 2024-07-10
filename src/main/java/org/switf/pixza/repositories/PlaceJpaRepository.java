package org.switf.pixza.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.switf.pixza.models.PlaceModel;

import java.util.List;

@Repository
public interface PlaceJpaRepository extends JpaRepository<PlaceModel, Long> {

    List<PlaceModel> findByCategory_IdCategory(Long idCategory);
}
