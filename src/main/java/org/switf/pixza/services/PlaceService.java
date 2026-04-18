package org.switf.pixza.services;

import org.switf.pixza.request.PlaceRequest;
import org.switf.pixza.response.PlaceResponse;

import java.util.List;

public interface PlaceService {
    PlaceResponse createPlace(PlaceRequest placeRequest);
    
    // Método para listar lugares
    List<PlaceResponse> getAllPlaces();

    List<PlaceResponse> getPlacesByCategory(Long categoryId);

    // Método para actualizar un lugar existente
    PlaceResponse updatePlaceByName(String placeName, PlaceRequest newPlaceModel);

    // Método para eliminar un lugar
    void deleteCategoryByName(String placeName);

    PlaceResponse savePlaceUrlImage(String placeName, PlaceRequest urlImage);

    //Método para buscar un lugar
    List<PlaceResponse> searchPlaces(String searchTerm);
}
