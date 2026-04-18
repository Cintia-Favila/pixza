package org.switf.pixza.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.switf.pixza.models.PlaceModel;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaceJpaRepository extends JpaRepository<PlaceModel, Long> {

    List<PlaceModel> findByCategory_IdCategory(Long idCategory);

    List<PlaceModel> findByNameContainingOrDescriptionContainingOrAddressContainingOrCategory_CategoryContaining(String name, String description, String address, String category);

    Optional<Object> findByName(String placeName);
}
