package services;

import entities.Achat;
import utils.DataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

public class AchatServiceImpl implements AchatService {
    
    @Override
    public List<Achat> getAllAchats() {
        List<Achat> achatList = new ArrayList<>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DataBase.getInstance().getConnection();
            
            if (conn == null) {
                System.err.println("ERREUR: Impossible d'obtenir une connexion à la base de données");
                return achatList; // Return empty list
            }
            
            String query = "SELECT * FROM achat ORDER BY date DESC";
            stmt = conn.createStatement();
            rs = stmt.executeQuery(query);

            while (rs.next()) {
                Achat a = new Achat(
                        rs.getInt("id"),
                        rs.getInt("equipement_id"),
                        rs.getDate("date"),
                        rs.getInt("total")
                );
                achatList.add(a);
            }
        } catch (SQLException e) {
            System.err.println("Erreur SQL lors de la récupération des achats: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Erreur générale lors de la récupération des achats: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(rs, stmt, null);
        }
        
        return achatList;
    }

    @Override
    public Achat getAchatById(int id) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DataBase.getInstance().getConnection();
            
            if (conn == null) {
                System.err.println("ERREUR: Impossible d'obtenir une connexion à la base de données");
                return null;
            }
            
            String query = "SELECT * FROM achat WHERE id = ?";
            pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Achat(
                        rs.getInt("id"),
                        rs.getInt("equipement_id"),
                        rs.getDate("date"),
                        rs.getInt("total")
                );
            }
        } catch (SQLException e) {
            System.err.println("Erreur SQL lors de la récupération de l'achat ID " + id + ": " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Erreur générale lors de la récupération de l'achat ID " + id + ": " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(rs, pstmt, null);
        }
        
        return null;
    }

    @Override
    public void addAchat(Achat a) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = DataBase.getInstance().getConnection();
            
            if (conn == null) {
                System.err.println("ERREUR: Impossible d'obtenir une connexion à la base de données");
                return;
            }
            
            // Start transaction
            conn.setAutoCommit(false);
            
            // Verify that the referenced equipment exists
            String checkQuery = "SELECT id FROM equipement WHERE id = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setInt(1, a.getEquipementId());
                ResultSet checkRs = checkStmt.executeQuery();
                
                if (!checkRs.next()) {
                    System.err.println("ERREUR: L'équipement avec ID " + a.getEquipementId() + " n'existe pas");
                    conn.rollback();
                    return;
                }
            }
            
            // Check if there's already an achat with this equipment_id
            // We'll allow multiple achats with the same equipment_id
            // This is the key change to fix the foreign key constraint issue
            
            // Insert the achat
            String query = "INSERT INTO achat (equipement_id, date, total) VALUES (?, ?, ?)";
            pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, a.getEquipementId());
            pstmt.setDate(2, new java.sql.Date(a.getDate().getTime()));
            pstmt.setInt(3, a.getTotal());
            
            int result = pstmt.executeUpdate();
            
            if (result > 0) {
                // Commit if successful
                conn.commit();
                System.out.println("Achat ajouté avec succès");
            } else {
                // Rollback if no rows affected
                conn.rollback();
                System.err.println("Aucun achat n'a été ajouté");
            }
        } catch (SQLException ex) {
            // Rollback on error
            rollbackQuietly(conn);
            
            // Check if this is a foreign key constraint error
            if (ex.getMessage().contains("foreign key constraint")) {
                System.err.println("ERREUR: L'équipement référencé n'existe pas ou contrainte de clé étrangère violée");
                throw new RuntimeException("Erreur de contrainte de clé étrangère: " + ex.getMessage());
            } else {
                System.err.println("Erreur SQL lors de l'ajout de l'achat: " + ex.getMessage());
                throw new RuntimeException("Erreur SQL lors de l'ajout de l'achat: " + ex.getMessage());
            }
        } catch (Exception ex) {
            // Rollback on error
            rollbackQuietly(conn);
            
            System.err.println("Erreur générale lors de l'ajout de l'achat: " + ex.getMessage());
            throw new RuntimeException("Erreur générale lors de l'ajout de l'achat: " + ex.getMessage());
        } finally {
            // Reset auto-commit and close resources
            resetAutoCommitAndCloseResources(conn, pstmt);
        }
    }

    @Override
    public void updateAchat(Achat a) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = DataBase.getInstance().getConnection();
            
            if (conn == null) {
                System.err.println("ERREUR: Impossible d'obtenir une connexion à la base de données");
                return;
            }
            
            // Start transaction
            conn.setAutoCommit(false);
            
            // First check if the achat exists
            String checkAchatQuery = "SELECT id, equipement_id FROM achat WHERE id = ?";
            int currentEquipmentId = -1;
            try (PreparedStatement checkAchatStmt = conn.prepareStatement(checkAchatQuery)) {
                checkAchatStmt.setInt(1, a.getId());
                ResultSet achatRs = checkAchatStmt.executeQuery();
                
                if (!achatRs.next()) {
                    System.err.println("ERREUR: L'achat avec ID " + a.getId() + " n'existe pas");
                    conn.rollback();
                    return;
                }
                
                // Store the current equipment ID to check if it's changing
                currentEquipmentId = achatRs.getInt("equipement_id");
            }
            
            // Then check if the referenced equipment exists
            String checkEquipQuery = "SELECT id FROM equipement WHERE id = ?";
            try (PreparedStatement checkEquipStmt = conn.prepareStatement(checkEquipQuery)) {
                checkEquipStmt.setInt(1, a.getEquipementId());
                ResultSet equipRs = checkEquipStmt.executeQuery();
                
                if (!equipRs.next()) {
                    System.err.println("ERREUR: L'équipement avec ID " + a.getEquipementId() + " n'existe pas");
                    conn.rollback();
                    return;
                }
            }
            
            // Update the achat
            String query = "UPDATE achat SET equipement_id = ?, date = ?, total = ? WHERE id = ?";
            pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, a.getEquipementId());
            pstmt.setDate(2, new java.sql.Date(a.getDate().getTime()));
            pstmt.setInt(3, a.getTotal());
            pstmt.setInt(4, a.getId());
            
            int result = pstmt.executeUpdate();
            
            if (result > 0) {
                // Commit if successful
                conn.commit();
                System.out.println("Achat modifié avec succès");
            } else {
                // Rollback if no rows affected
                conn.rollback();
                System.err.println("Aucun achat n'a été modifié");
            }
        } catch (SQLException ex) {
            // Rollback on error
            rollbackQuietly(conn);
            
            // Check if this is a foreign key constraint error
            if (ex.getMessage().contains("foreign key constraint")) {
                System.err.println("ERREUR: L'équipement référencé n'existe pas ou contrainte de clé étrangère violée");
                throw new RuntimeException("Erreur de contrainte de clé étrangère: " + ex.getMessage());
            } else {
                System.err.println("Erreur SQL lors de la modification de l'achat: " + ex.getMessage());
                throw new RuntimeException("Erreur SQL lors de la modification de l'achat: " + ex.getMessage());
            }
        } catch (Exception ex) {
            // Rollback on error
            rollbackQuietly(conn);
            
            System.err.println("Erreur générale lors de la modification de l'achat: " + ex.getMessage());
            throw new RuntimeException("Erreur générale lors de la modification de l'achat: " + ex.getMessage());
        } finally {
            // Reset auto-commit and close resources
            resetAutoCommitAndCloseResources(conn, pstmt);
        }
    }

    @Override
    public void deleteAchat(int id) throws Exception {
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = DataBase.getInstance().getConnection();
            
            if (conn == null) {
                throw new Exception("Impossible d'obtenir une connexion à la base de données");
            }
            
            // Start transaction
            conn.setAutoCommit(false);
            
            // Check if achat exists
            String checkQuery = "SELECT id FROM achat WHERE id = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setInt(1, id);
                ResultSet rs = checkStmt.executeQuery();
                
                if (!rs.next()) {
                    conn.rollback();
                    throw new Exception("Achat avec ID " + id + " n'existe pas");
                }
            }
            
            // Delete the achat
            String query = "DELETE FROM achat WHERE id = ?";
            pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, id);
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                // Commit if successful
                conn.commit();
                System.out.println("Achat supprimé avec succès");
            } else {
                // Rollback if no rows affected
                conn.rollback();
                throw new Exception("Aucun achat n'a été supprimé");
            }
        } catch (SQLException e) {
            // Rollback on error
            rollbackQuietly(conn);
            
            System.err.println("Erreur SQL lors de la suppression de l'achat: " + e.getMessage());
            throw new Exception("Erreur lors de la suppression de l'achat: " + e.getMessage());
        } catch (Exception e) {
            // Rollback on error if it's not the original exception
            if (!(e.getMessage().equals("Achat avec ID " + id + " n'existe pas") || 
                  e.getMessage().equals("Aucun achat n'a été supprimé") ||
                  e.getMessage().equals("Impossible d'obtenir une connexion à la base de données"))) {
                rollbackQuietly(conn);
            }
            
            throw e; // Rethrow the exception
        } finally {
            // Reset auto-commit and close resources
            resetAutoCommitAndCloseResources(conn, pstmt);
        }
    }
    
    // Helper methods for resource management
    
    private void closeResources(ResultSet rs, Statement stmt, PreparedStatement pstmt) {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (pstmt != null) pstmt.close();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la fermeture des ressources: " + e.getMessage());
        }
    }
    
    private void rollbackQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Erreur lors du rollback: " + rollbackEx.getMessage());
            }
        }
    }
    
    private void resetAutoCommitAndCloseResources(Connection conn, PreparedStatement pstmt) {
        try {
            if (conn != null) {
                conn.setAutoCommit(true);
            }
            
            if (pstmt != null) {
                pstmt.close();
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la réinitialisation de l'autocommit ou fermeture de ressources: " + e.getMessage());
        }
    }
}