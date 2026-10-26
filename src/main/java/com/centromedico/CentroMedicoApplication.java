package com.centromedico;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;

/**
 * La autenticación se resuelve con JWT contra la tabla de usuarios, así que se
 * excluye el usuario en memoria que Spring Security crea por defecto.
 */
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class CentroMedicoApplication {

    public static void main(String[] args) {
        SpringApplication.run(CentroMedicoApplication.class, args);
    }
}
