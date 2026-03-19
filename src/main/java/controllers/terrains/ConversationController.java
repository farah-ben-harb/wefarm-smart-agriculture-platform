package controllers.terrains;

import entities.Conversation;
import entities.Message;
import entities.Terrain;
import entities.Utilisateur;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import services.ConversationService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ConversationController {

    @FXML private ListView<Message> messagesList;
    @FXML private TextArea messageInput;
    @FXML private Button sendButton;

    private ConversationService conversationService;
    private Utilisateur client;
    private Utilisateur owner;
    private Conversation conversation;
    private ObservableList<Message> messages;
    private boolean cssLoaded = false;

    public void initialize() {
        messages = FXCollections.observableArrayList();
        messagesList.setItems(messages);

        // Load CSS
        messagesList.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null && !cssLoaded) {
                try {
                    newScene.getStylesheets().add(getClass().getResource("/css/messenger.css").toExternalForm());
                    cssLoaded = true;
                } catch (Exception e) {
                    System.err.println("Error loading CSS: " + e.getMessage());
                }
            }
        });

        // Customize message display
        messagesList.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Message message, boolean empty) {
                super.updateItem(message, empty);
                if (empty || message == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    // Create message bubble
                    HBox bubble = new HBox();
                    VBox contentBox = new VBox(2);
                    Text contentText = new Text(message.getContent());
                    Text timeText = new Text(formatDate(message.getCreatedAt()));
                    timeText.getStyleClass().add("message-time");

                    contentBox.getChildren().addAll(contentText, timeText);
                    bubble.getChildren().add(contentBox);

                    // Apply styles and alignment
                    if (message.getSenderId() == client.getId()) {
                        bubble.getStyleClass().add("sent-message");
                        contentBox.getStyleClass().add("sent-content");
                        bubble.setAlignment(Pos.CENTER_RIGHT); // Align sent messages to the right
                    } else {
                        bubble.getStyleClass().add("received-message");
                        contentBox.getStyleClass().add("received-content");
                        bubble.setAlignment(Pos.CENTER_LEFT); // Align received messages to the left
                    }

                    setGraphic(bubble);
                    setText(null);
                }
            }

            private String formatDate(LocalDateTime createdAt) {
                if (createdAt == null) return "Unknown";
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                    return createdAt.format(formatter);
                } catch (Exception e) {
                    System.err.println("Error formatting date: " + e.getMessage());
                    return "Error";
                }
            }
        });
    }

    public void setConversation(ConversationService service, Utilisateur client, Utilisateur owner, Terrain terrain) {
        try {
            this.conversationService = service;
            this.client = client;
            this.owner = owner;
            this.conversation = conversationService.getOrCreateConversation(client, terrain);
            loadMessages();
        } catch (Exception e) {
            System.err.println("Error initializing conversation: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Error initializing conversation: " + e.getMessage());
        }
    }

    private void loadMessages() {
        try {
            List<Message> conversationMessages = conversationService.getMessages(conversation);
            messages.setAll(conversationMessages);
            if (messages.isEmpty()) {
                System.out.println("No messages found for conversation (ID: " + conversation.getId() + ").");
            } else {
                System.out.println("Messages loaded successfully for conversation (ID: " + conversation.getId() + ").");
            }
            messagesList.scrollTo(messagesList.getItems().size() - 1);
            messagesList.getParent().requestLayout(); // Force refresh
        } catch (Exception e) {
            System.err.println("Error loadings messages: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Error loading messages: " + e.getMessage());
        }
    }

    @FXML
    private void handleSendMessage() {
        String content = messageInput.getText().trim();
        if (!content.isEmpty()) {
            try {
                conversationService.sendMessage(client, owner, conversation, content);
                messageInput.clear();
                loadMessages(); // Refresh message list
            } catch (Exception e) {
                System.err.println("Error sending message: " + e.getMessage());
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Error sending message: " + e.getMessage());
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