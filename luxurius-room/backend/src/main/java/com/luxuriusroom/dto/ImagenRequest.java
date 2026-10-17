package com.luxuriusroom.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ImagenRequest(@NotBlank @Size(max = 500) String url) {
}