package org.switf.pixza.services;

import org.switf.pixza.request.PlaceRequest;
import org.switf.pixza.response.PlaceResponse;

import java.util.List;

public interface PlaceService {
    PlaceResponse createPlace(PlaceRequest placeRequest);


    // Método para listar lugares
    List<PlaceResponse> getAllPlaces();

    List<PlaceResponse> getPlacesByCategory(Long categoryId);

    PlaceResponse updatePlaceById(Long idPlace, PlaceRequest placeRequest);

    void deleteCategoryById(Long idPlace);
}
