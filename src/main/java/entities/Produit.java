package entities;

import javafx.beans.property.*;

import java.util.Date;
import java.util.Objects;

public class Produit {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty nom = new SimpleStringProperty();
    private final IntegerProperty quantite = new SimpleIntegerProperty();
    private final DoubleProperty prixTotal = new SimpleDoubleProperty();
    private final StringProperty description = new SimpleStringProperty();
    private final StringProperty image = new SimpleStringProperty();
    private final StringProperty categorie = new SimpleStringProperty();
    private final StringProperty stockD = new SimpleStringProperty();
    private final BooleanProperty approved = new SimpleBooleanProperty();
    private final ObjectProperty<Date> updatedAt = new SimpleObjectProperty<>();

    // Default constructor
    public Produit() {
        this.approved.set(false); // Default value matching database
    }

    // Full constructor with optional ID (for database retrieval)
    public Produit(Integer id, String nom, int quantite, double prixTotal,
                   String description, String image, String categorie, String stockD,
                   boolean approved, Date updatedAt) {
        if (id != null) {
            this.id.set(id);
        }
        this.nom.set(nom);
        this.quantite.set(quantite);
        this.prixTotal.set(prixTotal);
        this.description.set(description);
        this.image.set(image);
        this.categorie.set(categorie);
        this.stockD.set(stockD);
        this.approved.set(approved);
        this.updatedAt.set(updatedAt);
    }

    // Constructor without ID (for creating new products)
    public Produit(String nom, int quantite, double prixTotal,
                   String description, String image, String categorie, String stockD,
                   boolean approved, Date updatedAt) {
        this.nom.set(nom);
        this.quantite.set(quantite);
        this.prixTotal.set(prixTotal);
        this.description.set(description);
        this.image.set(image);
        this.categorie.set(categorie);
        this.stockD.set(stockD);
        this.approved.set(approved);
        this.updatedAt.set(updatedAt);
    }

    // Property getters
    public IntegerProperty idProperty() {
        return id;
    }

    public StringProperty nomProperty() {
        return nom;
    }

    public IntegerProperty quantiteProperty() {
        return quantite;
    }

    public DoubleProperty prixTotalProperty() {
        return prixTotal;
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public StringProperty imageProperty() {
        return image;
    }

    public StringProperty categorieProperty() {
        return categorie;
    }

    public StringProperty stockDProperty() {
        return stockD;
    }

    public BooleanProperty approvedProperty() {
        return approved;
    }

    public ObjectProperty<Date> updatedAtProperty() {
        return updatedAt;
    }

    // Regular getters and setters
    public int getId() {
        return id.get();
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public String getNom() {
        return nom.get();
    }

    public void setNom(String nom) {
        this.nom.set(nom);
    }

    public int getQuantite() {
        return quantite.get();
    }

    public void setQuantite(int quantite) {
        this.quantite.set(quantite);
    }

    public double getPrixTotal() {
        return prixTotal.get();
    }

    public void setPrixTotal(double prixTotal) {
        this.prixTotal.set(prixTotal);
    }

    public String getDescription() {
        return description.get();
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public String getImage() {
        return image.get();
    }

    public void setImage(String image) {
        this.image.set(image);
    }

    public String getCategorie() {
        return categorie.get();
    }

    public void setCategorie(String categorie) {
        this.categorie.set(categorie);
    }

    public String getStockD() {
        return stockD.get();
    }

    public void setStockD(String stockD) {
        this.stockD.set(stockD);
    }

    public boolean isApproved() {
        return approved.get();
    }

    public void setApproved(boolean approved) {
        this.approved.set(approved);
    }

    public Date getUpdatedAt() {
        return updatedAt.get();
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt.set(updatedAt);
    }

    @Override
    public String toString() {
        return "Produit{" +
                "id=" + id.get() +
                ", nom='" + nom.get() + '\'' +
                ", quantite=" + quantite.get() +
                ", prixTotal=" + prixTotal.get() +
                ", description='" + description.get() + '\'' +
                ", image='" + image.get() + '\'' +
                ", categorie='" + categorie.get() + '\'' +
                ", stockD='" + stockD.get() + '\'' +
                ", approved=" + approved.get() +
                ", updatedAt=" + updatedAt.get() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Produit produit = (Produit) o;
        return id.get() == produit.id.get() &&
                quantite.get() == produit.quantite.get() &&
                Double.compare(produit.prixTotal.get(), prixTotal.get()) == 0 &&
                approved.get() == produit.approved.get() &&
                Objects.equals(nom.get(), produit.nom.get()) &&
                Objects.equals(description.get(), produit.description.get()) &&
                Objects.equals(image.get(), produit.image.get()) &&
                Objects.equals(categorie.get(), produit.categorie.get()) &&
                Objects.equals(stockD.get(), produit.stockD.get()) &&
                Objects.equals(updatedAt.get(), produit.updatedAt.get());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id.get(), nom.get(), quantite.get(), prixTotal.get(), 
                          description.get(), image.get(), categorie.get(), 
                          stockD.get(), approved.get(), updatedAt.get());
    }
}

