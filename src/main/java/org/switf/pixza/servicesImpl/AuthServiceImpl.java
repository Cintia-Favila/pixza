package org.switf.pixza.servicesImpl;

import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.switf.pixza.jwt.JwtService;
import org.switf.pixza.models.UserModel;
import org.switf.pixza.repositories.UserJpaRepository;
import org.switf.pixza.request.LoginRequest;
import org.switf.pixza.request.RegisterRequest;
import org.switf.pixza.response.LoginResponse;
import org.switf.pixza.services.AuthService;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserJpaRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public String register(RegisterRequest request) {
        Optional<UserModel> existingUser = userRepository.findByUsername(request.getUsername());
        if (existingUser.isPresent()) {
            return "El nombre de usuario ya está en uso";
        }
        UserModel userModel = UserModel.builder()
                .username(request.getUsername())
                .password(bCryptPasswordEncoder.encode(request.getPassword()))
                .build();
        userRepository.save(userModel);
        return "¡Registro exitoso para el usuario: " + request.getUsername() + "!";
    }

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        Optional<UserModel> userModelOptional = userRepository.findByUsername(loginRequest.getUsername());

        if (userModelOptional.isEmpty()) {
            throw new UsernameNotFoundException("Usuario incorrecto");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Credenciales incorrectas");
        }

        UserDetails userDetails = userModelOptional.get();

        String token = jwtService.getToken(userDetails);

        return LoginResponse.builder()
                .token(token)
                .username(loginRequest.getUsername())
                .build();
    }
    @Override
    public void logout(String token) {
        try {
            jwtService.addToBlacklist(token);
        } catch (Exception e) {
            throw new JwtException("Error al procesar el token JWT: " + e.getMessage());
        }
    }
}
