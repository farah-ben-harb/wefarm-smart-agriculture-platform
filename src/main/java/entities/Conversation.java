package entities;

import java.util.Objects;

public class Conversation {
    private int id;

    // Constructeur sans paramètre
    public Conversation() {
    }

    // Constructeur avec paramètre
    public Conversation(int id) {
        this.id = id;
    }

    // Getter et Setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    // Méthode toString
    @Override
    public String toString() {
        return "Conversation{" +
                "id=" + id +
                '}';
    }

    // Méthodes equals et hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Conversation that = (Conversation) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

