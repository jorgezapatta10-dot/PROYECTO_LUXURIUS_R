package com.luxuriusroom.service;

import com.luxuriusroom.model.Auditoria;
import com.luxuriusroom.model.Usuario;
import com.luxuriusroom.repository.AuditoriaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuditoriaService {
    private final AuditoriaRepository auditoriaRepository;

    public AuditoriaService(AuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    public void guardar(Usuario usuario, String accion, String entidad, Long entidadId, String detalle) {
        Auditoria auditoria = new Auditoria();
        auditoria.setUsuario(usuario);
        auditoria.setAccion(accion);
        auditoria.setEntidad(entidad);
        auditoria.setEntidadId(entidadId);
        auditoria.setDetalle(detalle);
        auditoria.setCreadoEn(LocalDateTime.now());
        auditoriaRepository.save(auditoria);
    }
}
