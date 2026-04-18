package org.switf.pixza.servicesImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.switf.pixza.exceptions.AuthenticationException;
import org.switf.pixza.exceptions.CategoryNotFoundException;
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

    // Método para obtener una categoría por su nombre
    private CategoryModel getCategoryByName(String categoryName) {
        return categoryJpaRepository.findByCategory(categoryName)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found"));
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
        CategoryModel category = getCategoryByName(placeRequest.getCategoryName()); // Obtener la categoría por nombre
        UserModel user = getAuthenticatedUser();

        PlaceModel placeModel = new PlaceModel();
        placeModel.setName(placeRequest.getName());
        placeModel.setDescription(placeRequest.getDescription());
        placeModel.setAddress(placeRequest.getAddress());
        placeModel.setImageUrl(placeRequest.getImageUrl());
        placeModel.setCategory(category);
        placeModel.setUser(user);

        PlaceModel savedPlace = placeJpaRepository.save(placeModel);

        return new PlaceResponse(
                savedPlace.getIdPlace(),
                savedPlace.getName(),
                savedPlace.getDescription(),
                savedPlace.getAddress(),
                savedPlace.getImageUrl(),
                savedPlace.getCategory().getCategory()
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
                        place.getImageUrl(),
                        place.getCategory().getCategory(),
                        place.getUser().getUsername()
                ))
                .collect(Collectors.toList());
    }

    //Método para listar los lugares de una categoría
    @Override
    public List<PlaceResponse> getPlacesByCategory(Long idCategory) {
        List<PlaceModel> places = placeJpaRepository.findByCategory_IdCategory(idCategory);
        return places.stream()
                .map(place -> new PlaceResponse(
                        place.getIdPlace(),
                        place.getName(),
                        place.getDescription(),
                        place.getAddress(),
                        place.getImageUrl(),
                        place.getCategory().getCategory(),
                        place.getUser().getUsername()))
                .collect(Collectors.toList());
    }


    // Método para actualizar un lugar existente
    @Override
    public PlaceResponse updatePlaceByName(String placeName, PlaceRequest newPlaceModel) {
        UserModel user = getAuthenticatedUser();

        // Buscar el lugar por nombre
        PlaceModel placeModel = (PlaceModel) placeJpaRepository.findByName(placeName)
                .orElseThrow(() -> new PlaceNotFoundException("Lugar no encontrado."));

        // Buscar la categoría por nombre
        CategoryModel categoryModel = categoryJpaRepository.findByCategory(newPlaceModel.getCategoryName())
                .orElseThrow(() -> new CategoryNotFoundException("Categoría: " + newPlaceModel.getCategoryName() + " no encontrada."));

        // Actualizar el lugar
        placeModel.setName(newPlaceModel.getName());
        placeModel.setDescription(newPlaceModel.getDescription());
        placeModel.setAddress(newPlaceModel.getAddress());
        placeModel.setImageUrl(newPlaceModel.getImageUrl());
        placeModel.setCategory(categoryModel);
        placeModel.setUser(user);

        // Guardar el lugar actualizado
        PlaceModel updatedPlace = placeJpaRepository.save(placeModel);

        return new PlaceResponse(
                updatedPlace.getIdPlace(),
                updatedPlace.getName(),
                updatedPlace.getDescription(),
                updatedPlace.getAddress(),
                updatedPlace.getImageUrl(),
                updatedPlace.getCategory().getCategory(),
                updatedPlace.getUser().getIdUser().toString()
        );
    }

    // Método para eliminar un lugar
    @Override
    public void deleteCategoryByName(String placeName) {
        PlaceModel placeModel = (PlaceModel) placeJpaRepository.findByName(placeName)
                .orElseThrow(() -> new PlaceNotFoundException("Lugar no encontrado."));
        placeJpaRepository.delete(placeModel);
    }

    @Override
    public PlaceResponse savePlaceUrlImage(String placeName, PlaceRequest urlImage) {
        UserModel user = getAuthenticatedUser();

        // Buscar el lugar por nombre
        PlaceModel placeModel = (PlaceModel) placeJpaRepository.findByName(placeName)
                .orElseThrow(() -> new PlaceNotFoundException("Lugar no encontrado."));

        // Actualizar la URL de la imagen
        String imageUrlReceived = urlImage.getImageUrl();
        placeModel.setImageUrl(imageUrlReceived);

        // Guardar los cambios en el repositorio
        PlaceModel updatedPlace = placeJpaRepository.save(placeModel);

        // Crear y devolver la respuesta
        PlaceResponse placeResponse = new PlaceResponse();
        placeResponse.setIdPlace(updatedPlace.getIdPlace());
        placeResponse.setName(updatedPlace.getName());
        placeResponse.setDescription(updatedPlace.getDescription());
        placeResponse.setAddress(updatedPlace.getAddress());
        placeResponse.setImageUrl(updatedPlace.getImageUrl());

        return placeResponse;
    }


    //Método para buscar un lugar
    @Override
    public List<PlaceResponse> searchPlaces(String searchTerm) {
        List<PlaceModel> placeModels = placeJpaRepository.findByNameContainingOrDescriptionContainingOrAddressContainingOrCategory_CategoryContaining(searchTerm, searchTerm, searchTerm, searchTerm);
        return placeModels.stream()
                .map(place -> new PlaceResponse(
                        place.getIdPlace(),
                        place.getName(),
                        place.getDescription(),
                        place.getAddress(),
                        place.getImageUrl(),
                        place.getCategory().getCategory()
                ))
                .collect(Collectors.toList());
    }
}


