package services;

import entities.Produit;
import utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ProduitServiceImpl implements ProduitService {
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9\\s\\-_àáâãäçèéêëìíîïñòóôõöùúûüýÿÀÁÂÃÄÇÈÉÊËÌÍÎÏÑÒÓÔÕÖÙÚÛÜÝ]{2,50}$");
    private static final Pattern DESCRIPTION_PATTERN = Pattern.compile("^[a-zA-Z0-9\\s\\-_.,!?()'\";:àáâãäçèéêëìíîïñòóôõöùúûüýÿÀÁÂÃÄÇÈÉÊËÌÍÎÏÑÒÓÔÕÖÙÚÛÜÝ]{5,500}$");
    private static final Pattern CATEGORY_PATTERN = Pattern.compile("^[a-zA-Z\\s\\-_àáâãäçèéêëìíîïñòóôõöùúûüýÿÀÁÂÃÄÇÈÉÊËÌÍÎÏÑÒÓÔÕÖÙÚÛÜÝ]{2,30}$");
    private static final Pattern STOCK_PATTERN = Pattern.compile("^[a-zA-Z0-9\\s\\-_]{2,20}$");

    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    private String validateProduit(Produit produit) {
        if (produit == null) {
            return "Le produit ne peut pas être null";
        }

        if (produit.getNom() == null || !NAME_PATTERN.matcher(produit.getNom()).matches()) {
            return "Le nom du produit doit contenir entre 2 et 50 caractères (lettres, chiffres, espaces, tirets, accents)";
        }

        if (produit.getDescription() == null || !DESCRIPTION_PATTERN.matcher(produit.getDescription()).matches()) {
            return "La description doit contenir entre 5 et 500 caractères";
        }

        if (produit.getCategorie() == null || !CATEGORY_PATTERN.matcher(produit.getCategorie()).matches()) {
            return "La catégorie doit contenir entre 2 et 30 caractères (lettres, espaces, tirets, accents)";
        }

        if (produit.getStockD() == null || !STOCK_PATTERN.matcher(produit.getStockD()).matches()) {
            return "Le stock doit contenir entre 2 et 20 caractères";
        }

        if (produit.getQuantite() < 0) {
            return "La quantité ne peut pas être négative";
        }

        if (produit.getPrixTotal() <= 0) {
            return "Le prix total doit être supérieur à 0";
        }

        return null; // No validation errors
    }

    @Override
    public Produit createProduit(Produit produit) {
        String validationError = validateProduit(produit);
        if (validationError != null) {
            throw new IllegalArgumentException(validationError);
        }

        String sql = "INSERT INTO produit (nom, quantite, prix_total, description, " +
                    "image, categorie, stock_d, approved, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW())";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, produit.getNom());
            pstmt.setInt(2, produit.getQuantite());
            pstmt.setDouble(3, produit.getPrixTotal());
            pstmt.setString(4, produit.getDescription());
            pstmt.setString(5, produit.getImage() != null ? produit.getImage() : "");
            pstmt.setString(6, produit.getCategorie());
            pstmt.setString(7, produit.getStockD());
            pstmt.setBoolean(8, false); // Default to not approved

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating product failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    produit.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating product failed, no ID obtained.");
                }
            }

            return produit;
        } catch (SQLException e) {
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
    }

    @Override
    public Produit getProduitById(int id) {
        String sql = "SELECT * FROM produit WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToProduit(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public List<Produit> getAllProduits() {
        List<Produit> produits = new ArrayList<>();
        String sql = "SELECT * FROM produit";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                produits.add(mapResultSetToProduit(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
        return produits;
    }

    @Override
    public Produit updateProduit(Produit produit) {
        String validationError = validateProduit(produit);
        if (validationError != null) {
            throw new IllegalArgumentException(validationError);
        }

        String sql = "UPDATE produit SET nom = ?, quantite = ?, " +
                    "prix_total = ?, description = ?, image = ?, categorie = ?, " +
                    "stock_d = ?, updated_at = NOW() WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, produit.getNom());
            pstmt.setInt(2, produit.getQuantite());
            pstmt.setDouble(3, produit.getPrixTotal());
            pstmt.setString(4, produit.getDescription());
            pstmt.setString(5, produit.getImage() != null ? produit.getImage() : "");
            pstmt.setString(6, produit.getCategorie());
            pstmt.setString(7, produit.getStockD());
            pstmt.setInt(8, produit.getId());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating product failed, no rows affected.");
            }

            return produit;
        } catch (SQLException e) {
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteProduit(int id) {
        String sql = "DELETE FROM produit WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Produit> getApprovedProduits() {
        List<Produit> produits = new ArrayList<>();
        String sql = "SELECT * FROM produit WHERE approved = true";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                produits.add(mapResultSetToProduit(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
        return produits;
    }

    @Override
    public List<Produit> getProduitsByCategorie(String categorie) {
        List<Produit> produits = new ArrayList<>();
        String sql = "SELECT * FROM produit WHERE categorie = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, categorie);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    produits.add(mapResultSetToProduit(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
        return produits;
    }

    @Override
    public List<Produit> getProduitsByAgriculteur(int agriculteurId) {
        // For now, return an empty list since agriculteur functionality is not implemented
        return new ArrayList<>();
        // TODO: Implement this method when agriculteur functionality is added
    }

    @Override
    public Produit approveProduit(int id, boolean approved) {
        String sql = "UPDATE produit SET approved = ?, updated_at = NOW() WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setBoolean(1, approved);
            pstmt.setInt(2, id);
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Approving product failed, no rows affected.");
            }
            
            return getProduitById(id);
        } catch (SQLException e) {
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
    }

    private Produit mapResultSetToProduit(ResultSet rs) throws SQLException {
        Produit produit = new Produit();
        produit.setId(rs.getInt("id"));
        produit.setNom(rs.getString("nom"));
        produit.setQuantite(rs.getInt("quantite"));
        produit.setPrixTotal(rs.getDouble("prix_total"));
        produit.setDescription(rs.getString("description"));
        produit.setImage(rs.getString("image"));
        produit.setCategorie(rs.getString("categorie"));
        produit.setStockD(rs.getString("stock_d"));
        produit.setApproved(rs.getBoolean("approved"));
        produit.setUpdatedAt(rs.getTimestamp("updated_at"));
        return produit;
    }
} 