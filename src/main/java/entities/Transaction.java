package entities;

import java.util.Date;
import java.util.Objects;

public class Transaction {
    private int id;
    private int clientId;
    private int agriculteurId;
    private int terrainId;
    private String type;
    private Date dateTransaction;
    private double montant;

    // Constructeur sans paramètre
    public Transaction() {
    }

    // Constructeur avec tous les paramètres sauf l'ID (car il est auto-incrémenté)
    public Transaction(int clientId, int agriculteurId, int terrainId, String type, Date dateTransaction, double montant) {
        this.clientId = clientId;
        this.agriculteurId = agriculteurId;
        this.terrainId = terrainId;
        this.type = type;
        this.dateTransaction = dateTransaction;
        this.montant = montant;
    }

    // Constructeur avec tous les paramètres (y compris l'ID)
    public Transaction(int id, int clientId, int agriculteurId, int terrainId, String type, Date dateTransaction, double montant) {
        this.id = id;
        this.clientId = clientId;
        this.agriculteurId = agriculteurId;
        this.terrainId = terrainId;
        this.type = type;
        this.dateTransaction = dateTransaction;
        this.montant = montant;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public int getAgriculteurId() {
        return agriculteurId;
    }

    public void setAgriculteurId(int agriculteurId) {
        this.agriculteurId = agriculteurId;
    }

    public int getTerrainId() {
        return terrainId;
    }

    public void setTerrainId(int terrainId) {
        this.terrainId = terrainId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getDateTransaction() {
        return dateTransaction;
    }

    public void setDateTransaction(Date dateTransaction) {
        this.dateTransaction = dateTransaction;
    }

    public double getMontant() {
        return montant;
    }

    public void setMontant(double montant) {
        this.montant = montant;
    }

    // Méthode toString
    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", clientId=" + clientId +
                ", agriculteurId=" + agriculteurId +
                ", terrainId=" + terrainId +
                ", type='" + type + '\'' +
                ", dateTransaction=" + dateTransaction +
                ", montant=" + montant +
                '}';
    }

    // Méthodes equals et hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return id == that.id &&
                clientId == that.clientId &&
                agriculteurId == that.agriculteurId &&
                terrainId == that.terrainId &&
                Double.compare(that.montant, montant) == 0 &&
                Objects.equals(type, that.type) &&
                Objects.equals(dateTransaction, that.dateTransaction);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, clientId, agriculteurId, terrainId, type, dateTransaction, montant);
    }
}

