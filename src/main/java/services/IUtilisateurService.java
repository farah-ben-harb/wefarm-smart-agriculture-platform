package services;

import entities.Utilisateur;

import java.sql.SQLException;
import java.util.List;

public interface IUtilisateurService {
    void ajouterUtilisateur(Utilisateur user);
    void modifierUtilisateur(Utilisateur user);
    void supprimerUtilisateur(int id);
    Utilisateur getUtilisateurById(int id);
    List<Utilisateur> getAllUtilisateurs();
    Utilisateur getUtilisateurByEmail(String email);//pr login verification
    void activerDesactiverUtilisateur(int id, boolean activer);
    boolean updateUser(Utilisateur user);
    Utilisateur findByConfirmationToken(String token);
    Utilisateur findByEmail(String email) throws SQLException;
}
