package services;

import entities.Conversation;
import entities.Message;
import entities.Terrain;
import entities.Utilisateur;
import utils.DataBase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ConversationService {

    private final Connection connection;

    public ConversationService() {
        this.connection = DataBase.getInstance().getConnection();
    }

    /**
     * Récupère un utilisateur par son ID.
     */
    public Utilisateur getUserById(int userId) {
        String sql = "SELECT id, first_name, last_name FROM utilisateur WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Utilisateur user = new Utilisateur();
                    user.setId(rs.getInt("id"));
                    user.setFirstName(rs.getString("first_name"));
                    user.setLastName(rs.getString("last_name"));
                    return user;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de l'utilisateur: " + e.getMessage());
            throw new RuntimeException("Erreur lors de la récupération de l'utilisateur: " + e.getMessage(), e);
        }
        return null; // Aucun utilisateur trouvé
    }

    /**
     * Supprime une conversation et ses messages associés, après vérification que l'utilisateur est impliqué.
     */
    public void deleteConversation(int conversationId, int userId) {
        // Vérifier si l'utilisateur est impliqué dans la conversation
        String checkSql = "SELECT COUNT(*) FROM message WHERE conversation_id = ? AND (sender_id = ? OR receiver_id = ?)";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
            checkStmt.setInt(1, conversationId);
            checkStmt.setInt(2, userId);
            checkStmt.setInt(3, userId);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    throw new RuntimeException("Vous n'êtes pas autorisé à supprimer cette conversation.");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la vérification de la conversation: " + e.getMessage(), e);
        }

        // Supprimer les messages associés
        String deleteMessagesSql = "DELETE FROM message WHERE conversation_id = ?";
        try (PreparedStatement deleteMessagesStmt = connection.prepareStatement(deleteMessagesSql)) {
            deleteMessagesStmt.setInt(1, conversationId);
            deleteMessagesStmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression des messages: " + e.getMessage(), e);
        }

        // Supprimer la conversation
        String deleteConversationSql = "DELETE FROM conversation WHERE id = ?";
        try (PreparedStatement deleteConversationStmt = connection.prepareStatement(deleteConversationSql)) {
            deleteConversationStmt.setInt(1, conversationId);
            int rowsAffected = deleteConversationStmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new RuntimeException("Aucune conversation trouvée avec l'ID: " + conversationId);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression de la conversation: " + e.getMessage(), e);
        }
    }

    /**
     * Vérifie si une conversation existe déjà entre un client et un owner (lié à un terrain).
     * Retourne l'ID de la conversation existante, ou -1 si aucune conversation n'existe.
     */
    private int findExistingConversation(int clientId, int ownerId) {
        String sql = "SELECT DISTINCT conversation_id FROM message WHERE sender_id = ? AND receiver_id = ? " +
                "UNION " +
                "SELECT DISTINCT conversation_id FROM message WHERE sender_id = ? AND receiver_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, clientId);
            stmt.setInt(2, ownerId);
            stmt.setInt(3, ownerId);
            stmt.setInt(4, clientId);
            System.out.println("Recherche d'une conversation existante entre client " + clientId + " et owner " + ownerId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int conversationId = rs.getInt("conversation_id");
                    System.out.println("Conversation existante trouvée avec l'ID: " + conversationId);
                    return conversationId;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche d'une conversation existante: " + e.getMessage());
            throw new RuntimeException("Erreur lors de la recherche d'une conversation existante: " + e.getMessage(), e);
        }
        System.out.println("Aucune conversation existante trouvée entre client " + clientId + " et owner " + ownerId);
        return -1; // Aucune conversation trouvée
    }

    /**
     * Crée ou récupère une conversation existante entre un client et un terrain (lié à un owner).
     */
    public Conversation getOrCreateConversation(Utilisateur client, Terrain terrain) {
        int clientId = client.getId();
        int ownerId = terrain.getOwnerId();

        // Vérifier si une conversation existe déjà entre le client et l'owner
        int existingConversationId = findExistingConversation(clientId, ownerId);
        if (existingConversationId != -1) {
            Conversation existingConversation = new Conversation();
            existingConversation.setId(existingConversationId);
            System.out.println("Conversation existante chargée avec l'ID: " + existingConversationId);
            return existingConversation;
        }

        // Si aucune conversation n'existe, en créer une nouvelle
        Conversation conversation = new Conversation();
        String sql = "INSERT INTO conversation () VALUES ()"; // Syntaxe correcte pour MariaDB/MySQL
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            System.out.println("Exécution de la requête: " + sql);
            int rowsAffected = stmt.executeUpdate();
            System.out.println("Nombre de lignes affectées: " + rowsAffected);
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    conversation.setId(rs.getInt(1));
                    System.out.println("ID de la conversation créée: " + conversation.getId());
                } else {
                    throw new SQLException("Échec de la récupération de l'ID généré pour la conversation.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur SQL lors de la création de la conversation: " + e.getMessage());
            throw new RuntimeException("Erreur lors de la création de la conversation: " + e.getMessage(), e);
        }
        return conversation;
    }

    /**
     * Envoie un message dans une conversation.
     */
    public void sendMessage(Utilisateur sender, Utilisateur receiver, Conversation conversation, String content) {
        String sql = "INSERT INTO message (sender_id, receiver_id, conversation_id, content, created_at) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, sender.getId());
            if (receiver.getId() == 0) {
                stmt.setObject(2, null);
            } else {
                stmt.setInt(2, receiver.getId());
            }
            stmt.setInt(3, conversation.getId());
            stmt.setString(4, content);
            stmt.setObject(5, LocalDateTime.now());
            System.out.println("Exécution de la requête: " + sql);
            System.out.println("sender_id: " + sender.getId() + ", receiver_id: " + (receiver.getId() == 0 ? "NULL" : receiver.getId()) +
                    ", conversation_id: " + conversation.getId() + ", content: " + content + ", created_at: " + LocalDateTime.now());
            int rowsAffected = stmt.executeUpdate();
            System.out.println("Nombre de lignes affectées dans message: " + rowsAffected);
            if (rowsAffected == 0) {
                throw new SQLException("Échec de l'insertion du message, aucune ligne affectée.");
            }
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    System.out.println("ID du message créé: " + rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'envoi du message: " + e.getMessage());
            throw new RuntimeException("Erreur lors de l'envoi du message: " + e.getMessage(), e);
        }
    }

    /**
     * Récupère tous les messages d'une conversation.
     */
    public List<Message> getMessages(Conversation conversation) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT * FROM message WHERE conversation_id = ? ORDER BY created_at ASC";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, conversation.getId());
            System.out.println("Récupération des messages pour conversation_id: " + conversation.getId());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Message message = new Message();
                    message.setId(rs.getInt("id"));
                    message.setSenderId(rs.getInt("sender_id"));
                    message.setReceiverId(rs.getObject("receiver_id") != null ? rs.getInt("receiver_id") : null);
                    message.setConversationId(rs.getInt("conversation_id"));
                    message.setContent(rs.getString("content"));
                    message.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    messages.add(message);
                    System.out.println("Message chargé: " + message);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des messages: " + e.getMessage());
            throw new RuntimeException("Erreur lors de la récupération des messages: " + e.getMessage(), e);
        }
        System.out.println("Nombre total de messages chargés: " + messages.size());
        return messages;
    }

    /**
     * Récupère toutes les conversations où un utilisateur est impliqué (comme expéditeur ou destinataire).
     */
    public List<Conversation> getConversationsForUser(int userId) {
        List<Conversation> conversations = new ArrayList<>();
        String sql = "SELECT DISTINCT c.id FROM conversation c " +
                "JOIN message m ON c.id = m.conversation_id " +
                "WHERE m.receiver_id = ? OR m.sender_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            System.out.println("Récupération des conversations pour userId: " + userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Conversation conversation = new Conversation();
                    conversation.setId(rs.getInt("id"));
                    conversations.add(conversation);
                    System.out.println("Conversation trouvée: " + conversation.getId());
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des conversations: " + e.getMessage());
            throw new RuntimeException("Erreur lors de la récupération des conversations", e);
        }
        System.out.println("Nombre de conversations trouvées: " + conversations.size());
        return conversations;
    }

    /**
     * Récupère tous les messages reçus par un utilisateur (où il est le destinataire).
     */
    public List<Message> getReceivedMessagesForUser(int userId) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT * FROM message WHERE receiver_id = ? ORDER BY created_at ASC";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            System.out.println("Récupération des messages reçus pour userId: " + userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Message message = new Message();
                    message.setId(rs.getInt("id"));
                    message.setSenderId(rs.getInt("sender_id"));
                    message.setReceiverId(rs.getObject("receiver_id") != null ? rs.getInt("receiver_id") : null);
                    message.setConversationId(rs.getInt("conversation_id"));
                    message.setContent(rs.getString("content"));
                    message.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    messages.add(message);
                    System.out.println("Message reçu chargé: " + message);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des messages reçus: " + e.getMessage());
            throw new RuntimeException("Erreur lors de la récupération des messages reçus", e);
        }
        System.out.println("Nombre total de messages reçus chargés: " + messages.size());
        return messages;
    }
}