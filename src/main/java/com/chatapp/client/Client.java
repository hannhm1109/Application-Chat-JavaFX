package com.chatapp.client;

import com.chatapp.model.Message;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Classe principale du client de chat
 */
public class Client {
    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());

    private final String hostname;
    private final int port;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private final String nomUtilisateur;
    private boolean isConnected = false;
    private Thread lectureThread;

    private final List<Consumer<Message>> observateursMessages = new ArrayList<>();
    private final List<Consumer<List<String>>> observateursUtilisateurs = new ArrayList<>();
    private final List<Consumer<Boolean>> observateursConnexion = new ArrayList<>();

    public Client(String hostname, int port, String nomUtilisateur) {
        this.hostname = hostname;
        this.port = port;
        this.nomUtilisateur = nomUtilisateur;
    }

    /**
     * Établit la connexion au serveur
     */
    public boolean connecter() {
        try {
            socket = new Socket(hostname, port);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            isConnected = true;

            // Démarrer le thread de lecture
            lectureThread = new Thread(this::lireMessages);
            lectureThread.setDaemon(true);
            lectureThread.start();

            // Envoyer un message de connexion
            Message messageConnexion = new Message(
                    nomUtilisateur,
                    "vient de rejoindre le chat",
                    Message.TypeMessage.CONNEXION);
            envoyerMessage(messageConnexion);

            // Notifier les observateurs
            notifierChangementConnexion(true);

            return true;

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la connexion au serveur", e);
            return false;
        }
    }

    /**
     * Lit les messages entrants du serveur
     */
    private void lireMessages() {
        try {
            while (isConnected) {
                try {
                    Message message = (Message) in.readObject();
                    traiterMessage(message);
                } catch (SocketException e) {
                    if (isConnected) {
                        LOGGER.log(Level.INFO, "Connexion au serveur perdue");
                        deconnecter();
                    }
                    break;
                } catch (ClassNotFoundException e) {
                    LOGGER.log(Level.WARNING, "Objet inconnu reçu", e);
                }
            }
        } catch (IOException e) {
            if (isConnected) {
                LOGGER.log(Level.SEVERE, "Erreur lors de la lecture des messages", e);
                deconnecter();
            }
        }
    }

    /**
     * Traite un message reçu selon son type
     */
    private void traiterMessage(Message message) {
        switch (message.getType()) {
            case LISTE_UTILISATEURS:
                // Traiter la liste des utilisateurs
                String[] noms = message.getContenu().split(",");
                List<String> listeUtilisateurs = new ArrayList<>();
                for (String nom : noms) {
                    if (!nom.isEmpty()) {
                        listeUtilisateurs.add(nom);
                    }
                }
                notifierChangementUtilisateurs(listeUtilisateurs);
                break;

            default:
                // Pour les messages normaux, notifier les observateurs
                notifierNouveauMessage(message);
                break;
        }
    }

    /**
     * Envoie un message au serveur
     */
    public void envoyerMessage(Message message) {
        if (!isConnected) return;

        try {
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'envoi d'un message", e);
        }
    }

    /**
     * Envoie un message texte au serveur
     */
    public void envoyerMessageTexte(String texte) {
        Message message = new Message(nomUtilisateur, texte, Message.TypeMessage.MESSAGE);
        envoyerMessage(message);
    }

    /**
     * Déconnecte le client du serveur
     */
    public void deconnecter() {
        if (!isConnected) return;

        try {
            // Envoyer un message de déconnexion
            Message messageDeconnexion = new Message(
                    nomUtilisateur,
                    "a quitté le chat",
                    Message.TypeMessage.DECONNEXION);
            envoyerMessage(messageDeconnexion);

            isConnected = false;

            // Fermer les flux et le socket
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();

            // Notifier les observateurs
            notifierChangementConnexion(false);

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la déconnexion", e);
        }
    }

    /**
     * Vérifie si le client est connecté
     */
    public boolean estConnecte() {
        return isConnected;
    }

    /**
     * Ajoute un observateur pour les nouveaux messages
     */
    public void ajouterObservateurMessages(Consumer<Message> observateur) {
        observateursMessages.add(observateur);
    }

    /**
     * Ajoute un observateur pour les changements de la liste des utilisateurs
     */
    public void ajouterObservateurUtilisateurs(Consumer<List<String>> observateur) {
        observateursUtilisateurs.add(observateur);
    }

    /**
     * Ajoute un observateur pour les changements d'état de connexion
     */
    public void ajouterObservateurConnexion(Consumer<Boolean> observateur) {
        observateursConnexion.add(observateur);
    }

    /**
     * Notifie les observateurs d'un nouveau message
     */
    private void notifierNouveauMessage(Message message) {
        for (Consumer<Message> observateur : observateursMessages) {
            observateur.accept(message);
        }
    }

    /**
     * Notifie les observateurs d'un changement dans la liste des utilisateurs
     */
    private void notifierChangementUtilisateurs(List<String> utilisateurs) {
        for (Consumer<List<String>> observateur : observateursUtilisateurs) {
            observateur.accept(utilisateurs);
        }
    }

    /**
     * Notifie les observateurs d'un changement d'état de connexion
     */
    private void notifierChangementConnexion(boolean estConnecte) {
        for (Consumer<Boolean> observateur : observateursConnexion) {
            observateur.accept(estConnecte);
        }
    }

    public String getNomUtilisateur() {
        return nomUtilisateur;
    }
}