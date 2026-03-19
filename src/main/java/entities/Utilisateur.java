package entities;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.sql.Date;

public class Utilisateur {

    // Attributs
    private int id;
    private String email;
    private String roles = "[\"ROLE_USER\"]";
    private String adresse;
    private String password;
    private String firstName;
    private String lastName;

    private Date dateNaissance;  // ✅ Utiliser LocalDate pour une date sans heure
    private String image;
    private LocalDateTime dateCreation = LocalDateTime.now(); // ✅ Utiliser LocalDateTime pour date + heure
    private String status = "désactivé";
    private boolean isVerified=true ;
    private String confirmationToken;
    private LocalDateTime reactivationDate;
    private int numTel;

    // Constructeur sans paramètre
    public Utilisateur() {
    }

    // Constructeur avec tous les paramètres
    public Utilisateur(int id, String email, String roles, String adresse, String password, String firstName,
                       String lastName, Date dateNaissance, String image, LocalDateTime dateCreation, String status,
                       boolean isVerified, String confirmationToken,LocalDateTime reactivationDate, int numTel) {
        this.id = id;
        this.email = email;
        this.roles = roles;
        this.adresse = adresse;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateNaissance = dateNaissance;
        this.image = image;
        this.dateCreation = dateCreation;
        this.status = status;
        this.isVerified = isVerified;
        this.confirmationToken = confirmationToken;
        this.reactivationDate = reactivationDate;
        this.numTel = numTel;
    }

    // Constructeur sans ID mais avec les autres paramètres
    public Utilisateur(String email, String roles, String adresse, String password, String firstName,
                       String lastName, Date dateNaissance, String image, LocalDateTime dateCreation, String status,
                       boolean isVerified, int numTel) {
        this.email = email;
        this.roles = roles;
        this.adresse = adresse;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateNaissance = dateNaissance;
        this.image = image;
        this.dateCreation = dateCreation;
        this.status = status;
        this.isVerified = isVerified;

        this.numTel = numTel;
    }

    // Getter et Setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Date getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(Date dateNaissance) {
        this.dateNaissance = dateNaissance;
    }
    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean isVerified) {
        this.isVerified = isVerified;
    }

    public String getConfirmationToken() {
        return confirmationToken;
    }

    public void setConfirmationToken(String confirmationToken) {
        this.confirmationToken = confirmationToken;
    }

    public LocalDateTime getReactivationDate() {
        return reactivationDate;
    }

    public void setReactivationDate(LocalDateTime reactivationDate) {
        this.reactivationDate = reactivationDate;
    }

    public int getNumTel() {
        return numTel;
    }

    public void setNumTel(int numTel) {
        this.numTel = numTel;
    }

    // Méthode toString
    @Override
    public String toString() {
        return "Utilisateur{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", roles='" + roles + '\'' +
                ", adresse='" + adresse + '\'' +
                ", password='" + password + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", dateNaissance='" + dateNaissance + '\'' +
                ", image='" + image + '\'' +
                ", dateCreation='" + dateCreation + '\'' +
                ", status='" + status + '\'' +
                ", isVerified=" + isVerified +
                ", confirmationToken='" + confirmationToken + '\'' +
                ", reactivationDate='" + reactivationDate + '\'' +
                ", numTel=" + numTel +
                '}';
    }

    // Méthode equals
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Utilisateur that = (Utilisateur) o;
        return id == that.id && isVerified == that.isVerified && numTel == that.numTel && Objects.equals(email, that.email) && Objects.equals(roles, that.roles) && Objects.equals(adresse, that.adresse) && Objects.equals(password, that.password) && Objects.equals(firstName, that.firstName) && Objects.equals(lastName, that.lastName) && Objects.equals(dateNaissance, that.dateNaissance) && Objects.equals(image, that.image) && Objects.equals(dateCreation, that.dateCreation) && Objects.equals(status, that.status) && Objects.equals(confirmationToken, that.confirmationToken) && Objects.equals(reactivationDate, that.reactivationDate);
    }

    // Méthode hashCode
    @Override
    public int hashCode() {
        return Objects.hash(id, email, roles, adresse, password, firstName, lastName, dateNaissance, image, dateCreation, status, isVerified, confirmationToken, reactivationDate, numTel);
    }
}

