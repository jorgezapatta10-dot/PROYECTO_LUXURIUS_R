package com.luxuriusroom.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PasswordChangeRequest(
    @NotBlank String passwordActual,
    @NotBlank @Size(min = 8) @Pattern(regexp = "(?=.*[A-Za-z])(?=.*\\d).*") String passwordNueva
) {
}
