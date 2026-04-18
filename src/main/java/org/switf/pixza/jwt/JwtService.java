package org.switf.pixza.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import java.security.Key;
import java.security.SecureRandom;
import java.util.*;
import java.util.function.Function;

@Service
public class JwtService {

    // Generar una clave secreta automáticamente al inicio de la aplicación
    private static final String SECRET_KEY = generateSecretKey();

    // Método para generar una clave secreta
    private static String generateSecretKey() {
        // Definir el tamaño de la clave (en bytes)
        int keySize = 256 / 8; // 256 bits

        // Crear un generador de números aleatorios seguro
        SecureRandom secureRandom = new SecureRandom();

        // Crear una matriz de bytes para almacenar la clave aleatoria
        byte[] keyBytes = new byte[keySize];

        // Generar bytes aleatorios utilizando SecureRandom
        secureRandom.nextBytes(keyBytes);

        // Convertir los bytes en una cadena codificada en Base64
        return Base64.getEncoder().encodeToString(keyBytes);
    }

    public String getToken(UserDetails userDetails) {
        return getToken(new HashMap<>(), userDetails);
    }

    private String getToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        JwtBuilder jwtBuilder = Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 24))
                .signWith(getKey(), SignatureAlgorithm.HS256);
        for (Map.Entry<String, Object> entry : extraClaims.entrySet()) {
            jwtBuilder.claim(entry.getKey(), entry.getValue());
        }

        return jwtBuilder.compact();
    }

    private Key getKey() {
        String secretKey = SECRET_KEY.replace("\n", "");
        byte[] keyBytes = Base64.getDecoder().decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String getUserNameFromToken(String token) {
        return getClaim(token, Claims::getSubject);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = getUserNameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private Claims getAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()  // <- Este método construye un JwtParser a partir del JwtParserBuilder
                .parseClaimsJws(token)
                .getBody();
    }

    public <T> T getClaim (String token, Function<Claims,T> claimsResolver){
        final Claims claims = getAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Date getExpiration (String token){
        return getClaim(token, Claims::getExpiration);
    }

    private boolean isTokenExpired (String token){
        return getExpiration(token).before(new Date());
    }

    private Set<String> blacklist = new HashSet<>();

    public void addToBlacklist(String token) {
        blacklist.add(token);
    }

}