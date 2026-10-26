package com.centromedico.service;

import com.centromedico.dto.LoginRequest;
import com.centromedico.dto.LoginResponse;
import com.centromedico.exception.CredencialesInvalidasException;
import com.centromedico.model.Usuario;
import com.centromedico.repository.UsuarioRepository;
import com.centromedico.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest peticion) {
        Usuario usuario = usuarioRepository.findByUsername(peticion.username())
                .orElseThrow(() -> new CredencialesInvalidasException("Usuario o contraseña incorrectos"));

        if (!usuario.isActivo()) {
            throw new CredencialesInvalidasException("El usuario se encuentra deshabilitado");
        }

        // Se compara el hash almacenado, nunca la contraseña en texto plano.
        if (!passwordEncoder.matches(peticion.password(), usuario.getPassword())) {
            throw new CredencialesInvalidasException("Usuario o contraseña incorrectos");
        }

        String token = jwtService.generarToken(usuario.getUsername(), usuario.getRol().name());

        return new LoginResponse(token,
                usuario.getUsername(),
                usuario.getNombreCompleto(),
                usuario.getRol().name());
    }
}
