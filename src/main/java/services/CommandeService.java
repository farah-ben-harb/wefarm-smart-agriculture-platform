package services;

import entities.Commande;

import java.util.List;

public interface CommandeService {
    Commande createCommande(int produitId, int quantite, String modePaiement, String adresseLivraison);
    Commande createCommande(Commande commande);
    Commande getCommandeById(int id);
    List<Commande> getAllCommandes();
    List<Commande> getCommandesByProduitId(int produitId);
    void updateCommande(Commande commande);
    void deleteCommande(int id);
    List<Commande> getCommandesByClient(int clientId);
    List<Commande> getCommandesByStatut(String statut);
} 