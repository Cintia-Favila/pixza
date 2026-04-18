package org.switf.pixza.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlaceResponse {
    private Long idPlace;
    private String name;
    private String description;
    private String address;
    private String imageUrl;
    private String categoryName;
    private String user;

    public PlaceResponse(Long idPlace, String name, String description, String address, String imageUrl, String category) {
        this.idPlace = idPlace;
        this.name = name;
        this.description = description;
        this.address = address;
        this.imageUrl = imageUrl;
        this.categoryName = category;
    }
}
