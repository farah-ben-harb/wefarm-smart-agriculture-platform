package controllers.Event;

import entities.Evenement;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import services.EvenementService;

import java.io.IOException;
import java.sql.SQLException;

public class EvenementCardController {

    @FXML private Label titreLabel, dateLabel, localisationLabel;
    @FXML private Button supprimerButton, modifierButton;

    private Evenement evenement;
    private AfficherEvenementsController parentController;
    private final EvenementService service = new EvenementService();

    public void setEvenement(Evenement e, AfficherEvenementsController parent) {
        this.evenement = e;
        this.parentController = parent;

        titreLabel.setText(e.getTitre());
        dateLabel.setText(e.getDateDebut().toLocalDate().toString());
        localisationLabel.setText(e.getLocalisation());
    }

    @FXML
    private void handleSupprimer() throws SQLException {
        service.supprimer(evenement.getId());
    }

    @FXML
    private void handleModifier() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Evenement/modifierEvenement.fxml"));
            Parent root = loader.load();
            ModifierEvenementController controller = loader.getController();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
