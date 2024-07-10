package org.switf.pixza.servicesImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.switf.pixza.config.GeocodingService;
import org.switf.pixza.exceptions.AuthenticationException;
import org.switf.pixza.exceptions.CategoryNotFoundException;
import org.switf.pixza.exceptions.CoordinateException;
import org.switf.pixza.exceptions.PlaceNotFoundException;
import org.switf.pixza.models.CategoryModel;
import org.switf.pixza.models.PlaceModel;
import org.switf.pixza.models.UserModel;
import org.switf.pixza.repositories.CategoryJpaRepository;
import org.switf.pixza.repositories.PlaceJpaRepository;
import org.switf.pixza.repositories.UserJpaRepository;
import org.switf.pixza.request.PlaceRequest;
import org.switf.pixza.response.PlaceResponse;
import org.switf.pixza.services.PlaceService;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class PlaceServiceImpl implements PlaceService {

    @Autowired
    private PlaceJpaRepository placeJpaRepository;

    @Autowired
    private CategoryJpaRepository categoryJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private GeocodingService geocodingService;

    // Método para obtener coordenadas desde una dirección
    private double[] getCoordinates(String address) {
        String latLong = geocodingService.getLatLongFromAddress(address);
        if (latLong.startsWith("Error")) {
            throw new CoordinateException("Error al obtener coordenadas: " + latLong);
        }
        String[] parts = latLong.split(",");
        return new double[] { Double.parseDouble(parts[0]), Double.parseDouble(parts[1]) };
    }

    // Método para obtener una categoría por su ID
    private CategoryModel getCategory(Long categoryId) {
        return categoryJpaRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("Categoría no encontrada."));
    }

    // Método para obtener el usuario autenticado
    private UserModel getAuthenticatedUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userJpaRepository.findByUsername(username)
                .orElseThrow(() -> new AuthenticationException("Please Login"));
    }

    // Método para crear un nuevo lugar
    @Override
    public PlaceResponse createPlace(PlaceRequest placeRequest) {
        double[] coordinates = getCoordinates(placeRequest.getAddress());
        CategoryModel category = getCategory(placeRequest.getIdCategory());
        UserModel user = getAuthenticatedUser();

        PlaceModel placeModel = new PlaceModel();
        placeModel.setName(placeRequest.getName());
        placeModel.setDescription(placeRequest.getDescription());
        placeModel.setAddress(placeRequest.getAddress());
        placeModel.setLatitude(coordinates[0]);
        placeModel.setLength(coordinates[1]);
        placeModel.setCategory(category);
        placeModel.setUser(user);

        PlaceModel savedPlace = placeJpaRepository.save(placeModel);

        return new PlaceResponse(
                savedPlace.getIdPlace(),
                savedPlace.getName(),
                savedPlace.getDescription(),
                savedPlace.getAddress(),
                savedPlace.getCategory().getIdCategory().toString()
        );
    }

    // Método para listar lugares
    @Override
    public List<PlaceResponse> getAllPlaces() {
        return placeJpaRepository.findAll().stream()
                .map(place -> new PlaceResponse(
                        place.getIdPlace(),
                        place.getName(),
                        place.getDescription(),
                        place.getAddress(),
                        place.getCategory().getIdCategory().toString(),
                        place.getUser().getUsername()
                ))
                .collect(Collectors.toList());
    }

    //Método para listar los lugares de una categoria
    @Override
    public List<PlaceResponse> getPlacesByCategory(Long idCategory) {
        List<PlaceModel> places = placeJpaRepository.findByCategory_IdCategory(idCategory);
        return places.stream()
                .map(place -> new PlaceResponse(
                        place.getIdPlace(),
                        place.getName(),
                        place.getDescription(),
                        place.getAddress(),
                        place.getCategory().getIdCategory().toString(),
                        place.getUser().getUsername()))
                .collect(Collectors.toList());
    }

    // Método para actualizar un lugar existente
    @Override
    public PlaceResponse updatePlaceById(Long idPlace, PlaceRequest placeRequest) {
        UserModel user = getAuthenticatedUser();
        PlaceModel placeModel = placeJpaRepository.findById(idPlace)
                .orElseThrow(() -> new PlaceNotFoundException("Lugar no encontrado."));

        CategoryModel categoryModel = categoryJpaRepository.findById(placeRequest.getIdCategory())
                .orElseThrow(() -> new CategoryNotFoundException("Categoría no encontrada."));

        // Actualizar coordenadas si la dirección ha cambiado
        if (!placeModel.getAddress().equals(placeRequest.getAddress())) {
            double[] coordinates = getCoordinates(placeRequest.getAddress());
            placeModel.setLatitude(coordinates[0]);
            placeModel.setLength(coordinates[1]);
        }

        placeModel.setName(placeRequest.getName());
        placeModel.setDescription(placeRequest.getDescription());
        placeModel.setAddress(placeRequest.getAddress());
        placeModel.setCategory(categoryModel);
        placeModel.setUser(user);

        PlaceModel updatedPlace = placeJpaRepository.save(placeModel);

        return new PlaceResponse(
                updatedPlace.getIdPlace(),
                updatedPlace.getName(),
                updatedPlace.getDescription(),
                updatedPlace.getAddress(),
                updatedPlace.getCategory().getIdCategory().toString(),
                updatedPlace.getUser().getIdUser().toString()
        );
    }

    // Método para eliminar un lugar
    @Override
    public void deleteCategoryById(Long idPlace) {
        PlaceModel placeModel = placeJpaRepository.findById(idPlace)
                .orElseThrow(() -> new PlaceNotFoundException("Lugar no encontrado."));
        placeJpaRepository.delete(placeModel);
    }
}


