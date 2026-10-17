package com.luxuriusroom.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReporteResenaRequest(@NotBlank @Size(max = 255) String motivo) {
}