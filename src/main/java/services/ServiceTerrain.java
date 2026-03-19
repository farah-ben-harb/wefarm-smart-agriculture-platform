package services;

import entities.Terrain;
import utils.DataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceTerrain implements IService<Terrain> {
    private Connection connection;

    public ServiceTerrain() {
        this.connection = DataBase.getInstance().getConnection();
    }

    @Override
    public void ajouter(Terrain terrain) throws SQLException {
        String req = "INSERT INTO terrain (own_id, adresse, surface, prix, status, description, latitude, longitude, type_sol, photos) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setObject(1, terrain.getOwnerId(), Types.INTEGER);
            ps.setString(2, terrain.getAdresse());
            ps.setDouble(3, terrain.getSurface());
            ps.setDouble(4, terrain.getPrix());
            ps.setString(5, terrain.getStatus());
            ps.setString(6, terrain.getDescription());
            ps.setDouble(7, terrain.getLatitude());
            ps.setDouble(8, terrain.getLongitude());
            ps.setString(9, terrain.getTypeSol());
            ps.setString(10, terrain.getPhotos());

            ps.executeUpdate();
            System.out.println("Terrain ajouté avec succès");
        }
    }

    @Override
    public void modifier(Terrain terrain) throws SQLException {
        String req = "UPDATE terrain SET own_id = ?, adresse = ?, surface = ?, prix = ?, status = ?, " +
                "description = ?, latitude = ?, longitude = ?, type_sol = ?, photos = ? WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setObject(1, terrain.getOwnerId(), Types.INTEGER);
            ps.setString(2, terrain.getAdresse());
            ps.setDouble(3, terrain.getSurface());
            ps.setDouble(4, terrain.getPrix());
            ps.setString(5, terrain.getStatus());
            ps.setString(6, terrain.getDescription());
            ps.setDouble(7, terrain.getLatitude());
            ps.setDouble(8, terrain.getLongitude());
            ps.setString(9, terrain.getTypeSol());
            ps.setString(10, terrain.getPhotos());
            ps.setInt(11, terrain.getId());

            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Terrain modifié avec succès");
            }
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM terrain WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setInt(1, id);

            int rowsDeleted = ps.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("Terrain supprimé avec succès");
            } else {
                System.out.println("Aucun terrain trouvé avec l'ID: " + id);
            }
        }
    }

    @Override
    public List<Terrain> afficher() throws SQLException {
        List<Terrain> terrains = new ArrayList<>();
        String req = "SELECT * FROM terrain";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(req)) {

            while (rs.next()) {
                Terrain t = new Terrain(
                        rs.getInt("id"),
                        rs.getObject("own_id", Integer.class),
                        rs.getString("adresse"),
                        rs.getDouble("surface"),
                        rs.getDouble("prix"),
                        rs.getString("status"),
                        rs.getString("description"),
                        rs.getDouble("latitude"),
                        rs.getDouble("longitude"),
                        rs.getString("type_sol"),
                        rs.getString("photos")
                );
                terrains.add(t);
            }
        }
        return terrains;
    }

    public List<Terrain> getTerrainsByAgriculteur(int idAgriculteur) throws SQLException {
        List<Terrain> terrains = new ArrayList<>();
        String req = "SELECT * FROM terrain WHERE own_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setInt(1, idAgriculteur);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Terrain t = new Terrain(
                            rs.getInt("id"),
                            rs.getObject("own_id", Integer.class),
                            rs.getString("adresse"),
                            rs.getDouble("surface"),
                            rs.getDouble("prix"),
                            rs.getString("status"),
                            rs.getString("description"),
                            rs.getDouble("latitude"),
                            rs.getDouble("longitude"),
                            rs.getString("type_sol"),
                            rs.getString("photos")
                    );
                    terrains.add(t);
                }
            }
        }
        return terrains;
    }

}