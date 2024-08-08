package org.switf.pixza.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Entity
@Table(name = "places")
@Transactional
public class PlaceModel {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "place_seq")
    @SequenceGenerator(name = "place_seq", sequenceName = "place_sequence", allocationSize = 1)
    private Long idPlace;
    private String name;
    private String description;
    private String address;
//    private double latitude;
//    private double longitude;
    private String imageUrl;

    @ManyToOne(targetEntity = CategoryModel.class)
    private CategoryModel category;

    @ManyToOne(targetEntity = UserModel.class)
    private UserModel user;
}
