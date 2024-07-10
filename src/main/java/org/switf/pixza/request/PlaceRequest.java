package org.switf.pixza.request;

import lombok.Data;

@Data
public class PlaceRequest {
    private String name;
    private String description;
    private String address;
    private Long idCategory;
}
