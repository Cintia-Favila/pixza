package org.switf.pixza.services;

import org.switf.pixza.request.LoginRequest;
import org.switf.pixza.request.RegisterRequest;
import org.switf.pixza.response.LoginResponse;

public interface AuthService {

    String register(RegisterRequest request);

    LoginResponse login(LoginRequest loginRequest);

    void logout(String token);
}
