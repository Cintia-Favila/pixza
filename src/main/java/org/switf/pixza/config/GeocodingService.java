package org.switf.pixza.config;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class GeocodingService {

    @Value("${openstreetmap.url}")
    private String openStreetMapUrl;

    public String getLatLongFromAddress(String address) {
        try {
            String formattedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8);
            String urlString = String.format("%s?q=%s&format=json", openStreetMapUrl, formattedAddress);

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }

                JSONArray jsonResponse = new JSONArray(response.toString());
                if (!jsonResponse.isEmpty()) {
                    JSONObject location = jsonResponse.getJSONObject(0);
                    String lat = location.getString("lat");
                    String lon = location.getString("lon");
                    return lat + "," + lon;
                } else {
                    return "Error: No se encontraron resultados para la dirección especificada";
                }
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}



