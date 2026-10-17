package com.luxuriusroom.dto;

import com.luxuriusroom.model.RolUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegistroRequest(
    @NotBlank String nombre,
    @NotBlank String apellido,
    @NotBlank @Email String email,
    @NotBlank @Size(min = 8) @Pattern(regexp = "(?=.*[A-Za-z])(?=.*\\d).*") String password,
    String telefono,
    @NotNull RolUsuario rol
) {
}
