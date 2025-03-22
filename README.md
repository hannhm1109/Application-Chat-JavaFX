# Application de Chat JavaFX avec Sockets et Threads

## À propos
Ce projet implémente une application de chat en temps réel utilisant JavaFX pour l'interface graphique, les sockets pour la communication réseau, et les threads pour la gestion de la concurrence.

## Structure du Projet

### Packages Principaux
- **com.chatapp.client** : Composants du client de chat
- **com.chatapp.server** : Composants du serveur de chat
- **com.chatapp.model** : Modèles partagés entre client et serveur

### Couche Modèle
- **Message.java** : Classe pour les messages échangés entre clients et serveur
  - Différents types : MESSAGE, CONNEXION, DECONNEXION, LISTE_UTILISATEURS

### Couche Serveur
- **Server.java** : Gestion des connexions et diffusion des messages
- **ClientHandler.java** : Gestion individuelle de chaque client connecté
- **ServerApplication.java** : Point d'entrée de l'application serveur
- **ServerController.java** : Contrôleur pour l'interface utilisateur du serveur

### Couche Client
- **Client.java** : Connexion au serveur et gestion des messages
- **ClientApplication.java** : Point d'entrée de l'application client
- **ClientController.java** : Contrôleur pour l'interface utilisateur du client

### Ressources
- **client-view.fxml** : Interface utilisateur du client
- **server-view.fxml** : Interface utilisateur du serveur
- **style.css** : Styles CSS pour les interfaces

## Fonctionnalités Implémentées

### Côté Serveur
- Gestion multi-clients avec threads
- Journal des événements et messages
- Interface d'administration avec contrôles pour démarrer/arrêter
- Suivi en temps réel des clients connectés

### Côté Client
- Interface utilisateur intuitive
- Affichage des messages en temps réel
- Liste des utilisateurs connectés
- Envoi et réception de messages instantanés

## Technologies Utilisées
- Java 23
- JavaFX pour l'interface graphique
- Sockets Java pour la communication réseau
- Threads Java pour le multi-threading
- Maven pour la gestion des dépendances
- FXML pour la définition des interfaces

## Comment Utiliser l'Application

### Démarrer le Serveur
1. Exécuter la classe ServerApplication
2. Spécifier le port (par défaut: 9000)
3. Cliquer sur "Démarrer"

### Démarrer un Client
1. Exécuter la classe ClientApplication
2. Entrer un nom d'utilisateur
3. Configurer l'adresse du serveur et le port
4. Cliquer sur "Connecter"

## Captures d'Écran

https://github.com/user-attachments/assets/9a695ca3-3749-493a-adee-7e7798f9e72b

## Auteur
Hanane Nahim
