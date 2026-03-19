package utils;

import entities.Utilisateur;
import services.UtilisateurService;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Date;
import java.util.prefs.Preferences;

public class SessionManager {
    private static SessionManager instance;

    private int id;
    private String email;
    private String roles;
    private String adresse;
    private String password;
    private String firstName;
    private String lastName;
    private Date dateNaissance;
    private String image;
    private LocalDateTime dateCreation;
    private String status;
    private boolean isVerified;
    private String confirmationToken;
    private LocalDateTime reactivationDate;
    private int numTel;

    // Gestion Remember Me
    private static String sessionToken;
    private static long tokenExpiryTime;

    // Constructeur avec Utilisateur
    private SessionManager(Utilisateur user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.roles = user.getRoles();
        this.adresse = user.getAdresse();
        this.password = user.getPassword();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.dateNaissance = user.getDateNaissance();
        this.image = user.getImage();
        this.dateCreation = LocalDateTime.now();
        this.status = user.getStatus();
        this.isVerified = user.isVerified();
        this.confirmationToken = user.getConfirmationToken();
        this.reactivationDate = user.getReactivationDate();
        this.numTel = user.getNumTel();
    }

    // Constructeur privé pour empêcher l'instanciation externe
    private SessionManager(int id, String email, String roles, String adresse, String password, String firstName,
                           String lastName, Date dateNaissance, String image,LocalDateTime dateCreation,
                           String status, boolean isVerified, String confirmationToken, LocalDateTime reactivationDate, int numTel) {
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

    // Méthode pour initialiser la session après connexion
    public static void startSession(int id, String email, String roles, String adresse, String password, String firstName,
                                    String lastName,Date dateNaissance, String image, String status, boolean isVerified,
                                    String confirmationToken, LocalDateTime reactivationDate, int numTel) {
        instance = new SessionManager(id, email, roles, adresse, password, firstName, lastName, dateNaissance, image,
                LocalDateTime.now(),
                status, isVerified, confirmationToken, reactivationDate, numTel);
    }

    // Méthode pour récupérer l'instance de la session
    public static SessionManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Aucune session utilisateur active !");
        }
        return instance;
    }

    // Getter et Setter


    public void setReactivationDate(LocalDateTime reactivationDate) {
        this.reactivationDate = reactivationDate;
    }

    // Getters
    public int getId() { return id; }
    public String getEmail() { return email; }
    public String getRoles() { return roles; }
    public String getAdresse() { return adresse; }
    public String getPassword() { return password; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public Date getDateNaissance() { return dateNaissance; }
    public String getImage() { return image; }
    public LocalDateTime getDateCreation() { return dateCreation; }
    public String getStatus() { return status; }
    public boolean isVerified() { return isVerified; }
    public String getConfirmationToken() { return confirmationToken; }
    public LocalDateTime getReactivationDate() { return reactivationDate; }
    public int getNumTel() { return numTel; }

    // Méthode pour mettre fin à la session (déconnexion)
    public static void logout() {
        instance = null;
    }

    public void setImage(String string) {
        this.image = string;
    }

    // Méthodes de session
    public static void startSession(Utilisateur user, boolean rememberMe) {
        instance = new SessionManager(user);
        if (rememberMe) {
            generateRememberMeToken(user.getId());
        }
    }
    // Remember Me
    // Gestion session
    // Ajoutez cette méthode pour compléter getCurrentUser()
    public static Utilisateur getCurrentUser() {
        if (instance == null) return null;

        Utilisateur user = new Utilisateur();
        user.setId(instance.id);
        user.setEmail(instance.email);
        user.setRoles(instance.roles);
        user.setAdresse(instance.adresse);
        user.setPassword(instance.password);
        user.setFirstName(instance.firstName);
        user.setLastName(instance.lastName);
        user.setDateNaissance(new java.sql.Date(instance.dateNaissance.getTime()));
        user.setImage(instance.image);
        user.setStatus(instance.status);
        user.setVerified(instance.isVerified);
        user.setConfirmationToken(instance.confirmationToken);
        user.setReactivationDate(instance.reactivationDate);
        user.setNumTel(instance.numTel);

        return user;
    }

    // Modifiez tryAutoLogin()
    public static boolean tryAutoLogin() {
        try {
            Preferences prefs = Preferences.userRoot().node("app_session");
            String savedToken = prefs.get("session_token", null);
            long expiryTime = prefs.getLong("token_expiry", 0);
            int userId = prefs.getInt("user_id", -1);

            // Debug logs
            System.out.println("Token: " + savedToken);
            System.out.println("Expiry: " + expiryTime);
            System.out.println("UserID: " + userId);
            System.out.println("Current time: " + System.currentTimeMillis());

            if (savedToken != null && expiryTime > System.currentTimeMillis() && userId != -1) {
                UtilisateurService service = new UtilisateurService();
                Utilisateur user = service.getUtilisateurById(userId);

                if (user != null) {
                    printSessionRemainingTime(); // Ajoutez cet appel

                    System.out.println("User found: " + user.getEmail());
                    // Utilisez la méthode startSession qui correspond à votre implémentation
                    startSession(
                            user.getId(),
                            user.getEmail(),
                            user.getRoles(),
                            user.getAdresse(),
                            user.getPassword(),
                            user.getFirstName(),
                            user.getLastName(),
                            user.getDateNaissance(),
                            user.getImage(),
                            user.getStatus(),
                            user.isVerified(),
                            user.getConfirmationToken(),
                            user.getReactivationDate(),
                            user.getNumTel()
                    );
                    return true;
                } else {
                    System.out.println("No user found with ID: " + userId);
                }
            } else {
                System.out.println("Invalid session data");
                if (savedToken == null) System.out.println("Token is null");
                if (expiryTime <= System.currentTimeMillis()) System.out.println("Token expired");
                if (userId == -1) System.out.println("Invalid user ID");
            }
        } catch (Exception e) {
            System.err.println("Error in tryAutoLogin:");
            e.printStackTrace();
        }

        clearSavedSession();
        return false;
    }
    public static void generateRememberMeToken(int userId) {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[64];
        random.nextBytes(bytes);
        sessionToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        tokenExpiryTime = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000);

        Preferences prefs = Preferences.userRoot().node("app_session");
        prefs.put("session_token", sessionToken);
        prefs.putLong("token_expiry", tokenExpiryTime);
        prefs.putInt("user_id", userId);
        printSessionRemainingTime(); // Ajoutez cet appel

    }
    public static void printSessionRemainingTime() {
        Preferences prefs = Preferences.userRoot().node("app_session");
        long expiryTime = prefs.getLong("token_expiry", 0);
        long currentTime = System.currentTimeMillis();

        if (expiryTime > currentTime) {
            long remainingMillis = expiryTime - currentTime;

            // Convertir en jours, heures, minutes, secondes
            long seconds = remainingMillis / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            long days = hours / 24;

            hours = hours % 24;
            minutes = minutes % 60;
            seconds = seconds % 60;

            System.out.println("\n=== Session Remember Me ===");
            System.out.println("Temps restant avant expiration:");
            System.out.printf("%d jours, %d heures, %d minutes, %d secondes%n", days, hours, minutes, seconds);
            System.out.println("==========================\n");
        } else {
            System.out.println("Aucune session Remember Me active ou session expirée");
        }
    }


    public static void clearSession() {
        instance = null;
        clearSavedSession();
    }

    private static void clearSavedSession() {
        Preferences prefs = Preferences.userRoot().node("app_session");
        prefs.remove("session_token");
        prefs.remove("token_expiry");
        prefs.remove("user_id");
    }
}
