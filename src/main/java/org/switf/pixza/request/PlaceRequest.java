package org.switf.pixza.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NonNull;

@Data
public class PlaceRequest {
    private String name;
    private String description;
    private String address;
    @NotBlank(message = "El nombre de la categoría no puede estar en blanco")
    private String categoryName;
    private String imageUrl;
}
