package entities;

import java.util.Date;
import java.util.Objects;

public class Commande {
    private int id;
    private int produitId;
    private int clientId;
    private Date dateCommande;
    private int quantite;
    private String status;
    private String modePaiement;
    private String adresseLivraison;
    private double prixTotal;
    private Produit produit;

    // Default constructor
    public Commande() {
    }

    // Full constructor
    public Commande(int id, int produitId, int clientId, Date dateCommande, int quantite,
                   String status, String modePaiement, String adresseLivraison, double prixTotal) {
        this.id = id;
        this.produitId = produitId;
        this.clientId = clientId;
        this.dateCommande = dateCommande;
        this.quantite = quantite;
        this.status = status;
        this.modePaiement = modePaiement;
        this.adresseLivraison = adresseLivraison;
        this.prixTotal = prixTotal;
    }

    // Constructor without ID
    public Commande(int produitId, int clientId, Date dateCommande, int quantite,
                   String status, String modePaiement, String adresseLivraison, double prixTotal) {
        this.produitId = produitId;
        this.clientId = clientId;
        this.dateCommande = dateCommande;
        this.quantite = quantite;
        this.status = status;
        this.modePaiement = modePaiement;
        this.adresseLivraison = adresseLivraison;
        this.prixTotal = prixTotal;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProduitId() {
        return produitId;
    }

    public void setProduitId(int produitId) {
        this.produitId = produitId;
    }

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public Date getDateCommande() {
        return dateCommande;
    }

    public void setDateCommande(Date dateCommande) {
        this.dateCommande = dateCommande;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Alias methods for service compatibility
    public String getStatut() {
        return status;
    }

    public void setStatut(String statut) {
        this.status = statut;
    }

    public String getModePaiement() {
        return modePaiement;
    }

    public void setModePaiement(String modePaiement) {
        this.modePaiement = modePaiement;
    }

    public String getAdresseLivraison() {
        return adresseLivraison;
    }

    public void setAdresseLivraison(String adresseLivraison) {
        this.adresseLivraison = adresseLivraison;
    }

    public double getPrixTotal() {
        return prixTotal;
    }

    public void setPrixTotal(double prixTotal) {
        this.prixTotal = prixTotal;
    }

    public Produit getProduit() {
        return produit;
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
        if (produit != null) {
            this.produitId = produit.getId();
        }
    }

    @Override
    public String toString() {
        return "Commande{" +
                "id=" + id +
                ", produitId=" + produitId +
                ", clientId=" + clientId +
                ", dateCommande=" + dateCommande +
                ", quantite=" + quantite +
                ", status='" + status + '\'' +
                ", modePaiement='" + modePaiement + '\'' +
                ", adresseLivraison='" + adresseLivraison + '\'' +
                ", prixTotal=" + prixTotal +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Commande commande = (Commande) o;
        return id == commande.id &&
                produitId == commande.produitId &&
                clientId == commande.clientId &&
                quantite == commande.quantite &&
                Double.compare(commande.prixTotal, prixTotal) == 0 &&
                Objects.equals(dateCommande, commande.dateCommande) &&
                Objects.equals(status, commande.status) &&
                Objects.equals(modePaiement, commande.modePaiement) &&
                Objects.equals(adresseLivraison, commande.adresseLivraison);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, produitId, clientId, dateCommande, quantite, status, 
                          modePaiement, adresseLivraison, prixTotal);
    }
}

