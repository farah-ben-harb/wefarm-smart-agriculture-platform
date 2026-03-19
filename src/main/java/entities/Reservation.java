package entities;

import com.sun.jna.WString;

import java.time.LocalDate;
import java.util.Objects;

public class Reservation {
    private int id;
    private Integer evenementId;
    private Integer clientId;
    private LocalDate dateReservation;
    private String etat;

    // Constructeur sans paramètre
    public Reservation() {
    }

    // Constructeur avec tous les paramètres
    public Reservation(int id, Integer evenementId, Integer clientId, LocalDate dateReservation) {
        this.id = id;
        this.evenementId = evenementId;
        this.clientId = clientId;
        this.dateReservation = dateReservation;
    }

    // Constructeur sans ID (cas où l'ID est généré automatiquement)
    public Reservation(Integer evenementId, Integer clientId, LocalDate dateReservation) {
        this.evenementId = evenementId;
        this.clientId = clientId;
        this.dateReservation = dateReservation;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getEvenementId() {
        return evenementId;
    }

    public void setEvenementId(Integer evenementId) {
        this.evenementId = evenementId;
    }

    public Integer getClientId() {
        return clientId;
    }

    public void setClientId(Integer clientId) {
        this.clientId = clientId;
    }

    public LocalDate getDateReservation() {
        return dateReservation;
    }

    public void setDateReservation(LocalDate dateReservation) {
        this.dateReservation = dateReservation;
    }

    public String getEtat() {
        return etat;
    }
    public void setEtat(String etat) {
        this.etat = etat;
    }

    // Méthode toString
    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", evenementId=" + evenementId +
                ", clientId=" + clientId +
                ", dateReservation=" + dateReservation +
                '}';
    }

    // Méthodes equals et hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reservation that = (Reservation) o;
        return id == that.id &&
                Objects.equals(evenementId, that.evenementId) &&
                Objects.equals(clientId, that.clientId) &&
                Objects.equals(dateReservation, that.dateReservation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, evenementId, clientId, dateReservation);
    }
}

