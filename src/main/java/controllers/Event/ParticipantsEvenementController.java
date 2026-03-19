package controllers.Event;

import entities.Evenement;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import services.ServiceReservation;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ParticipantsEvenementController implements Initializable {
    @FXML private TableView<ServiceReservation.ReservationWithDetails> reservationsTable;
    @FXML private TableColumn<ServiceReservation.ReservationWithDetails, String> nomCol;
    @FXML private TableColumn<ServiceReservation.ReservationWithDetails, String> prenomCol;
    @FXML private TableColumn<ServiceReservation.ReservationWithDetails, String> titreCol;
    @FXML private TableColumn<ServiceReservation.ReservationWithDetails, LocalDateTime> dateCol;
    @FXML private TableColumn<ServiceReservation.ReservationWithDetails, String> etatCol;
    @FXML
    private TableColumn<ServiceReservation.ReservationWithDetails, Void> actionsCol;

    private final ServiceReservation serviceReservation = new ServiceReservation();
    private Evenement evenement;

    public void setEvenement(Evenement evenement) {
        this.evenement = evenement;
        if (evenement != null) {
            loadReservations(); // Load only after evenement is set
        }
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        configureColumns();
    }

    private void configureColumns() {
        nomCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        prenomCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        titreCol.setCellValueFactory(new PropertyValueFactory<>("titreEvenement"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("dateReservation"));
        etatCol.setCellValueFactory(new PropertyValueFactory<>("etat"));

        dateCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(item));
                }
            }
        });

        actionsCol.setCellFactory(param -> new TableCell<>() {
            private final Button deleteBtn = new Button("Supprimer");

            {
                deleteBtn.setStyle("-fx-background-color: #e74a3b; -fx-text-fill: white;");
                deleteBtn.setOnAction(event -> {
                    ServiceReservation.ReservationWithDetails reservation = getTableView().getItems().get(getIndex());
                    try {
                        confirmAndDelete(reservation);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteBtn);
                }
            }
        });
    }

    void loadReservations() {
        try {
            if (evenement == null) {
                throw new IllegalStateException("Evenement is not set");
            }

            List<ServiceReservation.ReservationWithDetails> reservations =
                    serviceReservation.getReservationsForEvent(evenement.getId());
            reservationsTable.getItems().setAll(reservations);
        } catch (SQLException | IllegalStateException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Erreur lors du chargement des réservations: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void confirmAndDelete(ServiceReservation.ReservationWithDetails reservation) throws SQLException {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer cette réservation ?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (serviceReservation.deleteReservation(reservation.getId())) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Réservation supprimée avec succès");
                loadReservations();
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}