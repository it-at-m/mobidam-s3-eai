/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2023
 */
package de.muenchen.mobidam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot Anwendung, die alle Camel Routen startet.
 */
@SpringBootApplication
public class Application {

    /**
     * Startet die Anwendung.
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
