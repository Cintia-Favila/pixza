package org.switf.pixza.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Entity
@Table(name = "categories", uniqueConstraints = {@UniqueConstraint(columnNames = "category")})
public class CategoryModel {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "category_seq")
    @SequenceGenerator(name = "category_seq", sequenceName = "category_sequence", allocationSize = 1)
    @Column(name = "id_Category")
    private Long idCategory;
    private String category;

    @OneToMany(targetEntity = PlaceModel.class, fetch = FetchType.EAGER, mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<PlaceModel> placeModels;


    @ManyToOne(targetEntity = UserModel.class)
    private UserModel user;
}