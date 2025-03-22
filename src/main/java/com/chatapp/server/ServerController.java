package com.chatapp.server;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

/**
 * Contrôleur de l'interface utilisateur du serveur
 */
public class ServerController implements Initializable {

    @FXML private Button btnDemarrer;
    @FXML private Button btnArreter;
    @FXML private TextField txtPort;
    @FXML private ListView<String> lstJournal;
    @FXML private Label lblStatut;
    @FXML private Label lblConnections;

    private Server server;
    private boolean serverStarted = false;

    /**
     * Initialise le contrôleur
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        btnArreter.setDisable(true);
        txtPort.setText("9000");

        // Mise à jour périodique de l'interface
        Thread updateThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                    if (server != null && serverStarted) {
                        updateUI();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });

        updateThread.setDaemon(true);
        updateThread.start();
    }

    /**
     * Démarre le serveur
     */
    @FXML
    private void handleDemarrer() {
        try {
            int port = Integer.parseInt(txtPort.getText().trim());
            server = new Server(port);
            server.demarrer();

            serverStarted = true;
            btnDemarrer.setDisable(true);
            btnArreter.setDisable(false);
            txtPort.setDisable(true);
            lblStatut.setText("En cours d'exécution");

            updateUI();

        } catch (NumberFormatException e) {
            lstJournal.getItems().add("[ERREUR] Le port doit être un nombre valide");
        }
    }

    /**
     * Arrête le serveur
     */
    @FXML
    private void handleArreter() {
        if (server != null) {
            server.arreter();

            serverStarted = false;
            btnDemarrer.setDisable(false);
            btnArreter.setDisable(true);
            txtPort.setDisable(false);
            lblStatut.setText("Arrêté");
            lblConnections.setText("0");

            updateUI();
        }
    }

    /**
     * Met à jour l'interface utilisateur avec les informations du serveur
     */
    private void updateUI() {
        if (server == null) return;

        Platform.runLater(() -> {
            // Mettre à jour le nombre de connexions
            lblConnections.setText(String.valueOf(server.getNombreClients()));

            // Mettre à jour le journal
            lstJournal.getItems().clear();
            lstJournal.getItems().addAll(server.getJournalMessages());

            // Faire défiler vers le bas pour voir les messages les plus récents
            if (!lstJournal.getItems().isEmpty()) {
                lstJournal.scrollTo(lstJournal.getItems().size() - 1);
            }
        });
    }
}