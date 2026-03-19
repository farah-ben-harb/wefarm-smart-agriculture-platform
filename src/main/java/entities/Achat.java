package entities;

import java.util.Date;
import java.util.Objects;

public class Achat {
    private int id;
    private Integer equipementId;
    private Date date;
    private int total;

    // Constructeur sans paramètre
    public Achat() {
    }

    // Constructeur avec tous les paramètres
    public Achat(int id, Integer equipementId, Date date, int total) {
        this.id = id;
        this.equipementId = equipementId;
        this.date = date;
        this.total = total;
    }

    // Constructeur sans ID (cas où l'ID est généré automatiquement)
    public Achat(Integer equipementId, Date date, int total) {
        this.equipementId = equipementId;
        this.date = date;
        this.total = total;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getEquipementId() {
        return equipementId;
    }

    public void setEquipementId(Integer equipementId) {
        this.equipementId = equipementId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    // Méthode toString
    @Override
    public String toString() {
        return "Achat{" +
                "id=" + id +
                ", equipementId=" + equipementId +
                ", date=" + date +
                ", total=" + total +
                '}';
    }

    // Méthodes equals et hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Achat achat = (Achat) o;
        return id == achat.id &&
                total == achat.total &&
                Objects.equals(equipementId, achat.equipementId) &&
                Objects.equals(date, achat.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, equipementId, date, total);
    }
}

