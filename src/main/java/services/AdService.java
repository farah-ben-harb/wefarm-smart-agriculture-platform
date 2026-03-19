package services;

import entities.Ad;
import utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdService {
    
    public void createAd(Ad ad) {
        String query = "INSERT INTO ad (image_url, start_date, end_date, is_active) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, ad.getImageUrl());
            pstmt.setTimestamp(2, new Timestamp(ad.getStartDate().getTime()));
            pstmt.setTimestamp(3, new Timestamp(ad.getEndDate().getTime()));
            pstmt.setBoolean(4, ad.isActive());
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error creating ad: " + e.getMessage());
        }
    }
    
    public List<Ad> getAllAds() {
        List<Ad> ads = new ArrayList<>();
        String query = "SELECT * FROM ad ORDER BY start_date DESC";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Ad ad = new Ad();
                ad.setId(rs.getInt("id"));
                ad.setImageUrl(rs.getString("image_url"));
                ad.setStartDate(rs.getTimestamp("start_date"));
                ad.setEndDate(rs.getTimestamp("end_date"));
                ad.setActive(rs.getBoolean("is_active"));
                ads.add(ad);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching ads: " + e.getMessage());
        }
        
        return ads;
    }
    
    public List<Ad> getActiveAds() {
        List<Ad> ads = new ArrayList<>();
        String query = "SELECT * FROM ad WHERE is_active = true " +
                      "AND start_date <= CURRENT_TIMESTAMP " +
                      "AND end_date >= CURRENT_TIMESTAMP " +
                      "ORDER BY start_date DESC";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Ad ad = new Ad();
                ad.setId(rs.getInt("id"));
                ad.setImageUrl(rs.getString("image_url"));
                ad.setStartDate(rs.getTimestamp("start_date"));
                ad.setEndDate(rs.getTimestamp("end_date"));
                ad.setActive(rs.getBoolean("is_active"));
                ads.add(ad);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching active ads: " + e.getMessage());
        }
        
        return ads;
    }
    
    public void updateAd(Ad ad) {
        String query = "UPDATE ad SET image_url = ?, start_date = ?, end_date = ?, is_active = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, ad.getImageUrl());
            pstmt.setTimestamp(2, new Timestamp(ad.getStartDate().getTime()));
            pstmt.setTimestamp(3, new Timestamp(ad.getEndDate().getTime()));
            pstmt.setBoolean(4, ad.isActive());
            pstmt.setInt(5, ad.getId());
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating ad: " + e.getMessage());
        }
    }
    
    public void deleteAd(int id) {
        String query = "DELETE FROM ad WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting ad: " + e.getMessage());
        }
    }
} 