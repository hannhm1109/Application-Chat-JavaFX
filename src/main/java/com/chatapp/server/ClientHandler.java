package com.chatapp.server;

import com.chatapp.model.Message;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Classe gérant la connexion avec un client
 */
public class ClientHandler implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(ClientHandler.class.getName());

    private final Server server;
    private final Socket clientSocket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private String nomUtilisateur;
    private boolean isRunning = true;

    public ClientHandler(Server server, Socket clientSocket) {
        this.server = server;
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            // Initialisation des flux d'entrée/sortie
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            in = new ObjectInputStream(clientSocket.getInputStream());

            // Boucle principale pour recevoir les messages
            while (isRunning) {
                try {
                    Message message = (Message) in.readObject();
                    traiterMessage(message);
                } catch (ClassNotFoundException e) {
                    LOGGER.log(Level.WARNING, "Objet inconnu reçu", e);
                }
            }
        } catch (IOException e) {
            if (isRunning) {
                LOGGER.log(Level.INFO, "Client déconnecté: {0}",
                        (nomUtilisateur != null ? nomUtilisateur : "Inconnu"));
            }
        } finally {
            fermer();
        }
    }

    /**
     * Traite un message reçu selon son type
     */
    private void traiterMessage(Message message) {
        switch (message.getType()) {
            case CONNEXION:
                nomUtilisateur = message.getExpediteur();
                server.ajouterAuJournal("Nouvel utilisateur connecté: " + nomUtilisateur);
                server.diffuser(message);
                server.envoyerListeUtilisateurs();
                break;

            case DECONNEXION:
                server.diffuser(message);
                fermer();
                break;

            case MESSAGE:
                server.diffuser(message);
                break;

            default:
                break;
        }
    }

    /**
     * Envoie un message au client
     */
    public void envoyerMessage(Message message) {
        try {
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'envoi d'un message à " + nomUtilisateur, e);
            fermer();
        }
    }

    /**
     * Ferme la connexion avec le client
     */
    public void fermer() {
        if (!isRunning) return;

        isRunning = false;

        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (clientSocket != null && !clientSocket.isClosed()) clientSocket.close();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la fermeture de la connexion", e);
        } finally {
            server.supprimerClient(this);
        }
    }

    public String getNomUtilisateur() {
        return nomUtilisateur;
    }
}