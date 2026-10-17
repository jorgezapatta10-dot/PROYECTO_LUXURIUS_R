package com.luxuriusroom.controller;

import com.luxuriusroom.service.ResenaService;
import com.luxuriusroom.service.UsuarioService;
import com.luxuriusroom.dto.ResenaRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api")
public class ResenaController {
    private final ResenaService resenaService;
    private final UsuarioService usuarioService;

    public ResenaController(ResenaService resenaService, UsuarioService usuarioService) {
        this.resenaService = resenaService;
        this.usuarioService = usuarioService;
    }

    @PostMapping("/resenas")
    public ResponseEntity<?> crear(@Valid @RequestBody ResenaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(resenaService.crear(usuarioService.obtenerUsuarioActual(), request));
    }
}
