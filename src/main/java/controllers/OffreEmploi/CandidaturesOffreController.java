package controllers.OffreEmploi;

import entities.OffreEmploi;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.util.Duration;
import services.ServiceCandidature;
import utils.Matching;
import utils.TwilioSMSUtil;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class CandidaturesOffreController  {
    @FXML private Label titreOffreLabel;
    @FXML private VBox candidaturesContainer;
    private final ServiceCandidature serviceCandidature = new ServiceCandidature();
    private final Matching matchingService = new Matching();

    public void setOffre(OffreEmploi offre) {
        // Création d'un TextFlow pour texte + image
        TextFlow flow = new TextFlow();

        // Texte du titre
        Text text = new Text(
                offre.getTitre().substring(0, 1).toUpperCase() +
                        offre.getTitre().substring(1) + " "
        );

        // Chargement de l'image
        ImageView userIcon = new ImageView(
                new Image(getClass().getResourceAsStream("/icons/candidats.png")));
        userIcon.setFitHeight(50);
        userIcon.setFitWidth(50);

        flow.getChildren().addAll(text, userIcon);
        titreOffreLabel.setGraphic(flow);
        titreOffreLabel.setText("");
    }

    public void setCandidaturesWithDetails(List<Map<String, Object>> candidaturesWithDetails) {
        // Vider le conteneur actuel
        candidaturesContainer.getChildren().clear();

        if (candidaturesWithDetails == null || candidaturesWithDetails.isEmpty()) {
            Label emptyLabel = new Label("Aucune candidature trouvée");
            emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #666666; -fx-font-style: italic; -fx-padding: 20px;");
            candidaturesContainer.getChildren().add(emptyLabel);
        } else {
            // Afficher les cartes de candidatures
            for (Map<String, Object> details : candidaturesWithDetails) {
                VBox card = createCandidatureCard(details);
                candidaturesContainer.getChildren().add(card);
            }
        }
    }

    private VBox createCandidatureCard(Map<String, Object> details) {
        VBox card = new VBox(10);
        card.getStyleClass().add("candidature-card");

        // Récupération des données
        Map<String, Object> user = (Map<String, Object>) details.get("user");
        Map<String, Object> offre = (Map<String, Object>) details.get("offre");
        int candidatureId = (int) details.get("candidature_id");
        String etat = (String) details.get("etat");

        // Calcul du pourcentage de matching (uniquement si état n'est pas "acceptée")
        double matchPercentage = 0.0;
        HBox matchingBox = new HBox(10);

        if (!"acceptée".equalsIgnoreCase(etat)) {
            String requiredSkills = (String) offre.get("competences_requises");
            String candidateSkills = (String) details.get("competences");
            if (requiredSkills != null && candidateSkills != null) {
                matchPercentage = matchingService.calculateMatchPercentage(requiredSkills, candidateSkills);
            }

            matchingBox.setAlignment(Pos.CENTER_LEFT);
            matchingBox.setStyle("-fx-padding: 5px;");

            ProgressBar progressBar = new ProgressBar(0);
            progressBar.setPrefWidth(150);
            progressBar.setPrefHeight(10);

            // Animation de remplissage de la barre
            Timeline timeline = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(progressBar.progressProperty(), 0)),
                    new KeyFrame(Duration.seconds(1.5),
                            new KeyValue(progressBar.progressProperty(), matchPercentage / 100.0))
            );
            timeline.play();

            if (matchPercentage >= 75) {
                progressBar.setStyle("-fx-accent: #2e7d32;"); // Vert
            } else if (matchPercentage >= 50) {
                progressBar.setStyle("-fx-accent: #ff8f00;"); // Orange
            } else {
                progressBar.setStyle("-fx-accent: #c62828;"); // Rouge
            }

            Label percentageLabel = new Label(String.format("%.0f%%", matchPercentage));
            percentageLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");

            Label correspondanceLabel = new Label("Correspondance:");
            correspondanceLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #333; -fx-font-size: 12px;");

            matchingBox.getChildren().addAll(
                    correspondanceLabel,
                    progressBar,
                    percentageLabel
            );
        }

        // Nom et prénom
        Label nomPrenom = new Label(user.get("prenom") + " " + user.get("nom"));
        nomPrenom.getStyleClass().add("nom-prenom");

        // Email
        Label email = new Label((String) "✉"+user.get("email"));
        email.getStyleClass().add("email");

        // État de la candidature avec icône
        HBox etatBox = new HBox(5);
        etatBox.setAlignment(Pos.CENTER_LEFT);

        Label etatIcon = new Label();
        etatIcon.getStyleClass().add("etat-icon");

        Label etatText = new Label(etat);
        etatText.getStyleClass().add("etat-text");

        // Appliquer le style selon l'état
        if ("acceptée".equalsIgnoreCase(etat)) {
            card.getStyleClass().add("candidature-acceptee");
            etatIcon.setText("✓");
            etatIcon.getStyleClass().add("etat-accepte");
        } else if ("refusée".equalsIgnoreCase(etat)) {
            card.getStyleClass().add("candidature-refusee");
            etatIcon.setText("✗");
            etatIcon.getStyleClass().add("etat-refuse");
        } else {
            etatIcon.setText("?");
            etatIcon.getStyleClass().add("etat-attente");
        }

        etatBox.getChildren().addAll(etatIcon, etatText);

        // Date de candidature
        LocalDateTime dateCandidature = (LocalDateTime) details.get("date_candidature");
        Label date = new Label("📅"+"Postulé le: " + formatDate(dateCandidature));
        date.getStyleClass().add("date");

        // Compétences avec titre
        VBox competencesBox = new VBox(5);
        Label competencesTitre = new Label("Compétences : ");
        competencesTitre.getStyleClass().add("competences-titre");

        // Compétences (tags)
        FlowPane competencesPane = new FlowPane(5, 5);
        competencesPane.getStyleClass().add("competences-pane");
        String[] competences = ((String) details.get("competences")).split(",");
        for (String competence : competences) {
            Label tag = new Label(competence.trim());
            tag.getStyleClass().add("competence-tag");
            competencesPane.getChildren().add(tag);
        }

        // Ajouter matchingBox seulement si état n'est pas "acceptée"
        if (!"acceptée".equalsIgnoreCase(etat)) {
            competencesBox.getChildren().addAll(competencesTitre, competencesPane, matchingBox);
        } else {
            competencesBox.getChildren().addAll(competencesTitre, competencesPane);
        }

        // Section boutons
        HBox buttonsContainer = new HBox(10);
        buttonsContainer.setAlignment(Pos.CENTER_RIGHT);
        buttonsContainer.setStyle("-fx-padding: 10 0 0 0;");

        if (!"acceptée".equalsIgnoreCase(etat)) {
            Button accepterBtn = new Button("Accepter");
            accepterBtn.getStyleClass().add("accept-button");
            accepterBtn.setOnAction(e -> handleAccept(candidatureId, card, etatIcon, etatText));

            Button refuserBtn = new Button("Refuser");
            refuserBtn.getStyleClass().add("reject-button");
            refuserBtn.setOnAction(e -> handleReject(candidatureId, card, etatIcon, etatText));

            buttonsContainer.getChildren().addAll(accepterBtn, refuserBtn);
        } else {
            Label acceptedLabel = new Label("✔ Acceptée");
            acceptedLabel.setStyle("-fx-text-fill: #038156;-fx-font-size: 14px;");
            buttonsContainer.getChildren().add(acceptedLabel);
        }

        card.getChildren().addAll(nomPrenom, email, etatBox, date, competencesBox, buttonsContainer);
        return card;
    }

    private void handleAccept(int candidatureId, VBox card, Label etatIcon, Label etatText) {
        try {
            // 1. Récupérer les détails de la candidature avant modification
            Map<String, Object> details = serviceCandidature.getCandidatureDetails(candidatureId);
            if (details == null) {
                System.err.println("Aucun détail trouvé pour la candidature ID: " + candidatureId);
                return;
            }

            Map<String, Object> offreDetails = (Map<String, Object>) details.get("offre");
            if (offreDetails == null || offreDetails.get("id") == null) {
                System.err.println("Aucune information d'offre trouvée pour la candidature ID: " + candidatureId);
                return;
            }

            int offreId = (int) offreDetails.get("id");

            // 2. Mettre à jour en base de données
            serviceCandidature.modifierEtatCandidature(candidatureId, "Acceptée");

            // 3. Décrémenter le nombre de postes et vérifier le statut de l'offre
            serviceCandidature.decrementerPostesOffre(offreId);

            // 4. Envoyer un SMS
            Map<String, Object> userDetails = (Map<String, Object>) details.get("user");
            String phoneNumber = "+216" + (String) userDetails.get("telephone");
            String offreTitre = (String) offreDetails.get("titre");

            String smsMessage = String.format(
                    "Votre candidature pour l'offre '%s' a été acceptée. Félicitations!",
                    offreTitre
            );

            TwilioSMSUtil.sendSMS(phoneNumber, smsMessage);

            card.getStyleClass().add("candidature-acceptee");

            etatIcon.setText("✓");
            etatIcon.getStyleClass().removeAll("etat-attente", "etat-refuse");
            etatIcon.getStyleClass().add("etat-accepte");
            etatText.setText("acceptée");

            HBox buttonsContainer = (HBox) card.getChildren().get(card.getChildren().size()-1);
            buttonsContainer.getChildren().clear();
            Label acceptedLabel = new Label("✔ Acceptée");
            acceptedLabel.setStyle("-fx-text-fill: #038156;-fx-font-size: 14px;");
            buttonsContainer.getChildren().add(acceptedLabel);
        } catch (SQLException e) {
            e.printStackTrace();
            // Gérer l'erreur
        }
    }
    private void handleReject(int candidatureId, VBox card, Label etatIcon, Label etatText) {
        try {
            // 1. Mettre à jour en base de données
            serviceCandidature.modifierEtatCandidature(candidatureId, "refusée");

            // 2. Envoyer un SMS
            Map<String, Object> details = serviceCandidature.getCandidatureDetails(candidatureId);
            String phoneNumber = "+216"+(String) ((Map<String, Object>) details.get("user")).get("telephone");
            String offreTitre = (String) ((Map<String, Object>) details.get("offre")).get("titre");

            String smsMessage = String.format(
                    "Votre candidature pour l'offre '%s' a été refusée. Merci pour votre intérêt.",
                    offreTitre
            );

            TwilioSMSUtil.sendSMS(phoneNumber, smsMessage);
            // 3. Mettre à jour le style de la carte
            card.getStyleClass().remove("candidature-acceptee");
            card.getStyleClass().add("candidature-refusee");

            // 4. Mettre à jour l'icône et le texte d'état
            etatIcon.setText("✗");
            etatIcon.getStyleClass().removeAll("etat-attente", "etat-accepte");
            etatIcon.getStyleClass().add("etat-refuse");
            etatText.setText("refusée");

            // 5. Désactiver le bouton "Refuser" et réactiver "Accepter"
            HBox buttonsContainer = (HBox) card.getChildren().get(card.getChildren().size()-1);
            Button refuserBtn = (Button) buttonsContainer.getChildren().get(1);
            Button accepterBtn = (Button) buttonsContainer.getChildren().get(0);
            refuserBtn.setDisable(true);
            accepterBtn.setDisable(false);

        } catch (SQLException e) {
            e.printStackTrace();
            // Gérer l'erreur
        }
    }
    /* ********************AMELIORATIONS****************************** */
    private String formatDate(LocalDateTime date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return date.format(formatter);
    }
    //Méthode pour le retour
    @FXML
    private void handleRetour(ActionEvent event) {
        try {
            // Charger la nouvelle vue
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/OffreEmploi/afficherOffres.fxml"));
            Parent root = loader.load();

            // Créer une nouvelle scène et fenêtre
            Stage newStage = new Stage();
            newStage.setScene(new Scene(root, 800, 600));
            newStage.setTitle("Liste des Offres");
            newStage.show();

            // Fermer la fenêtre actuelle
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}