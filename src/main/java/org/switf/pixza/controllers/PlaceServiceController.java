package org.switf.pixza.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.switf.pixza.exceptions.CategoryNotFoundException;
import org.switf.pixza.exceptions.PlaceNotFoundException;
import org.switf.pixza.request.PlaceRequest;
import org.switf.pixza.response.PlaceResponse;
import org.switf.pixza.services.PlaceService;

import java.io.*;
import java.util.List;

@RestController
@RequestMapping("/places")
public class PlaceServiceController {

    @Autowired
    private PlaceService placeService;

    @PostMapping("/addPlace")
    public ResponseEntity<?> createPlace(@RequestBody PlaceRequest placeRequest) {
        try {
            PlaceResponse createdPlace = placeService.createPlace(placeRequest);
            return ResponseEntity.ok("Lugar creado: " + createdPlace.getName());
        } catch (CategoryNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al crear lugar: " + e.getMessage());
        }
    }

    @GetMapping("/allPlaces")
    public ResponseEntity<?> getAllPlaces () {
        try {
            List<PlaceResponse> places = placeService.getAllPlaces();
            return ResponseEntity.ok(places);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al cargar lugares: " + e.getMessage());
        }
    }

    @GetMapping("/placesByCategory/{idCategory}")
    public ResponseEntity<?> getPlacesByCategory(@PathVariable Long idCategory){
        try {
            List<PlaceResponse> places = placeService.getPlacesByCategory(idCategory);
            return ResponseEntity.ok(places);
        } catch (CategoryNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al cargar lugares: " + e.getMessage());
        }
    }

    @PatchMapping("/updatePlace")
    public ResponseEntity<?> updatePlace(@RequestParam String placeName, @RequestBody PlaceRequest newPlaceModel) {
        try {
            PlaceResponse updatedPlace = placeService.updatePlaceByName(placeName, newPlaceModel);
            return ResponseEntity.ok(updatedPlace);
        } catch (PlaceNotFoundException | CategoryNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }


    @DeleteMapping("/deletePlace")
    public ResponseEntity<?> deletePlace(@RequestParam String placeName) {
        try {
            placeService.deleteCategoryByName(placeName);
            return ResponseEntity.ok("Lugar eliminado: " + placeName);
        } catch (CategoryNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("ID " + e.getMessage() + " no encontrado");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al eliminar el lugar: " + e.getMessage());
        }
    }

    @PatchMapping("/addImage")
    public ResponseEntity<?> addPlaceUrlImage(@RequestParam String placeName, @RequestBody PlaceRequest urlImage){
        try {
            PlaceResponse updatedPlace = placeService.savePlaceUrlImage(placeName, urlImage);
            return ResponseEntity.ok("Imagen guardada con éxito" + updatedPlace.getName());
        } catch (CategoryNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al cargar la imagen: " + e.getMessage());
        }
    }
}
