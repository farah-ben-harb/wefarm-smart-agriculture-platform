package services;

import entities.Message;
import utils.DataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MessageService {
    private Connection connection;

    public MessageService() {
        // Initialisation de la connexion à la base de données
        connection = DataBase.getInstance().getConnection();
    }

    // Méthode pour envoyer un message
    public void sendMessage(Message message) throws SQLException {
        String sql = "INSERT INTO message (sender_id, receiver_id, conversation_id, content, created_at) " +
                "VALUES (?, ?, ?, ?, ?)";

        PreparedStatement pst = null;

        try {
            pst = connection.prepareStatement(sql);
            pst.setInt(1, message.getSenderId());
            pst.setInt(2, message.getReceiverId());
            pst.setInt(3, message.getConversationId());
            pst.setString(4, message.getContent());
            pst.setTimestamp(5, Timestamp.valueOf(message.getCreatedAt()));

            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace(); // Log l'exception pour le débogage
            throw e; // Relancer l'exception pour qu'elle soit gérée en amont
        } finally {
            if (pst != null) {
                pst.close(); // Fermeture du PreparedStatement
            }
        }
    }

    // Méthode pour récupérer tous les messages d'une conversation
    public List<Message> getMessagesByConversation(int conversationId) throws SQLException {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT * FROM message WHERE conversation_id = ? ORDER BY created_at ASC";
        PreparedStatement pst = null;
        ResultSet rs = null;

        try {
            pst = connection.prepareStatement(sql);
            pst.setInt(1, conversationId);
            rs = pst.executeQuery();

            while (rs.next()) {
                Message message = new Message();
                message.setId(rs.getInt("id"));
                message.setSenderId(rs.getInt("sender_id"));
                message.setReceiverId(rs.getInt("receiver_id"));
                message.setConversationId(rs.getInt("conversation_id"));
                message.setContent(rs.getString("content"));
                message.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());

                messages.add(message); // Ajout du message à la liste
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Log l'exception pour le débogage
            throw e; // Relancer l'exception pour qu'elle soit gérée en amont
        } finally {
            if (rs != null) {
                rs.close(); // Fermeture du ResultSet
            }
            if (pst != null) {
                pst.close(); // Fermeture du PreparedStatement
            }
        }

        return messages;
    }
}
