package org.switf.pixza.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "El nombre de usuario no puede estar en blanco")
    String username;
    @NotBlank(message = "La contraseña no puede estar en blanco")
    String password;
}
