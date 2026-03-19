package services;

import entities.Produit;

import java.util.List;

public interface ProduitService {
    Produit createProduit(Produit produit);
    Produit getProduitById(int id);
    List<Produit> getAllProduits();
    List<Produit> getProduitsByAgriculteur(int agriculteurId);
    Produit updateProduit(Produit produit);
    void deleteProduit(int id);
    List<Produit> getApprovedProduits();
    List<Produit> getProduitsByCategorie(String categorie);
    Produit approveProduit(int id, boolean approved);
} 