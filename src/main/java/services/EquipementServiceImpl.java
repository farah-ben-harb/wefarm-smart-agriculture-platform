package services;

import entities.Equipement;
import utils.DataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class EquipementServiceImpl implements EquipementService {
    
    @Override
    public List<Equipement> getAllEquipements() {
        List<Equipement> equipementList = new ArrayList<>();
        try {
            Connection conn = DataBase.getInstance().getConnection();
            String query = "SELECT * FROM equipement";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                Equipement e = new Equipement(
                        rs.getInt("id"),
                        rs.getString("nom_equipement"),
                        rs.getInt("quantite_disponible"),
                        rs.getDouble("prix"),
                        rs.getString("image"),
                        rs.getString("category")
                );
                equipementList.add(e);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return equipementList;
    }

    @Override
    public Equipement getEquipementById(int id) {
        try {
            Connection conn = DataBase.getInstance().getConnection();
            String query = "SELECT * FROM equipement WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Equipement(
                        rs.getInt("id"),
                        rs.getString("nom_equipement"),
                        rs.getInt("quantite_disponible"),
                        rs.getDouble("prix"),
                        rs.getString("image"),
                        rs.getString("category")
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void addEquipement(Equipement e) {
        try {
            Connection conn = DataBase.getInstance().getConnection();
            String query = "INSERT INTO equipement (nom_equipement, quantite_disponible, prix, image, category) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, e.getNomEquipement());
            pstmt.setInt(2, e.getQuantiteDisponible());
            pstmt.setDouble(3, e.getPrix());
            pstmt.setString(4, e.getImage());
            pstmt.setString(5, e.getCategory());
            pstmt.executeUpdate();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void updateEquipement(Equipement e) {
        try {
            Connection conn = DataBase.getInstance().getConnection();
            String query = "UPDATE equipement SET nom_equipement = ?, quantite_disponible = ?, prix = ?, image = ?, category = ? WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, e.getNomEquipement());
            pstmt.setInt(2, e.getQuantiteDisponible());
            pstmt.setDouble(3, e.getPrix());
            pstmt.setString(4, e.getImage());
            pstmt.setString(5, e.getCategory());
            pstmt.setInt(6, e.getId());
            pstmt.executeUpdate();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void deleteEquipement(int id) throws Exception {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DataBase.getInstance().getConnection();
            
            // Vérifier d'abord si l'équipement existe
            String checkQuery = "SELECT * FROM equipement WHERE id = ?";
            pstmt = conn.prepareStatement(checkQuery);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();
            
            if (!rs.next()) {
                throw new Exception("Aucun équipement trouvé avec l'ID " + id);
            }
            
            // Vérifier si l'équipement est référencé dans la table achat
            pstmt.close();
            String checkAchatQuery = "SELECT COUNT(*) FROM achat WHERE equipement_id = ?";
            pstmt = conn.prepareStatement(checkAchatQuery);
            pstmt.setInt(1, id);
            rs.close();
            rs = pstmt.executeQuery();
            
            if (rs.next() && rs.getInt(1) > 0) {
                throw new Exception("Impossible de supprimer cet équipement car il est référencé dans des achats. Veuillez d'abord supprimer les achats associés.");
            }
            
            // L'équipement existe et n'est pas référencé, procéder à la suppression
            pstmt.close();
            String query = "DELETE FROM equipement WHERE id = ?";
            pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, id);
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected == 0) {
                throw new Exception("La suppression a échoué. Aucune ligne n'a été affectée.");
            }
            
            System.out.println("Suppression de l'équipement ID " + id + ": " + rowsAffected + " ligne(s) affectée(s)");
        } catch (SQLException e) {
            System.out.println("Erreur SQL lors de la suppression de l'équipement ID " + id + ": " + e.getMessage());
            
            // Vérifier si l'erreur est liée à une contrainte de clé étrangère
            if (e.getMessage() != null && (
                e.getMessage().toLowerCase().contains("foreign key constraint") ||
                e.getMessage().toLowerCase().contains("constraint violation") ||
                e.getMessage().toLowerCase().contains("integrity constraint"))) {
                throw new Exception("Impossible de supprimer cet équipement car il est référencé dans des achats. Veuillez d'abord supprimer les achats associés.");
            }
            
            throw e; // Propager l'exception SQL pour la gérer dans le contrôleur
        } catch (Exception e) {
            System.out.println("Erreur lors de la suppression de l'équipement ID " + id + ": " + e.getMessage());
            throw e; // Propager l'exception pour la gérer dans le contrôleur
        } finally {
            // Fermer les ressources
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                System.out.println("Erreur lors de la fermeture des ressources: " + e.getMessage());
            }
        }
    }
}