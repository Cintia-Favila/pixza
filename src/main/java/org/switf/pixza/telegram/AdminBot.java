package org.switf.pixza.telegram;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.switf.pixza.request.CategoryRequest;
import org.switf.pixza.request.LoginRequest;
import org.switf.pixza.request.PlaceRequest;
import org.switf.pixza.response.CategoryResponse;
import org.switf.pixza.response.LoginResponse;
import org.switf.pixza.response.PlaceResponse;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.*;

@Component
public class AdminBot extends TelegramLongPollingBot {
    private final String botName;
    private final String botToken;
    private final RestTemplate restTemplate;
    private final Map<Long, String> userStates = new HashMap<>();
    private final Map<Long, String> authenticatedUsers = new HashMap<>();
    private final Map<Long, String> userTokens = new HashMap<>();

    public AdminBot(DefaultBotOptions botOptions,
                    @Value("${telegram.bot.name}") String botName,
                    @Value("${telegram.bot.token}") String botToken,
                    RestTemplate restTemplate) {
        super(botOptions);
        this.botName = botName;
        this.botToken = botToken;
        this.restTemplate = restTemplate;
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            handleMessage(update.getMessage());
        } else if (update.hasCallbackQuery()) {
            handleCallback(update.getCallbackQuery().getMessage().getChatId(), update.getCallbackQuery().getData());
        }
    }

    // Método para verificar si un usuario está autenticado
    private boolean isUserAuthenticated(Long chatId) {
        return !authenticatedUsers.containsKey(chatId);
    }

    //     Método para autenticar a un usuario y asociar su ID de chat con un token de autenticación
    public void authenticateUser(Long chatId, String authToken) {
        authenticatedUsers.put(chatId, authToken);
    }

    private void handleMessage(Message message) {
        Long chatId = message.getChatId();
        String text = message.getText();
        if (text.equals("/start")) {
            sendMenuStart(chatId);
        } else {
            handleState(message);
        }
    }

    private void handleCallback(Long chatId, String callbackData) {
        // Excluir verificación de autenticación para las acciones de "login" y "/start"
        if (!callbackData.equals("/start") && !callbackData.equals("login") && !callbackData.equals("places")) {
            // Verificar la autenticación antes de procesar otras acciones
            if (isUserAuthenticated(chatId)) {
                // Si el usuario no está autenticado, enviar un mensaje indicando que necesitan iniciar sesión
                sendText(chatId, "Debe iniciar sesión para acceder.");
                sendMenuStart(chatId);
                return;
            }
        }

        switch (callbackData) {
            case "/start" -> sendMenuStart(chatId);
            case "login" -> promptForUsername(chatId);
            case "logout" -> promptForLogout(chatId);
            case "add_category" -> promptForNewCategory(chatId);
            case "list_categories" -> listCategories(chatId);
            case "edit_category" -> promptForCategoryIdToEdit(chatId);
            case "delete_category" -> promptForCategoryIdToDelete(chatId);
            case "add_place" -> promptForNewPlace(chatId);
            case "list_places" -> listPlaces(chatId);
            case "edit_place" -> promptForPlaceIdToEdit(chatId);
            case "delete_place" -> promptForPlaceIdToDelete (chatId);
            case "places" -> promptForListCategoriesUser(chatId);
            default -> sendText(chatId, "Acción no reconocida: " + callbackData);
        }
    }

    private void promptForUsername(Long chatId) {
        sendText(chatId, "Ingresa tu usuario:");
        userStates.put(chatId, "LOGIN_USER");
    }

    private void promptForLogout(Long chatId){
        logout(chatId);
        userStates.remove(chatId);
    }

    private void promptForListCategoriesUser (Long chatId) {
        sendText(chatId, "Elige el ID de la categoria:");
        listCategoriesUser(chatId);
        userStates.put(chatId, "SENDING_PLACES");
    }

    private void promptForNewCategory(Long chatId) {
        sendText(chatId, "Nombre de la nueva categoría:");
        userStates.put(chatId, "ADDING_CATEGORY");
    }

    private void promptForCategoryIdToEdit(Long chatId) {
        sendText(chatId, "ID de la categoría a editar:");
        userStates.put(chatId, "EDIT_CATEGORY_ID");
    }

    private void promptForCategoryIdToDelete(Long chatId) {
        sendText(chatId, "ID de la categoría a eliminar:");
        userStates.put(chatId, "DELETING_CATEGORY");
    }

    private void promptForNewPlace(Long chatId) {
        sendText(chatId, "Nombre del lugar:");
        userStates.put(chatId, "ADDING_PLACE_NAME");
    }

    private void promptForPlaceIdToEdit (Long chatid){
        sendText(chatid, "ID del lugar a editar:");
        userStates.put(chatid, "EDIT_PLACE_ID");
    }

    private void promptForPlaceIdToDelete(Long chatId) {
        sendText(chatId, "ID del lugar a eliminar:");
        userStates.put(chatId, "DELETING_PLACE");
    }

    private void handleState(Message message) {
        Long chatId = message.getChatId();
        String state = userStates.get(chatId);

        if (state == null) {
            sendMenuCategories(chatId);
            return;
        }

        switch (state) {
            case "LOGIN_USER" -> handleLoginUser(chatId, message.getText());
            case "ADDING_CATEGORY" -> handleAddCategory(chatId, message.getText());
            case "EDIT_CATEGORY_ID" -> handleEditCategoryId(chatId, message.getText());
            case "DELETING_CATEGORY" -> handleDeleteCategory(chatId, message.getText());
            case "ADDING_PLACE_NAME" -> handleAddPlaceName(chatId, message.getText());
            case "EDIT_PLACE_ID" -> handleEditPlaceId(chatId, message.getText());
            case "DELETING_PLACE" -> handleDeletePlace(chatId, message.getText());
            case "SENDING_PLACES" -> handleSendingPlaces(chatId, message.getText());
            default -> handlePlaceState(chatId, state, message.getText());
        }
    }

    private void handleLoginUser(Long chatId, String username) {
        sendText(chatId, "Ingresa el password:");
        userStates.put(chatId, "LOGIN_PASSWORD_" + username);
    }

    private void handleAddCategory(Long chatId, String category) {
        addCategory(chatId, category);
    }

    private void handleEditCategoryId(Long chatId, String categoryId) {
        sendText(chatId, "Nuevo nombre de la categoría:");
        userStates.put(chatId, "EDIT_CATEGORY_NAME_" + categoryId);
    }

    private void handleDeleteCategory(Long chatId, String categoryId) {
        deleteCategory(chatId, categoryId);
    }

    private void handleAddPlaceName(Long chatId, String placeName) {
        sendText(chatId, "Descripción del lugar:");
        userStates.put(chatId, "ADDING_PLACE_DESCRIPTION_" + placeName);
    }

    private void handleEditPlaceId (Long chatId, String idPlace){
        sendText(chatId, "Nuevo nombre del lugar:");
        userStates.put(chatId, "EDIT_PLACE_NAME_" + idPlace);
    }

    private void handleDeletePlace(Long chatId, String IdPlace) {
        deletePlace(chatId, IdPlace);
    }

    private void handleSendingPlaces(Long chatId, String idCategory) {
        listPlacesByCategory(chatId, idCategory);
        userStates.remove(chatId);
    }

    private void handlePlaceState(Long chatId, String state, String text) {
        if (state.startsWith("LOGIN_PASSWORD_")) {
            String username = state.substring("LOGIN_PASSWORD_".length());
            login(chatId, username, text);
        } else if (state.startsWith("EDIT_CATEGORY_NAME_")) {
            String categoryId = state.substring("EDIT_CATEGORY_NAME_".length());
            editCategory(chatId, categoryId, text);
        } else if (state.startsWith("ADDING_PLACE_DESCRIPTION_")) {
            String placeName = state.substring("ADDING_PLACE_DESCRIPTION_".length());
            handleAddPlaceDescription(chatId, placeName, text);
        } else if (state.startsWith("ADDING_PLACE_ADDRESS_")) {
            String[] parts = state.split("_");
            String placeName = parts[3];
            String placeDescription = parts[4];
            handleAddPlaceAddress(chatId, placeName, placeDescription, text);
        } else if (state.startsWith("ADDING_PLACE_CATEGORY_")) {
            String[] parts = state.split("_");
            String placeName = parts[3];
            String placeDescription = parts[4];
            String placeAddress = parts[5];
            handleAddPlaceCategory(chatId, placeName, placeDescription, placeAddress, text);
        } else if (state.startsWith("EDIT_PLACE_NAME_")) {
            String idPlace = state.substring("EDIT_PLACE_NAME_".length());
            handleEditPlaceName(chatId, idPlace, text);
        } else if (state.startsWith("EDIT_PLACE_DESCRIPTION_")) {
            String[] parts = state.split("_");
            String idPlace = parts[3];
            String newPlaceName = parts[4];
            handleEditPlaceDescription(chatId, idPlace, newPlaceName, text);
        } else if (state.startsWith("EDIT_PLACE_ADDRESS_")) {
            String[] parts = state.split("_");
            String idPlace = parts[3];
            String newPlaceName = parts[4];
            String newDescription = parts[5];
            handleEditPlaceAddress(chatId, idPlace, newPlaceName, newDescription, text);
        } else if (state.startsWith("EDIT_PLACE_CATEGORY_")) {
            String[] parts = state.split("_");
            String idPlace = parts[3];
            String newPlaceName = parts[4];
            String newDescription = parts[5];
            String newAddress = parts[6];
            handleEditPlaceCategory(chatId, idPlace, newPlaceName, newDescription, newAddress, text);
        }
    }

    private void handleAddPlaceDescription(Long chatId, String placeName, String description) {
        sendText(chatId, "Dirección del lugar:");
        userStates.put(chatId, "ADDING_PLACE_ADDRESS_" + placeName + "_" + description);
    }

    private void handleAddPlaceAddress(Long chatId, String placeName, String description, String address) {
        sendText(chatId, "ID de la categoría:");
        userStates.put(chatId, "ADDING_PLACE_CATEGORY_" + placeName + "_" + description + "_" + address);
    }

    private void handleAddPlaceCategory(Long chatId, String placeName, String description, String address, String categoryId) {
        addPlace(chatId, placeName, description, address, categoryId);
    }

    private void handleEditPlaceName(Long chatId, String idPlace, String newPlaceName) {
        sendText(chatId, "Nueva descripción del lugar:");
        userStates.put(chatId, "EDIT_PLACE_DESCRIPTION_" + idPlace + "_" + newPlaceName);
    }

    private void handleEditPlaceDescription(Long chatId, String idPlace, String newPlaceName, String newDescription) {
        sendText(chatId, "Nueva dirección del lugar:");
        userStates.put(chatId, "EDIT_PLACE_ADDRESS_" + idPlace + "_" + newPlaceName + "_" + newDescription);
    }

    private void handleEditPlaceAddress(Long chatId, String idPlace, String newPlaceName, String newDescription, String newAddress) {
        sendText(chatId, "Nuevo ID de la categoría:");
        userStates.put(chatId, "EDIT_PLACE_CATEGORY_" + idPlace + "_" + newPlaceName + "_" + newDescription + "_" + newAddress);
    }

    private void handleEditPlaceCategory(Long chatId, String idPlace, String newPlaceName, String newDescription, String newAddress, String newCategoryId) {
        editPlace(chatId, idPlace, newPlaceName, newDescription, newAddress, newCategoryId);
    }

    private void sendMenuStart(Long chatId) {
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        rowsInline.add(Arrays.asList(
                InlineKeyboardButton.builder().text("Ver Lugares").callbackData("places").build(),
                InlineKeyboardButton.builder().text("Login").callbackData("login").build()
        ));
        sendMenu(chatId, rowsInline, "Bienvenido al bot. ¿Qué deseas hacer?");
    }

    private void sendMenuCategories(Long chatId) {
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        rowsInline.add(Arrays.asList(
                InlineKeyboardButton.builder().text("Añadir Categoría").callbackData("add_category").build(),
                InlineKeyboardButton.builder().text("Listar Categorías").callbackData("list_categories").build()
        ));
        rowsInline.add(Arrays.asList(
                InlineKeyboardButton.builder().text("Editar Categoría").callbackData("edit_category").build(),
                InlineKeyboardButton.builder().text("Eliminar Categoría").callbackData("delete_category").build()
        ));
        rowsInline.add(Arrays.asList(
                InlineKeyboardButton.builder().text("Añadir Lugar").callbackData("add_place").build(),
                InlineKeyboardButton.builder().text("Listar Lugares").callbackData("list_places").build()
        ));
        rowsInline.add(Arrays.asList(
                InlineKeyboardButton.builder().text("Editar Lugar").callbackData("edit_place").build(),
                InlineKeyboardButton.builder().text("Eliminar Lugar").callbackData("delete_place").build()
        ));

        rowsInline.add(Collections.singletonList(
                InlineKeyboardButton.builder().text("Cerrar Sesión").callbackData("logout").build()
        ));
        sendMenu(chatId, rowsInline, "Menú de Categorías:");
    }

    private void sendMenu(Long chatId, List<List<InlineKeyboardButton>> rowsInline, String text) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId.toString())
                .text(text)
                .replyMarkup(InlineKeyboardMarkup.builder().keyboard(rowsInline).build())
                .build();

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendText(Long chatId, String text) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId.toString())
                .text(text)
                .build();

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void login(Long chatId, String user, String password) {
        String url = "http://localhost:8080/auth/login";
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(user);
        loginRequest.setPassword(password);

        try {
            ResponseEntity<?> responseEntity = restTemplate.postForEntity(url, loginRequest, LoginResponse.class);
            responseEntity.getStatusCode().is2xxSuccessful();
            LoginResponse loginResponse = (LoginResponse) responseEntity.getBody();
            assert loginResponse != null;
            String token = ((LoginResponse) responseEntity.getBody()).getToken();
            userTokens.put(chatId, token); // Almacenar el token para el usuario
            sendText(chatId, "Bienvenido " + loginResponse.getUsername());
            // Autenticar al usuario después del inicio de sesión exitoso
            authenticateUser(chatId, token);
            sendMenuCategories(chatId);
        } catch (HttpClientErrorException e) {
            sendText(chatId, "Error " + e.getMessage());
            sendMenuStart(chatId);
        } catch (RestClientException e) {
            // Capturar otras excepciones relacionadas con errores de REST, como errores de serialización o deserialización
            sendText(chatId, "Error " + e.getMessage());
            sendMenuStart(chatId);
        }
    }

    public void logout(Long chatId) {
        if (isUserAuthenticated(chatId)) {
            sendText(chatId, "No estas logeado");
            sendMenuStart(chatId);
            return;
        }
        HttpHeaders headers = new HttpHeaders();
        String token = userTokens.get(chatId);
        if (token != null) {
            headers.setBearerAuth(token); // Obtener el token del usuario
        }
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        String url = "http://localhost:8080/auth/logout";
        try {
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, requestEntity, String.class);
            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                sendText(chatId, "Logout exitoso");
                authenticatedUsers.remove(chatId);
                userTokens.remove(chatId); // Eliminar el token del mapa
            } else {
                sendText(chatId, "Error al realizar logout");
            }
        } catch (RestClientException e) {
            sendText(chatId, "Error al realizar logout: " + e.getMessage());
        }
        sendMenuStart(chatId);
    }

    //CRUD CATEGORIAS
    private void addCategory(Long chatId, String category) {
        if (isUserAuthenticated(chatId)) {
            sendText(chatId, "Debe iniciar sesión para acceder a esta funcionalidad.");
            sendMenuStart(chatId);
            return;
        }
        String url = "http://localhost:8080/categories/newCategory";
        CategoryRequest request = new CategoryRequest();
        request.setCategory(category);

        HttpHeaders headers = new HttpHeaders();
        String token = userTokens.get(chatId);
        if (token != null) {
            headers.setBearerAuth(token); // Obtener el token del usuario
        }
        HttpEntity<CategoryRequest> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    url, HttpMethod.POST, entity, String.class);

            String response = responseEntity.getBody();
            if (response != null) {
                sendText(chatId, response);
            } else {
                sendText(chatId, "Error al crear la categoría.");
            }
        } catch (Exception e) {
            sendText(chatId, "Error al crear la categoría: " + e.getMessage());
        } finally {
            sendMenuCategories(chatId);
        }
    }
    private void listCategories(Long chatId) {
        if (isUserAuthenticated(chatId)) {
            sendText(chatId, "Debe iniciar sesión para acceder a esta funcionalidad.");
            sendMenuStart(chatId);
            return;
        }

        String token = userTokens.get(chatId); // Obtener el token del usuario
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        HttpEntity<Void> request = new HttpEntity<>(headers);
        String url = "http://localhost:8080/categories/allCategories";

        try {
            ResponseEntity<List<CategoryResponse>> responseEntity = restTemplate.exchange(url, HttpMethod.GET, request, new ParameterizedTypeReference<List<CategoryResponse>>() {});
            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
                StringBuilder categoryList = new StringBuilder("Categorías:\n");
                for (CategoryResponse category : responseEntity.getBody()) {
                    categoryList.append("ID: ").append(category.getIdCategory()).append(", Nombre: ").append(category.getCategory()).append("\n");
                }
                sendText(chatId, categoryList.toString());
            } else {
                sendText(chatId, "No se encontraron categorías.");
            }
        } catch (Exception e) {
            sendText(chatId, "Error al cargar categorías: " + e.getMessage());
        } finally {
            sendMenuCategories(chatId);
        }
    }
    private void editCategory(Long chatId, String idCategory, String newCategory) {
        if (isUserAuthenticated(chatId)) {
            sendText(chatId, "Debe iniciar sesión para acceder a esta funcionalidad.");
            sendMenuStart(chatId);
            return;
        }

        String token = userTokens.get(chatId); // Obtener el token del usuario
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        CategoryRequest categoryRequest = new CategoryRequest();
        categoryRequest.setCategory(newCategory);
        HttpEntity<CategoryRequest> request = new HttpEntity<>(categoryRequest, headers);

        try {
            long id = Long.parseLong(idCategory);
            String url = "http://localhost:8080/categories/edit/" + id;
            restTemplate.exchange(url, HttpMethod.PUT, request, Void.class);
            sendText(chatId, "ID editado: " + idCategory + " Nueva categoría: " + newCategory);
        } catch (NumberFormatException e) {
            sendText(chatId, "ID inválido.");
        } catch (Exception e) {
            sendText(chatId, "Error: " + e.getMessage());
        } finally {
            sendMenuCategories(chatId);
        }
    }
    private void deleteCategory(Long chatId, String idCategory) {
        if (isUserAuthenticated(chatId)) {
            sendText(chatId, "Debe iniciar sesión para acceder a esta funcionalidad.");
            sendMenuStart(chatId);
            return;
        }

        String token = userTokens.get(chatId);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            long id = Long.parseLong(idCategory); // Convertir la cadena a Long
            String url = "http://localhost:8080/categories/deleteCategory/" + id;
            restTemplate.exchange(url, HttpMethod.DELETE, request, Void.class);
            sendText(chatId, "Categoría " + idCategory + " eliminada exitosamente.");
        } catch (NumberFormatException e) {
            sendText(chatId, "ID inválido.");
        } catch (Exception e) {
            sendText(chatId, "Error " + e.getMessage());
        } finally {
            sendMenuCategories(chatId);
        }
    }

    //CRUD LUGARES
    private void addPlace(Long chatId, String placeName, String placeDescription, String placeAddress, String idCategory){
        if (isUserAuthenticated(chatId)) {
            sendText(chatId, "Debe iniciar sesión para acceder a esta funcionalidad.");
            sendMenuStart(chatId);
            return;
        }

        long id = Long.parseLong(idCategory);
        String url = "http://localhost:8080/places/addPlace";
        PlaceRequest request = new PlaceRequest();
        request.setName(placeName);
        request.setDescription(placeDescription);
        request.setAddress(placeAddress);
        request.setIdCategory(id);

        HttpHeaders headers = new HttpHeaders();
        String token = userTokens.get(chatId);
        if (token != null) {
            headers.setBearerAuth(token); // Obtener el token del usuario
        }
        HttpEntity<PlaceRequest> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    url, HttpMethod.POST, entity, String.class);

            String response = responseEntity.getBody();
            if (response != null) {
                sendText(chatId, response);
            } else {
                sendText(chatId, "Error al crear la categoría.");
            }
        } catch (Exception e) {
            sendText(chatId, "Error al crear la categoría: " + e.getMessage());
        } finally {
            sendMenuCategories(chatId);
        }
    }

    private void listPlaces(Long chatId) {
        if (isUserAuthenticated(chatId)) {
            sendText(chatId, "Debe iniciar sesión para acceder a esta funcionalidad.");
            sendMenuStart(chatId);
            return;
        }

        String token = userTokens.get(chatId); // Obtener el token del usuario
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        HttpEntity<Void> request = new HttpEntity<>(headers);
        String url = "http://localhost:8080/places/allPlaces";

        try {
            ResponseEntity<List<PlaceResponse>> responseEntity = restTemplate.exchange(url, HttpMethod.GET, request, new ParameterizedTypeReference<List<PlaceResponse>>() {});
            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
                StringBuilder placeList = new StringBuilder("Lugares:\n\n");
                for (PlaceResponse place : responseEntity.getBody()) {
                    placeList.append("ID: ").append(place.getIdPlace()).append("\n")
                            .append("Nombre: ").append(place.getName()).append("\n")
                            .append("Descripción: ").append(place.getDescription()).append("\n")
                            .append("Dirección: ").append(place.getAddress()).append("\n")
                            .append("Categoria: ").append(place.getCategory()).append("\n")
                            .append("\n\n");
                }
                sendText(chatId, placeList.toString());
            } else {
                sendText(chatId, "No se encontraron lugares.");
            }
        } catch (Exception e) {
            sendText(chatId, "Error al cargar lugares: " + e.getMessage());
        } finally {
            sendMenuCategories(chatId);
        }
    }

    private void editPlace(Long chatId, String idPlace, String newPlaceName, String newDescription, String newAddress, String newCategoryId) {
        if (isUserAuthenticated(chatId)) {
            sendText(chatId, "Debe iniciar sesión para acceder a esta funcionalidad.");
            sendMenuStart(chatId);
            return;
        }
        String token = userTokens.get(chatId); // Obtener el token del usuario
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        long idCategory = Long.parseLong(newCategoryId);
        PlaceRequest placeRequest = new PlaceRequest();
        placeRequest.setName(newPlaceName);
        placeRequest.setDescription(newDescription);
        placeRequest.setAddress(newAddress);
        placeRequest.setIdCategory(idCategory);
        HttpEntity<PlaceRequest> request = new HttpEntity<>(placeRequest, headers);

        try {
            long id = Long.parseLong(idPlace);
            String url = "http://localhost:8080/places//updatePlace/" + id;
            restTemplate.exchange(url, HttpMethod.PATCH, request, Void.class);
            sendText(chatId, "ID editado: " + idPlace + ", Nombre: " + Objects.requireNonNull(request.getBody()).getName());
        } catch (RestClientException e) {
            sendText(chatId, "Error al editar lugar: " + e.getMessage());
        } finally {
            sendMenuCategories(chatId);
        }
    }

    private void deletePlace(Long chatId, String idPlace) {
        if (isUserAuthenticated(chatId)) {
            sendText(chatId, "Debe iniciar sesión para acceder a esta funcionalidad.");
            sendMenuStart(chatId);
            return;
        }
        String token = userTokens.get(chatId); // Obtener el token del usuario
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            long id = Long.parseLong(idPlace); // Convertir la cadena a Long
            String url = "http://localhost:8080/places/deletePlace/" + id;
            restTemplate.exchange(url, HttpMethod.DELETE, request, Void.class);
            sendText(chatId, "Lugar " + idPlace + " eliminado exitosamente.");
        } catch (NumberFormatException e) {
            sendText(chatId, "ID inválido.");
        } catch (Exception e) {
            sendText(chatId, "Error " + e.getMessage());
        } finally {
            sendMenuCategories(chatId);
        }
    }

    //METODOS DE USUARIOS
    //Método para que el usuario elija una categoria
    private void listCategoriesUser(Long chatId) {
        String url = "http://localhost:8080/categories/allCategories";

        try {
            ResponseEntity<List<CategoryResponse>> responseEntity = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), new ParameterizedTypeReference<List<CategoryResponse>>() {});
            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
                StringBuilder categoryList = new StringBuilder();
                for (CategoryResponse category : responseEntity.getBody()) {
                    categoryList.append("ID: ").append(category.getIdCategory()).append(", Nombre: ").append(category.getCategory()).append("\n");
                }
                sendText(chatId, categoryList.toString());
            }
        } catch (Exception e) {
            sendText(chatId, "Error al cargar categorías: " + e.getMessage());
        }

    }

    //Metodo para listar lugares por categoria al usuario
    private void listPlacesByCategory(Long chatId, String idCategory) {
        try {
            // Convertir el idCategory de String a Long
            long id = Long.parseLong(idCategory);
            String url = "http://localhost:8080/places/placesByCategory/" + id;

            ResponseEntity<List<PlaceResponse>> responseEntity = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(new HttpHeaders()),
                    new ParameterizedTypeReference<List<PlaceResponse>>() {}
            );

            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
                List<PlaceResponse> places = responseEntity.getBody();
                if (places.isEmpty()) {
                    sendText(chatId, "No se encontraron lugares para la categoría: " + idCategory);
                } else {
                    StringBuilder placeList = new StringBuilder("Lugares:\n\n");
                    for (PlaceResponse place : places) {
                        placeList.append("ID: ").append(place.getIdPlace()).append("\n")
                                .append("Nombre: ").append(place.getName()).append("\n")
                                .append("Descripción: ").append(place.getDescription()).append("\n")
                                .append("Dirección: ").append(place.getAddress()).append("\n")
                                .append("\n");
                    }
                    sendText(chatId, placeList.toString());
                }
            }
        } catch (NumberFormatException e) {
            sendText(chatId, "ID de categoría no válido. Por favor, ingresa un número válido.");
        } catch (Exception e) {
            sendText(chatId, "Error al cargar lugares: " + e.getMessage());
        } finally {
            sendMenuStart(chatId);
        }
    }
}