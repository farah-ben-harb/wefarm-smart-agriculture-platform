package controllers.terrains;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class MessageController {

    @FXML
    private Label messageLabel;

    private String messageText;

    // Cette méthode permet d'initialiser le message à afficher
    public void setMessageText(String messageText) {
        this.messageText = messageText;
        messageLabel.setText(messageText); // Afficher le message dans la vue
    }
}
