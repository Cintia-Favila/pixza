package org.switf.pixza.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.switf.pixza.exceptions.CategoryNotFoundException;
import org.switf.pixza.request.PlaceRequest;
import org.switf.pixza.response.PlaceResponse;
import org.switf.pixza.services.PlaceService;

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
        }catch (Exception e) {
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
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al cargar lugares: " + e.getMessage());
        }
    }

    @PatchMapping("/updatePlace/{idPlace}")
    public ResponseEntity<?> updatePlace(@PathVariable Long idPlace, @RequestBody PlaceRequest placeRequest) {
        try {
            PlaceResponse updatedPlace = placeService.updatePlaceById(idPlace, placeRequest);
            return ResponseEntity.ok(updatedPlace);
        } catch (CategoryNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("ID " + e.getMessage() + " no encontrado");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/deletePlace/{idPlace}")
    public ResponseEntity<?> deletePlace(@PathVariable Long idPlace) {
        try {
            placeService.deleteCategoryById(idPlace);
            return ResponseEntity.ok("Lugar eliminado: " + idPlace);
        } catch (CategoryNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("ID " + e.getMessage() + " no encontrado");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al eliminar el lugar: " + e.getMessage());
        }
    }
}
