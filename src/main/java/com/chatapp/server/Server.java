package com.chatapp.server;

import com.chatapp.model.Message;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Classe principale du serveur de chat
 */
public class Server {
    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    private final int port;
    private ServerSocket serverSocket;
    private final List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private final List<String> journalMessages = new ArrayList<>();
    private boolean isRunning = false;

    public Server(int port) {
        this.port = port;
    }

    /**
     * Démarre le serveur sur le port spécifié
     */
    public void demarrer() {
        try {
            serverSocket = new ServerSocket(port);
            isRunning = true;
            LOGGER.log(Level.INFO, "Serveur démarré sur le port {0}", port);
            ajouterAuJournal("Serveur démarré sur le port " + port);

            // Thread d'acceptation des connexions
            new Thread(this::accepterConnexions).start();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du démarrage du serveur", e);
            ajouterAuJournal("Erreur lors du démarrage du serveur: " + e.getMessage());
        }
    }

    /**
     * Accepte les connexions entrantes et crée un handler pour chaque client
     */
    private void accepterConnexions() {
        while (isRunning) {
            try {
                Socket clientSocket = serverSocket.accept();
                LOGGER.log(Level.INFO, "Nouvelle connexion acceptée: {0}", clientSocket.getInetAddress());

                ClientHandler clientHandler = new ClientHandler(this, clientSocket);
                clients.add(clientHandler);
                new Thread(clientHandler).start();

            } catch (IOException e) {
                if (isRunning) {
                    LOGGER.log(Level.SEVERE, "Erreur lors de l'acceptation d'une connexion", e);
                    ajouterAuJournal("Erreur lors de l'acceptation d'une connexion: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Diffuse un message à tous les clients connectés
     */
    public void diffuser(Message message) {
        ajouterAuJournal(message.toString());
        for (ClientHandler client : clients) {
            client.envoyerMessage(message);
        }
    }

    /**
     * Supprime un client de la liste des clients connectés
     */
    public void supprimerClient(ClientHandler client) {
        clients.remove(client);
        ajouterAuJournal("Client déconnecté: " + client.getNomUtilisateur());

        // Envoyer la liste mise à jour des utilisateurs
        envoyerListeUtilisateurs();
    }

    /**
     * Envoie la liste des utilisateurs connectés à tous les clients
     */
    public void envoyerListeUtilisateurs() {
        StringBuilder listeUtilisateurs = new StringBuilder();
        for (ClientHandler client : clients) {
            if (client.getNomUtilisateur() != null) {
                listeUtilisateurs.append(client.getNomUtilisateur()).append(",");
            }
        }

        // Supprimer la dernière virgule si elle existe
        if (listeUtilisateurs.length() > 0) {
            listeUtilisateurs.deleteCharAt(listeUtilisateurs.length() - 1);
        }

        Message message = new Message("Serveur", listeUtilisateurs.toString(),
                Message.TypeMessage.LISTE_UTILISATEURS);

        for (ClientHandler client : clients) {
            client.envoyerMessage(message);
        }
    }

    /**
     * Arrête le serveur
     */
    public void arreter() {
        isRunning = false;
        LOGGER.info("Arrêt du serveur...");
        ajouterAuJournal("Arrêt du serveur...");

        // Fermer toutes les connexions client
        for (ClientHandler client : clients) {
            client.fermer();
        }

        clients.clear();

        // Fermer le socket serveur
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la fermeture du serveur", e);
        }
    }

    /**
     * Ajoute un message au journal du serveur
     */
    public void ajouterAuJournal(String message) {
        journalMessages.add("[" + java.time.LocalTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")) + "] " + message);
    }

    /**
     * Retourne la liste des messages du journal
     */
    public List<String> getJournalMessages() {
        return new ArrayList<>(journalMessages);
    }

    /**
     * Retourne le nombre de clients connectés
     */
    public int getNombreClients() {
        return clients.size();
    }
}