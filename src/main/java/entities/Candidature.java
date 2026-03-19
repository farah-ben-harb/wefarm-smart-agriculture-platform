package entities;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.time.LocalDateTime;

public class Candidature {
    private int id;
    private Integer offreId;
    private Integer employeId;
    private LocalDateTime dateCandidature;
    private String competences;
    private String etat;
    private Map<String, Object> metadata;

    // Constructeur sans paramètre
    public Candidature() {
    }

    // Constructeur avec tous les paramètres
    public Candidature(int id, Integer offreId, Integer employeId, LocalDateTime dateCandidature, String competences, String etat) {
        this.id = id;
        this.offreId = offreId;
        this.employeId = employeId;
        this.dateCandidature = dateCandidature;
        this.competences = competences;
        this.etat = etat;
    }

    // Constructeur sans ID
    public Candidature(Integer offreId, Integer employeId, LocalDateTime dateCandidature, String competences, String etat) {
        this.offreId = offreId;
        this.employeId = employeId;
        this.dateCandidature = dateCandidature;
        this.competences = competences;
        this.etat = etat;
    }
    //Constructeur pour l'ajout d'une candidature
    public Candidature(int offreId, String competences) {
        this.offreId = offreId;
        this.employeId = 1;
        this.competences = competences;

    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getOffreId() {
        return offreId;
    }

    public void setOffreId(Integer offreId) {
        this.offreId = offreId;
    }

    public Integer getEmployeId() {
        return employeId;
    }

    public void setEmployeId(Integer employeId) {
        this.employeId = employeId;
    }

    public LocalDateTime getDateCandidature() {
        return dateCandidature;
    }

    public void setDateCandidature(LocalDateTime dateCandidature) {
        this.dateCandidature = dateCandidature;
    }

    public String getCompetences() {
        return competences;
    }

    public void setCompetences(String competences) {
        this.competences = competences;
    }

    public String getEtat() {
        return etat;
    }

    public void setEtat(String etat) {
        this.etat = etat;
    }
    public Map<String, Object> getMetadata() {
        if (metadata == null) {
            metadata = new HashMap<>();
        }
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    // Méthode toString
    @Override
    public String toString() {
        return "Candidature{" +
                "id=" + id +
                ", offreId=" + offreId +
                ", employeId=" + employeId +
                ", dateCandidature=" + dateCandidature +
                ", competences='" + competences + '\'' +
                ", etat='" + etat + '\'' +
                '}';
    }

    // Méthodes equals et hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Candidature that = (Candidature) o;
        return id == that.id &&
                Objects.equals(offreId, that.offreId) &&
                Objects.equals(employeId, that.employeId) &&
                Objects.equals(dateCandidature, that.dateCandidature) &&
                Objects.equals(competences, that.competences) &&
                Objects.equals(etat, that.etat);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, offreId, employeId, dateCandidature, competences, etat);
    }
}
