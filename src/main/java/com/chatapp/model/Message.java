package com.chatapp.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Classe représentant un message envoyé entre le client et le serveur
 */
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    private String expediteur;
    private String contenu;
    private LocalDateTime horodatage;
    private TypeMessage type;

    public enum TypeMessage {
        CONNEXION,
        DECONNEXION,
        MESSAGE,
        LISTE_UTILISATEURS
    }

    public Message(String expediteur, String contenu, TypeMessage type) {
        this.expediteur = expediteur;
        this.contenu = contenu;
        this.type = type;
        this.horodatage = LocalDateTime.now();
    }

    public String getExpediteur() {
        return expediteur;
    }

    public String getContenu() {
        return contenu;
    }

    public TypeMessage getType() {
        return type;
    }

    public LocalDateTime getHorodatage() {
        return horodatage;
    }

    public String getHorodatageFormate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        return horodatage.format(formatter);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s: %s",
                getHorodatageFormate(),
                expediteur,
                contenu);
    }
}