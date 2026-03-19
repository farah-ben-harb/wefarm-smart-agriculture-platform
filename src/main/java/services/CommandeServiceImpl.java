package services;

import entities.Commande;
import entities.Produit;
import utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CommandeServiceImpl implements CommandeService {
    private final ProduitService produitService;
    private static final Pattern STATUS_PATTERN = Pattern.compile("^[a-zA-Z\\s-]{3,20}$");
    private static final String[] VALID_STATUSES = {"Pending", "Confirmed", "Shipped", "Delivered", "Cancelled"};

    public CommandeServiceImpl(ProduitService produitService) {
        if (produitService == null) {
            throw new IllegalArgumentException("ProduitService cannot be null");
        }
        this.produitService = produitService;
    }

    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    @Override
    public Commande createCommande(int produitId, int quantite, String modePaiement, String adresseLivraison) {
        Commande commande = new Commande();
        commande.setProduitId(produitId);
        commande.setQuantite(quantite);
        commande.setDateCommande(new Date());
        commande.setStatus("Pending");
        commande.setModePaiement(modePaiement);
        commande.setAdresseLivraison(adresseLivraison);
        return createCommande(commande);
    }

    @Override
    public Commande createCommande(Commande commande) {
        String validationError = validateCommande(commande);
        if (validationError != null) {
            throw new IllegalArgumentException(validationError);
        }

        // Set default client_id if not set
        if (commande.getClientId() == 0) {
            commande.setClientId(1); // Set default client ID
        }

        String sql = "INSERT INTO commande (produit_id, client_id, date_commande, quantite, status, mode_paiement, adresse_livraison, prix_total) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, commande.getProduitId());
            pstmt.setInt(2, commande.getClientId());
            pstmt.setTimestamp(3, new Timestamp(commande.getDateCommande().getTime()));
            pstmt.setInt(4, commande.getQuantite());
            pstmt.setString(5, commande.getStatus());
            pstmt.setString(6, commande.getModePaiement());
            pstmt.setString(7, commande.getAdresseLivraison());
            pstmt.setDouble(8, commande.getPrixTotal());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("La création de la commande a échoué");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    commande.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("La création de la commande a échoué, aucun ID obtenu");
                }
            }

            // Update product stock
            Produit produit = produitService.getProduitById(commande.getProduitId());
            if (produit != null) {
                produit.setQuantite(produit.getQuantite() - commande.getQuantite());
                produitService.updateProduit(produit);
            }

            return commande;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la création de la commande: " + e.getMessage(), e);
        }
    }

    @Override
    public Commande getCommandeById(int id) {
        String sql = "SELECT * FROM commande WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCommande(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération de la commande: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public List<Commande> getAllCommandes() {
        List<Commande> commandes = new ArrayList<>();
        String sql = "SELECT c.*, p.* FROM commande c LEFT JOIN produit p ON c.produit_id = p.id";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Commande commande = new Commande();
                commande.setId(rs.getInt("c.id"));
                commande.setProduitId(rs.getInt("c.produit_id"));
                commande.setClientId(rs.getInt("c.client_id"));
                commande.setDateCommande(new Date(rs.getTimestamp("c.date_commande").getTime()));
                commande.setQuantite(rs.getInt("c.quantite"));
                commande.setStatus(rs.getString("c.status"));
                commande.setModePaiement(rs.getString("c.mode_paiement"));
                commande.setAdresseLivraison(rs.getString("c.adresse_livraison"));
                commande.setPrixTotal(rs.getDouble("c.prix_total"));
                
                // Create and set the product directly from the join
                Produit produit = new Produit();
                produit.setId(rs.getInt("p.id"));
                produit.setNom(rs.getString("p.nom"));
                produit.setPrixTotal(rs.getDouble("p.prix_total"));
                produit.setCategorie(rs.getString("p.categorie"));
                commande.setProduit(produit);
                
                commandes.add(commande);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des commandes: " + e.getMessage(), e);
        }
        return commandes;
    }

    @Override
    public void updateCommande(Commande commande) {
        String validationError = validateCommande(commande);
        if (validationError != null) {
            throw new IllegalArgumentException(validationError);
        }

        String sql = "UPDATE commande SET produit_id = ?, date_commande = ?, quantite = ?, " +
                    "status = ?, mode_paiement = ?, adresse_livraison = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, commande.getProduitId());
            pstmt.setTimestamp(2, new Timestamp(commande.getDateCommande().getTime()));
            pstmt.setInt(3, commande.getQuantite());
            pstmt.setString(4, commande.getStatus());
            pstmt.setString(5, commande.getModePaiement());
            pstmt.setString(6, commande.getAdresseLivraison());
            pstmt.setInt(7, commande.getId());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("La mise à jour de la commande a échoué, aucune ligne affectée");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise à jour de la commande: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteCommande(int id) {
        String sql = "DELETE FROM commande WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("La suppression de la commande a échoué, aucune ligne affectée");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression de la commande: " + e.getMessage(), e);
        }
    }

    private Commande mapResultSetToCommande(ResultSet rs) throws SQLException {
        Commande commande = new Commande();
        commande.setId(rs.getInt("id"));
        commande.setProduitId(rs.getInt("produit_id"));
        commande.setClientId(rs.getInt("client_id"));
        commande.setDateCommande(new Date(rs.getTimestamp("date_commande").getTime()));
        commande.setQuantite(rs.getInt("quantite"));
        commande.setStatus(rs.getString("status"));
        commande.setModePaiement(rs.getString("mode_paiement"));
        commande.setAdresseLivraison(rs.getString("adresse_livraison"));
        commande.setPrixTotal(rs.getDouble("prix_total"));
        
        // Load the product information
        Produit produit = produitService.getProduitById(commande.getProduitId());
        commande.setProduit(produit);
        
        return commande;
    }

    private String validateCommande(Commande commande) {
        if (commande == null) {
            return "La commande ne peut pas être null";
        }

        if (commande.getProduitId() <= 0) {
            return "L'ID du produit doit être positif";
        }

        if (commande.getQuantite() <= 0) {
            return "La quantité doit être supérieure à 0";
        }

        if (commande.getStatus() == null || !isValidStatus(commande.getStatus())) {
            return "Le statut est invalide. Les statuts valides sont: " + String.join(", ", VALID_STATUSES);
        }

        if (commande.getModePaiement() == null || commande.getModePaiement().trim().isEmpty()) {
            return "Le mode de paiement est requis";
        }

        if (commande.getAdresseLivraison() == null || commande.getAdresseLivraison().trim().isEmpty()) {
            return "L'adresse de livraison est requise";
        }

        // Validate product existence and availability
        Produit produit = produitService.getProduitById(commande.getProduitId());
        if (produit == null) {
            return "Le produit n'existe pas";
        }

        if (!produit.isApproved()) {
            return "Le produit n'est pas approuvé";
        }

        if (produit.getQuantite() < commande.getQuantite()) {
            return "La quantité demandée n'est pas disponible";
        }

        return null; // No validation errors
    }

    private boolean isValidStatus(String status) {
        for (String validStatus : VALID_STATUSES) {
            if (validStatus.equalsIgnoreCase(status)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<Commande> getCommandesByProduitId(int produitId) {
        List<Commande> commandes = new ArrayList<>();
        String sql = "SELECT * FROM commande WHERE produit_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, produitId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    commandes.add(mapResultSetToCommande(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des commandes: " + e.getMessage(), e);
        }
        return commandes;
    }

    @Override
    public List<Commande> getCommandesByStatut(String statut) {
        List<Commande> commandes = new ArrayList<>();
        String sql = "SELECT * FROM commande WHERE status = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, statut);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    commandes.add(mapResultSetToCommande(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des commandes: " + e.getMessage(), e);
        }
        return commandes;
    }

    @Override
    public List<Commande> getCommandesByClient(int clientId) {
        if (clientId <= 0) {
            throw new IllegalArgumentException("L'ID du client doit être positif");
        }
        return getAllCommandes().stream()
                .filter(c -> c.getClientId() == clientId)
                .collect(Collectors.toList());
    }
} 