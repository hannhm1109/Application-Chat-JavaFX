package com.chatapp.client;

import com.chatapp.model.Message;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;

/**
 * Contrôleur de l'interface utilisateur du client
 */
public class ClientController implements Initializable {

    @FXML private TextField txtNom;
    @FXML private TextField txtServeur;
    @FXML private TextField txtPort;
    @FXML private Button btnConnecter;
    @FXML private Button btnDeconnecter;
    @FXML private ListView<String> lstMessages;
    @FXML private ListView<String> lstUtilisateurs;
    @FXML private TextArea txtMessage;
    @FXML private Button btnEnvoyer;
    @FXML private Label lblStatut;

    private Client client;
    private final ObservableList<String> messages = FXCollections.observableArrayList();
    private final ObservableList<String> utilisateurs = FXCollections.observableArrayList();
    private final BooleanProperty connecte = new SimpleBooleanProperty(false);

    /**
     * Initialise le contrôleur
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialiser les listes
        lstMessages.setItems(messages);
        lstUtilisateurs.setItems(utilisateurs);

        // Configurer l'état initial de l'interface
        btnDeconnecter.disableProperty().bind(connecte.not());
        btnEnvoyer.disableProperty().bind(connecte.not());
        txtMessage.disableProperty().bind(connecte.not());

        // Par défaut
        txtServeur.setText("localhost");
        txtPort.setText("9000");

        // Gestion de l'envoi par la touche Entrée
        txtMessage.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER && !event.isShiftDown()) {
                if (connecte.get()) {
                    envoyerMessage();
                    event.consume();
                }
            }
        });
    }

    /**
     * Gère la connexion au serveur
     */
    @FXML
    private void handleConnecter() {
        String nom = txtNom.getText().trim();
        String serveur = txtServeur.getText().trim();
        String portStr = txtPort.getText().trim();

        if (nom.isEmpty() || serveur.isEmpty() || portStr.isEmpty()) {
            ajouterMessage("Système", "Veuillez remplir tous les champs");
            return;
        }

        try {
            int port = Integer.parseInt(portStr);

            // Créer et connecter le client
            client = new Client(serveur, port, nom);

            // Ajouter les observateurs
            client.ajouterObservateurMessages(this::afficherMessage);
            client.ajouterObservateurUtilisateurs(this::mettreAJourUtilisateurs);
            client.ajouterObservateurConnexion(this::mettreAJourStatutConnexion);

            // Connecter
            boolean succes = client.connecter();

            if (succes) {
                // Désactiver les champs de connexion
                txtNom.setDisable(true);
                txtServeur.setDisable(true);
                txtPort.setDisable(true);
                btnConnecter.setDisable(true);

                connecte.set(true);
            } else {
                ajouterMessage("Système", "Erreur de connexion au serveur");
            }

        } catch (NumberFormatException e) {
            ajouterMessage("Système", "Le port doit être un nombre");
        }
    }

    /**
     * Gère la déconnexion du serveur
     */
    @FXML
    private void handleDeconnecter() {
        deconnecter();
    }

    /**
     * Déconnecte le client
     */
    public void deconnecter() {
        if (client != null && client.estConnecte()) {
            client.deconnecter();
        }
    }

    /**
     * Gère l'envoi d'un message
     */
    @FXML
    private void handleEnvoyer() {
        envoyerMessage();
    }

    /**
     * Envoie le message saisi
     */
    private void envoyerMessage() {
        String texte = txtMessage.getText().trim();
        if (!texte.isEmpty() && client != null && client.estConnecte()) {
            client.envoyerMessageTexte(texte);
            txtMessage.clear();
        }
    }

    /**
     * Affiche un nouveau message dans la liste
     */
    private void afficherMessage(Message message) {
        Platform.runLater(() -> {
            messages.add(message.toString());
            lstMessages.scrollTo(messages.size() - 1);
        });
    }

    /**
     * Ajoute un message système dans la liste
     */
    private void ajouterMessage(String auteur, String contenu) {
        Platform.runLater(() -> {
            messages.add(String.format("[%s] %s: %s",
                    java.time.LocalTime.now().format(
                            java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")),
                    auteur,
                    contenu));
            lstMessages.scrollTo(messages.size() - 1);
        });
    }

    /**
     * Met à jour la liste des utilisateurs connectés
     */
    private void mettreAJourUtilisateurs(List<String> listeUtilisateurs) {
        Platform.runLater(() -> {
            utilisateurs.clear();
            utilisateurs.addAll(listeUtilisateurs);
        });
    }

    /**
     * Met à jour l'affichage du statut de connexion
     */
    private void mettreAJourStatutConnexion(boolean estConnecte) {
        Platform.runLater(() -> {
            connecte.set(estConnecte);

            if (estConnecte) {
                lblStatut.setText("Connecté");
                lblStatut.setTextFill(Color.GREEN);
            } else {
                lblStatut.setText("Déconnecté");
                lblStatut.setTextFill(Color.RED);

                // Réactiver les champs de connexion
                txtNom.setDisable(false);
                txtServeur.setDisable(false);
                txtPort.setDisable(false);
                btnConnecter.setDisable(false);

                // Vider la liste des utilisateurs
                utilisateurs.clear();
            }
        });
    }

    /**
     * Vérifie si le client est connecté
     */
    public boolean estConnecte() {
        return client != null && client.estConnecte();
    }
}