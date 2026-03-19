package controllers.terrains;

import entities.Conversation;
import entities.Message;
import entities.Utilisateur;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import services.ConversationService;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class AgriculteurMessagesController {

    @FXML
    private ListView<Conversation> conversationsListView;

    @FXML
    private Label headerLabel;

    @FXML
    private Label selectedConversationLabel;

    @FXML
    private ScrollPane messagesScrollPane;

    @FXML
    private VBox messagesContainer;

    @FXML
    private TextField messageInput;

    @FXML
    private Button sendButton;

    @FXML
    private Button refreshButton;

    private final ConversationService conversationService = new ConversationService();
    private Utilisateur currentUser; // L'agriculteur connecté
    private Conversation selectedConversation; // Conversation sélectionnée

    // Définir l'utilisateur connecté
    public void setCurrentUser(Utilisateur user) {
        this.currentUser = user;
        initialize();
    }

    @FXML
    public void initialize() {
        if (currentUser == null) {
            return; // Ne rien faire si l'utilisateur n'est pas défini
        }

        // Personnaliser l'en-tête avec le nom de l'utilisateur connecté
        String userName = (currentUser.getFirstName() != null && currentUser.getLastName() != null)
                ? currentUser.getFirstName() + " " + currentUser.getLastName()
                : "Utilisateur #" + currentUser.getId();
        headerLabel.setText("Messagerie de " + userName);

        // Charger le CSS
        messagesContainer.getStylesheets().add(getClass().getResource("/css/messenger.css").toExternalForm());

        // Configurer la ListView pour afficher les conversations avec bouton Supprimer
        conversationsListView.setCellFactory(param -> new ListCell<Conversation>() {
            private final Button deleteButton = new Button("✖");
            private final HBox content = new HBox(10);
            private final VBox textContent = new VBox(5);
            private final Label nameLabel = new Label();
            private final Label lastMessage = new Label();

            {
                deleteButton.getStyleClass().add("delete-button");
                nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
                lastMessage.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
                textContent.getChildren().addAll(nameLabel, lastMessage);
                content.getChildren().addAll(textContent, deleteButton);
                content.setAlignment(Pos.CENTER_LEFT);
                HBox.setHgrow(textContent, Priority.ALWAYS);
            }

            @Override
            protected void updateItem(Conversation conversation, boolean empty) {
                super.updateItem(conversation, empty);
                if (empty || conversation == null) {
                    setGraphic(null);
                } else {
                    nameLabel.setText(getCorrespondentName(conversation));
                    lastMessage.setText(getLastMessagePreview(conversation));
                    deleteButton.setOnAction(event -> handleDeleteConversation(conversation));
                    setGraphic(content);
                }
            }
        });

        // Charger les conversations
        loadConversations();

        // Écouter la sélection d'une conversation
        conversationsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedConversation = newSelection;
                selectedConversationLabel.setText("Conversation avec " + getCorrespondentName(newSelection));
                loadMessagesForConversation(newSelection);
            } else {
                selectedConversation = null;
                selectedConversationLabel.setText("Sélectionnez une conversation");
                messagesContainer.getChildren().clear();
            }
        });

        // Faire défiler automatiquement vers le bas
        messagesScrollPane.vvalueProperty().bind(messagesContainer.heightProperty());
    }

    private String getCorrespondentName(Conversation conversation) {
        int correspondentId = getCorrespondentId(conversation);
        if (correspondentId == -1) {
            return "Utilisateur inconnu";
        }
        Utilisateur correspondent = conversationService.getUserById(correspondentId);
        if (correspondent != null && correspondent.getFirstName() != null && correspondent.getLastName() != null) {
            return correspondent.getFirstName() + " " + correspondent.getLastName();
        }
        return "Utilisateur #" + correspondentId;
    }

    private int getCorrespondentId(Conversation conversation) {
        try {
            List<Message> messages = conversationService.getMessages(conversation);
            if (!messages.isEmpty()) {
                Message lastMessage = messages.get(messages.size() - 1);
                Integer receiverId = lastMessage.getReceiverId();
                if (lastMessage.getSenderId() == currentUser.getId()) {
                    return receiverId != null ? receiverId : -1;
                } else {
                    return lastMessage.getSenderId();
                }
            }
            return -1; // Aucune information disponible
        } catch (Exception e) {
            return -1;
        }
    }

    private String getLastMessagePreview(Conversation conversation) {
        try {
            List<Message> messages = conversationService.getMessages(conversation);
            if (!messages.isEmpty()) {
                String content = messages.get(messages.size() - 1).getContent();
                return content.length() > 30 ? content.substring(0, 30) + "..." : content;
            }
            return "Aucun message";
        } catch (Exception e) {
            return "Erreur";
        }
    }

    private void loadConversations() {
        try {
            List<Conversation> conversations = conversationService.getConversationsForUser(currentUser.getId());
            ObservableList<Conversation> conversationList = FXCollections.observableArrayList(conversations);
            conversationsListView.setItems(conversationList);
        } catch (RuntimeException e) {
            showError("Erreur lors du chargement des conversations: " + e.getMessage());
        }
    }

    private void loadMessagesForConversation(Conversation conversation) {
        messagesContainer.getChildren().clear();
        try {
            List<Message> messages = conversationService.getMessages(conversation);
            for (Message message : messages) {
                HBox messageBox = new HBox();
                Label messageLabel = new Label(message.getContent());
                messageLabel.getStyleClass().add(message.getSenderId() == currentUser.getId() ? "message-sent" : "message-received");
                messageLabel.setWrapText(true);

                // Ajouter l'heure du message
                Label timeLabel = new Label(message.getCreatedAt().format(DateTimeFormatter.ofPattern("HH:mm")));
                timeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #666;");

                VBox messageContent = new VBox(2, messageLabel, timeLabel);
                messageBox.getChildren().add(messageContent);

                // Aligner les messages
                if (message.getSenderId() == currentUser.getId()) {
                    messageBox.setAlignment(Pos.CENTER_RIGHT);
                    HBox.setMargin(messageContent, new Insets(0, 10, 0, 50));
                } else {
                    messageBox.setAlignment(Pos.CENTER_LEFT);
                    HBox.setMargin(messageContent, new Insets(0, 50, 0, 10));
                }

                messagesContainer.getChildren().add(messageBox);
            }
        } catch (RuntimeException e) {
            showError("Erreur lors du chargement des messages: " + e.getMessage());
        }
    }

    @FXML
    private void sendMessage() {
        if (selectedConversation == null) {
            showError("Veuillez sélectionner une conversation.");
            return;
        }
        String content = messageInput.getText().trim();
        if (content.isEmpty()) {
            showError("Le message ne peut pas être vide.");
            return;
        }

        try {
            int receiverId = getCorrespondentId(selectedConversation);
            if (receiverId == -1) {
                showError("Impossible d'identifier le destinataire.");
                return;
            }
            Utilisateur sender = currentUser;
            Utilisateur receiver = new Utilisateur();
            receiver.setId(receiverId);
            conversationService.sendMessage(sender, receiver, selectedConversation, content);
            messageInput.clear();
            loadMessagesForConversation(selectedConversation); // Rafraîchir les messages
        } catch (RuntimeException e) {
            showError("Erreur lors de l'envoi du message: " + e.getMessage());
        }
    }

    @FXML
    private void refreshMessages() {
        try {
            loadConversations();
            if (selectedConversation != null) {
                loadMessagesForConversation(selectedConversation);
            }
        } catch (RuntimeException e) {
            showError("Erreur lors du rafraîchissement: " + e.getMessage());
        }
    }

    private void handleDeleteConversation(Conversation conversation) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation de suppression");
        confirmAlert.setHeaderText("Supprimer la conversation");
        confirmAlert.setContentText("Voulez-vous vraiment supprimer cette conversation ? Cette action est irréversible.");
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                conversationService.deleteConversation(conversation.getId(), currentUser.getId());
                if (selectedConversation != null && selectedConversation.getId() == conversation.getId()) {
                    selectedConversation = null;
                    selectedConversationLabel.setText("Sélectionnez une conversation");
                    messagesContainer.getChildren().clear();
                }
                loadConversations(); // Rafraîchir la liste
            } catch (RuntimeException e) {
                showError("Erreur lors de la suppression de la conversation: " + e.getMessage());
            }
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}