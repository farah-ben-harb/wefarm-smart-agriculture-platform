package controllers.User;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import services.UtilisateurService;
import entities.Utilisateur;

//import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import javafx.geometry.Pos;
import javafx.scene.input.MouseEvent;
import utils.SMSCompte;
import utils.SessionManager;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AdminController implements Initializable {
    @FXML
    private Button logoutButton;
    @FXML
    private HBox searchBarHBox;

    @FXML
    private TableView<Utilisateur> tableViewUsers;

    @FXML
    private TableColumn<Utilisateur, Integer> colId;

    @FXML
    private TableColumn<Utilisateur, String> colEmail;

    @FXML
    private TableColumn<Utilisateur, String> colRoles;

    @FXML
    private TableColumn<Utilisateur, String> colAdresse;

    @FXML
    private TableColumn<Utilisateur, String> colPassword;

    @FXML
    private TableColumn<Utilisateur, String> colFirstName;

    @FXML
    private TableColumn<Utilisateur, String> colLastName;

    @FXML
    private TableColumn<Utilisateur, String> colDateNaissance;

    @FXML
    private TableColumn<Utilisateur, String> colImage;

    @FXML
    private TableColumn<Utilisateur, String> colDateCreation;

    @FXML
    private TableColumn<Utilisateur, String> colStatus;

    @FXML
    private TableColumn<Utilisateur, Integer> colNumTel;
    @FXML
    private TableColumn<Utilisateur, Void> colAction;
    @FXML
    private TextField rechercheField;
    @FXML
    private ImageView activitiesIcon;
    @FXML
    private Button statsButton;
    //notification

    private static final int NOTIFICATION_WIDTH = 300;
    private static final int NOTIFICATION_HEIGHT = 60;
    private static final Duration NOTIFICATION_ANIMATION_DURATION = Duration.millis(300);
    private static final double NOTIFICATION_SPACING = 10;
    private final List<HBox> activeNotifications = new ArrayList<>();

    //tableview
    private UtilisateurService utilisateurService = new UtilisateurService();
    private ObservableList<Utilisateur> usersList = FXCollections.observableArrayList();

    //recherche
    private ObservableList<Utilisateur> filteredList = FXCollections.observableArrayList();
    private List<Utilisateur> allUsers = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        tableViewUsers.setStyle("-fx-background-color: white; "
                + "-fx-table-cell-border-color: transparent; "
                + "-fx-selection-bar: #e3e6f0; "
                + "-fx-selection-bar-non-focused: #f8f9fc;");

       // Supprimer les lignes vides et définir un placeholder
        tableViewUsers.setFixedCellSize(80); // Hauteur fixe pour chaque ligne


        // Initialiser les colonnes avec les propriétés des utilisateurs
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colAdresse.setCellValueFactory(new PropertyValueFactory<>("adresse"));
        colPassword.setCellValueFactory(new PropertyValueFactory<>("password"));
        colFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName")); // Correction
        colLastName.setCellValueFactory(new PropertyValueFactory<>("lastName")); // Correction
        colDateNaissance.setCellValueFactory(new PropertyValueFactory<>("dateNaissance")); // Correction
        colDateCreation.setCellValueFactory(new PropertyValueFactory<>("dateCreation")); // Correction
        colNumTel.setCellValueFactory(new PropertyValueFactory<>("numTel")); // Correction
        colRoles.setCellValueFactory(new PropertyValueFactory<>("roles"));

        colId.setVisible(false);
        colPassword.setVisible(false);
        colDateCreation.setVisible(false);
        colAdresse.setVisible(false);

        colRoles.setCellFactory(column -> new TableCell<Utilisateur, String>() {
            private final ObjectMapper objectMapper = new ObjectMapper();
            private final HBox container = new HBox(5);
            private final Map<String, ImageView> roleIcons = new HashMap<>();

            {
                roleIcons.put("ROLE_AGRICULTEUR", createRoleIcon("/img/agriculteur .png", "Agriculteur"));
                roleIcons.put("ROLE_FOURNISSEUR", createRoleIcon("/img/fournisseur.png", "Fournisseur"));
                roleIcons.put("ROLE_CLIENT", createRoleIcon("/img/client.png", "Client"));
                roleIcons.put("ROLE_EMPLOYE", createRoleIcon("/img/employe.png", "Employé"));
                roleIcons.put("ROLE_ADMIN", createRoleIcon("/img/admin.png", "Administrateur"));

                container.setAlignment(Pos.CENTER_LEFT);
            }

            private ImageView createRoleIcon(String iconPath, String tooltipText) {
                try {
                    ImageView icon = new ImageView(new Image(getClass().getResourceAsStream(iconPath)));
                    icon.setFitHeight(30);
                    icon.setFitWidth(30);
                    Tooltip.install(icon, new Tooltip(tooltipText));
                    return icon;
                } catch (Exception e) {
                    ImageView icon = new ImageView();
                    icon.setFitHeight(30);
                    icon.setFitWidth(30);
                    return icon;
                }
            }

            @Override
            protected void updateItem(String rolesJson, boolean empty) {
                super.updateItem(rolesJson, empty);

                if (empty || rolesJson == null || rolesJson.isEmpty()) {
                    setGraphic(null);
                    setText(null);
                } else {
                    try {
                        container.getChildren().clear();

                        // 1. Convertir le JSON en liste de rôles
                        List<String> rolesList = objectMapper.readValue(rolesJson, List.class);

                        // 2. Afficher les icônes et textes correspondants
                        rolesList.forEach(role -> {
                            String cleanedRole = role.trim().toUpperCase();
                            String roleName = convertRoleToReadable(role);

                            HBox roleContainer = new HBox(5);
                            roleContainer.setAlignment(Pos.CENTER_LEFT);

                            ImageView icon = roleIcons.getOrDefault(cleanedRole,
                                    createRoleIcon("/img/default-role.png", roleName));

                            Label nameLabel = new Label(roleName);
                            nameLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333;");

                            roleContainer.getChildren().addAll(icon, nameLabel);
                            container.getChildren().add(roleContainer);
                        });

                        setGraphic(container);
                        setText(null);

                    } catch (Exception e) {
                        System.err.println("Erreur de conversion des rôles: " + e.getMessage());
                        setGraphic(null);
                        setText(rolesJson);
                    }
                }
            }

            private String convertRoleToReadable(String technicalRole) {
                String cleanedRole = technicalRole.trim().toUpperCase();
                switch (cleanedRole) {
                    case "ROLE_AGRICULTEUR": return "Agriculteur";
                    case "ROLE_FOURNISSEUR": return "Fournisseur";
                    case "ROLE_CLIENT": return "Client";
                    case "ROLE_EMPLOYE": return "Employé";
                    case "ROLE_ADMIN": return "Administrateur";
                    default: return technicalRole;
                }
            }
        });

        colRoles.setStyle("-fx-alignment: CENTER-LEFT; -fx-padding: 5px;");
        colRoles.setPrefWidth(200);


        colStatus.setCellFactory(column -> new TableCell<Utilisateur, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);

                if (empty || status == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label statusLabel = new Label(status);
                    statusLabel.setTextFill(Color.WHITE);
                    statusLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");

                    StackPane container = new StackPane();
                    container.setPadding(new Insets(3, 12, 3, 12));

                    Color bgColor;
                    switch (status.toLowerCase()) {
                        case "actif":
                            bgColor = Color.web("#4CAF50");
                            break;
                        case "désactivé":
                            bgColor = Color.web("#F44336");
                            break;
                        case "en attente de validation":
                            bgColor = Color.web("#FF9800");
                            break;
                        default:
                            bgColor = Color.web("#9E9E9E");
                    }

                    Rectangle background = new Rectangle();
                    background.setArcHeight(8);
                    background.setArcWidth(8);
                    background.setFill(bgColor);
                    background.setStroke(Color.WHITE);
                    background.setStrokeWidth(0.8);

                    background.setEffect(new DropShadow(2, Color.color(0, 0, 0, 0.2)));

                    background.widthProperty().bind(statusLabel.widthProperty().add(24));
                    background.heightProperty().bind(statusLabel.heightProperty().add(8));

                    container.getChildren().addAll(background, statusLabel);

                    setGraphic(container);
                    setText(null);
                }
            }
        });
        colImage.setCellValueFactory(new PropertyValueFactory<>("image"));
        colImage.setCellFactory(param -> new TableCell<Utilisateur, String>() {
            private final ImageView imageView = new ImageView();

            @Override
            protected void updateItem(String imageUrl, boolean empty) {
                super.updateItem(imageUrl, empty);
                if (empty || imageUrl == null || imageUrl.isEmpty()) {
                    setGraphic(null);
                } else {
                    Task<Image> loadImageTask = new Task<>() {
                        @Override
                        protected Image call() throws Exception {
                            return new Image(imageUrl); // Charger l'image depuis l'URL
                        }
                    };

                    loadImageTask.setOnSucceeded(event -> {
                        Image image = loadImageTask.getValue();
                        ImageView imageView = new ImageView(image);

                        Circle clip = new Circle(25, 25, 25); // Rayon du cercle, ajustez la taille selon vos besoins
                        imageView.setClip(clip);

                        imageView.setPreserveRatio(false);
                        imageView.setFitHeight(50);
                        imageView.setFitWidth(50);

                        setGraphic(imageView);
                    });

                    loadImageTask.setOnFailed(event -> {
                        setGraphic(null);
                    });

                    new Thread(loadImageTask).start();
                }
            }
        });


        colAction.setCellFactory(param -> new TableCell<>() {
            private final HBox buttonsBox = new HBox(0);

            private final Button btnModifier = new Button("🖊");
            private final Button btnSupprimer = new Button("🗑");
            private final Button btnDetails = new Button("👁");
            private final Button btnActiverDesactiver = new Button();

            {
                // Style commun avec bordures ajustées
                String commonStyle = "-fx-font-size: 14px; -fx-background-color: white; " +
                        "-fx-border-radius: 0; -fx-background-radius: 0; " +
                        "-fx-padding: 5px; -fx-border-width: 1px;";

                // Style pour chaque bouton avec bordures spécifiques
                btnDetails.setStyle(commonStyle +
                        " -fx-text-fill: #4e73df; -fx-border-color: #4e73df;" +
                        " -fx-border-radius: 5px 0 0 5px;"); // Arrondi à gauche seulement

                btnModifier.setStyle(commonStyle +
                        " -fx-text-fill: #f6c23e; -fx-border-color: #f6c23e;" +
                        " -fx-border-radius: 0; -fx-border-left-width: 0;");

                btnSupprimer.setStyle(commonStyle +
                        " -fx-text-fill: #e74a3b; -fx-border-color: #e74a3b;" +
                        " -fx-border-radius: 0; -fx-border-left-width: 0;");

                btnActiverDesactiver.setStyle(commonStyle +
                        " -fx-border-radius: 0 5px 5px 0; " + // Arrondi à droite seulement
                        " -fx-border-left-width: 0;");

                // Actions
                btnModifier.setOnAction(event -> modifierUtilisateur(getTableView().getItems().get(getIndex())));
                btnSupprimer.setOnAction(event -> supprimerUtilisateur(getTableView().getItems().get(getIndex())));
                btnDetails.setOnAction(event -> afficherDetailsUtilisateur(getTableView().getItems().get(getIndex())));
                btnActiverDesactiver.setOnAction(event -> activerDesactiverCompte(getTableView().getItems().get(getIndex())));

                buttonsBox.getChildren().addAll(btnDetails, btnModifier, btnSupprimer, btnActiverDesactiver);
                buttonsBox.setAlignment(Pos.CENTER);

                // Effet de survol pour améliorer l'UX
                setupButtonHoverEffect(btnDetails, "#4e73df");
                setupButtonHoverEffect(btnModifier, "#f6c23e");
                setupButtonHoverEffect(btnSupprimer, "#e74a3b");
                setupButtonHoverEffect(btnActiverDesactiver, "#1cc88a");
            }

            private void setupButtonHoverEffect(Button button, String color) {
                button.setOnMouseEntered(e ->
                        button.setStyle(button.getStyle() + " -fx-background-color: " + color + "22;")
                );
                button.setOnMouseExited(e ->
                        button.setStyle(button.getStyle().replaceAll("-fx-background-color: " + color + "22;", ""))
                );
            }


            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    // Mise à jour du bouton Activer/Désactiver
                    Utilisateur utilisateur = getTableView().getItems().get(getIndex());
                    if ("actif".equalsIgnoreCase(utilisateur.getStatus())) {
                        btnActiverDesactiver.setText("❌");
                        btnActiverDesactiver.setStyle(btnActiverDesactiver.getStyle() +
                                " -fx-text-fill: #6c757d; -fx-border-color: #6c757d;");
                    } else {
                        btnActiverDesactiver.setText("✅");
                        btnActiverDesactiver.setStyle(btnActiverDesactiver.getStyle() +
                                " -fx-text-fill: #1cc88a; -fx-border-color: #1cc88a;");
                    }

                    setGraphic(buttonsBox);
                }
            }
        });



        // Charger les utilisateurs
        loadUsers();
        // Initialisation de la recherche
        setupSearchFunctionality();
    }

    private void loadUsers() {
        List<Utilisateur> users = utilisateurService.getAllUtilisateurs();

        if (users != null && !users.isEmpty()) {
            // Filtrer pour exclure l'email spécifique ET les admins
            List<Utilisateur> filteredUsers = users.stream()
                    .filter(user -> !"wefarmapplication@gmail.com".equalsIgnoreCase(user.getEmail()))
                    .collect(Collectors.toList());

            usersList.setAll(filteredUsers);
            allUsers = new ArrayList<>(filteredUsers);
            filteredList.setAll(filteredUsers);

            tableViewUsers.setItems(filteredList);
        }
    }

    private void modifierUtilisateur(Utilisateur user) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/views/user/EditUserForm.fxml"));
            AnchorPane page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Modifier Utilisateur");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(tableViewUsers.getScene().getWindow());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            EditUserController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setUser(user);

            dialogStage.showAndWait();

            if (controller.isSaveClicked()) {
                utilisateurService.modifierUtilisateur(user);
                showSuccessNotification("Utilisateur modifié avec succès");
                refreshTableView();
            }

        } catch (IOException e) {
            showErrorNotification("Erreur lors de l'ouverture de l'éditeur");
            e.printStackTrace();
        }
    }

    private void supprimerUtilisateur(Utilisateur utilisateur) {
        if (utilisateur == null) {
            showErrorNotification("🌿 Veuillez sélectionner un utilisateur !");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("🧑‍🌾 Suppression d’un utilisateur");
        alert.getDialogPane().setMinWidth(400);
        alert.getDialogPane().setMinHeight(100);

        ImageView customIcon = new ImageView(new Image(getClass().getResource("/delete.png").toExternalForm()));
        customIcon.setFitHeight(40);
        customIcon.setFitWidth(40);
        alert.setGraphic(customIcon);

        Label header = new Label("🟡 Attention !");
        header.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: orange;");

        Label content = new Label("Voulez-vous vraiment supprimer 👤 " +
                utilisateur.getFirstName() + " " + utilisateur.getLastName() + " ?\n\n" +
                "⚠️ Cette action est irréversible.");
        content.setStyle("-fx-font-size: 14px; -fx-text-fill: #5A3E1B;");

        VBox dialogContent = new VBox(10, header, content);
        dialogContent.setStyle("-fx-padding: 10px; -fx-background-color: #F4F1E9;");
        alert.getDialogPane().setContent(dialogContent);

        ButtonType buttonTypeOui = new ButtonType("✅ Oui, supprimer", ButtonBar.ButtonData.YES);
        ButtonType buttonTypeNon = new ButtonType("❌ Non", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(buttonTypeOui, buttonTypeNon);

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == buttonTypeOui) {
            try {
                utilisateurService.supprimerUtilisateur(utilisateur.getId());
                showSuccessNotification("✔️ Utilisateur supprimé avec succès !");
                refreshTableView();
            } catch (Exception e) {
                showErrorNotification("❗ Une erreur est survenue lors de la suppression.");
                e.printStackTrace();
            }
        }
    }


    private void afficherDetailsUtilisateur(Utilisateur utilisateur) {
        if (utilisateur != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/user/UserDetailsCard.fxml"));
                Parent root = loader.load();

                Stage popupStage = new Stage();
                popupStage.initModality(Modality.APPLICATION_MODAL);
                popupStage.setTitle("Détails de l'utilisateur");

                UserDetailsCardController controller = loader.getController();
                controller.setUtilisateur(utilisateur, popupStage);

                Scene scene = new Scene(root);
                popupStage.setScene(scene);

                popupStage.showAndWait();
                refreshTableView();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            afficherAlerte("Erreur", "Veuillez sélectionner un utilisateur.");
        }
    }

    private void activerDesactiverCompte(Utilisateur utilisateur) {
        if (utilisateur != null) {
            boolean activer = !utilisateur.getStatus().equalsIgnoreCase("actif");
            try {
                utilisateurService.activerDesactiverUtilisateur(utilisateur.getId(), activer);
                utilisateur.setStatus(activer ? "actif" : "inactif");

                String message = activer
                        ? "Compte activé avec succès et un SMS a été envoyé"
                        : "Compte désactivé avec succès";
                showSuccessNotification(message);

                // Envoi SMS si le compte est activé
                if (activer) {
                    SMSCompte smsCompte = new SMSCompte();
                    String numero = "216" + String.valueOf(utilisateur.getNumTel());
                    String sms = "Votre compte WeFarm a été activé. Bienvenue, " + utilisateur.getFirstName() + " !";
                    smsCompte.envoyerSMS(numero, sms);
                }

                refreshTableView();
            } catch (Exception e) {
                showErrorNotification("Erreur lors du changement de statut");
                e.printStackTrace();
            }
        } else {
            showErrorNotification("Veuillez sélectionner un utilisateur");
        }
    }



    public void refreshTableView() {
        List<Utilisateur> users = utilisateurService.getAllUtilisateurs();

        if (users != null) {
            List<Utilisateur> filteredUsers = users.stream()
                    .filter(user -> !"wefarmapplication@gmail.com".equalsIgnoreCase(user.getEmail()))
                    .collect(Collectors.toList());

            usersList.setAll(filteredUsers);
            allUsers = new ArrayList<>(filteredUsers);
            filteredList.setAll(filteredUsers);
            tableViewUsers.refresh();
        }
    }


    private void afficherAlerte(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    //recherche:
    private void setupSearchFunctionality() {
        // 1. Récupérer tous les utilisateurs une fois au début
        allUsers = utilisateurService.getAllUtilisateurs();

        // 2. Lier la liste filtrée à la TableView
        filteredList.setAll(allUsers);
        tableViewUsers.setItems(filteredList);

        // 3. Écouter les changements du champ de recherche
        rechercheField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterUsers(newValue);
        });
    }

    private void filterUsers(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            tableViewUsers.setItems(usersList); // Retour à la liste principale filtrée
            return;
        }

        String lowerCaseFilter = searchText.toLowerCase();

        List<Utilisateur> filtered = usersList.stream() // Utiliser usersList comme source
                .filter(user ->
                        user.getEmail().toLowerCase().contains(lowerCaseFilter) ||
                                user.getFirstName().toLowerCase().contains(lowerCaseFilter) ||
                                user.getLastName().toLowerCase().contains(lowerCaseFilter) ||
                                user.getAdresse().toLowerCase().contains(lowerCaseFilter) ||
                                String.valueOf(user.getNumTel()).contains(lowerCaseFilter))
                .collect(Collectors.toList());

        filteredList.setAll(filtered);
        tableViewUsers.setItems(filteredList);
    }

    @FXML
    public void handleSearchChange() {
        // Cette méthode est appelée à chaque frappe de touche
        filterUsers(rechercheField.getText());
    }

 //activities
 @FXML
 public void redirectToActivities(MouseEvent event) {
     try {
         Parent root = FXMLLoader.load(getClass().getResource("/views/user/activities.fxml"));

         Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

         double width = currentStage.getWidth();
         double height = currentStage.getHeight();

         Scene activitiesScene = new Scene(root, width, height);

         currentStage.setScene(activitiesScene);
         currentStage.show();

     } catch (IOException e) {
         e.printStackTrace();
     }
 }


    @FXML
    private void handleLogout() throws IOException {
        SessionManager.logout();
        showSuccessNotification("Déconnexion réussie");

        Stage currentStage = (Stage) logoutButton.getScene().getWindow();
        double width = currentStage.getWidth();
        double height = currentStage.getHeight();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/user/login.fxml"));
        Parent root = loader.load();

        Scene loginScene = new Scene(root, width, height);
        currentStage.setScene(loginScene);
        currentStage.show();
    }


    //add user
    @FXML
    public void handleAddButton(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/user/AddUserAdmin.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Ajouter un utilisateur");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(((Node) event.getSource()).getScene().getWindow());

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.showAndWait();

            showSuccessNotification("Utilisateur ajouté avec succès");
            refreshTableView();
        } catch (IOException e) {
            showErrorNotification("Erreur lors de l'ajout d'utilisateur");
            e.printStackTrace();
        }
    }

    //stats cnx
    @FXML
    private void handleStatsButton(ActionEvent event) {
        try {
            // Récupérer la scène actuelle à partir du bouton cliqué
            Node source = (Node) event.getSource();
            Stage currentStage = (Stage) source.getScene().getWindow();

            // Charger la nouvelle scène
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/user/StatsCnx.fxml"));
            Parent statsRoot = loader.load();

            // Remplacer la scène actuelle
            Scene statsScene = new Scene(statsRoot);
            currentStage.setScene(statsScene);
            currentStage.setTitle("Statistiques des utilisateurs");

        } catch (IOException e) {
            e.printStackTrace();
            afficherAlerte("Erreur", "Impossible d'afficher les statistiques");
        }
    }


    //notification
    private HBox createNotificationBox(String message, String color, String icon) {
        HBox notification = new HBox(10);
        notification.setAlignment(Pos.CENTER_LEFT);
        notification.setPrefSize(NOTIFICATION_WIDTH, NOTIFICATION_HEIGHT);
        notification.setStyle("-fx-background-color: " + color + "; "
                + "-fx-background-radius: 5; "
                + "-fx-padding: 10 15 10 15; "
                + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 3);");

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 18;");

        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14;");
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(NOTIFICATION_WIDTH - 50);

        notification.getChildren().addAll(iconLabel, messageLabel);
        return notification;
    }

    private void showAnimatedNotification(HBox notification) {
        Platform.runLater(() -> {
            try {
                Pane parent = (Pane) searchBarHBox.getParent();

                StackPane parentStack = new StackPane();
                int index = parent.getChildren().indexOf(searchBarHBox);

                parent.getChildren().remove(searchBarHBox);
                parentStack.getChildren().add(searchBarHBox);
                parent.getChildren().add(index, parentStack);

                notification.setMaxWidth(300);
                notification.setPrefWidth(300);
                StackPane.setAlignment(notification, Pos.TOP_RIGHT);
                StackPane.setMargin(notification, new Insets(10, 20, 0, 0));
                parentStack.getChildren().add(notification);

                notification.setOpacity(0);
                notification.setTranslateY(-50);

                ParallelTransition enterTransition = new ParallelTransition(
                        new FadeTransition(Duration.millis(300), notification),
                        new TranslateTransition(Duration.millis(300), notification)
                );
                ((FadeTransition) enterTransition.getChildren().get(0)).setFromValue(0);
                ((FadeTransition) enterTransition.getChildren().get(0)).setToValue(1);
                ((TranslateTransition) enterTransition.getChildren().get(1)).setFromY(-50);
                ((TranslateTransition) enterTransition.getChildren().get(1)).setToY(0);

                enterTransition.play();

                PauseTransition delay = new PauseTransition(Duration.seconds(3));
                delay.setOnFinished(e -> {
                    FadeTransition fadeOut = new FadeTransition(Duration.millis(300), notification);
                    fadeOut.setToValue(0);
                    fadeOut.setOnFinished(ev -> {
                        parentStack.getChildren().remove(notification);
                    });
                    fadeOut.play();
                });
                delay.play();

            } catch (Exception e) {
                System.err.println("Erreur d'affichage de notification: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }


    private void removeNotificationWithAnimation(HBox notification) {
        ParallelTransition exitTransition = new ParallelTransition();

        TranslateTransition slideOut = new TranslateTransition(NOTIFICATION_ANIMATION_DURATION, notification);
        slideOut.setToX(NOTIFICATION_WIDTH);
        slideOut.setInterpolator(Interpolator.EASE_IN);

        FadeTransition fadeOut = new FadeTransition(NOTIFICATION_ANIMATION_DURATION, notification);
        fadeOut.setToValue(0);

        exitTransition.getChildren().addAll(slideOut, fadeOut);
        exitTransition.setOnFinished(e -> {
            BorderPane root = (BorderPane) tableViewUsers.getScene().getRoot();
            root.getChildren().remove(notification);
            activeNotifications.remove(notification);
        });
        exitTransition.play();
    }

    private void showSuccessNotification(String message) {
        HBox notification = createNotificationBox(message, "#4CAF50", "✓");
        showAnimatedNotification(notification);

    }

    private void showErrorNotification(String message) {
        HBox notification = createNotificationBox(message, "#F44336", "✗");
        showAnimatedNotification(notification);
    }

}
