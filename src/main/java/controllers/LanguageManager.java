package controllers;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Gestionnaire de langue centralisé pour l'application
 */
public class LanguageManager {
    private static LanguageManager instance;
    private boolean isArabic = false;
    private Map<String, Consumer<Boolean>> translationListeners = new HashMap<>();

    private LanguageManager() {
        // Constructeur privé pour singleton
    }

    public static synchronized LanguageManager getInstance() {
        if (instance == null) {
            instance = new LanguageManager();
        }
        return instance;
    }


    public void addTranslationListener(String componentId, Consumer<Boolean> translationHandler) {
        translationListeners.put(componentId, translationHandler);

        // Appliquer immédiatement la langue actuelle
        if (translationHandler != null) {
            translationHandler.accept(isArabic);
        }
    }

    /**
     * Supprime un écouteur de changement de langue
     */
    public void removeTranslationListener(String componentId) {
        translationListeners.remove(componentId);
    }

    /**
     * Change la langue de l'application et notifie tous les écouteurs
     */
    public void toggleLanguage() {
        isArabic = !isArabic;
        notifyAllListeners();
    }

    /**
     * @return true si la langue actuelle est l'arabe
     */
    public boolean isArabic() {
        return isArabic;
    }

    /**
     * Notifie tous les écouteurs du changement de langue
     */
    private void notifyAllListeners() {
        for (Consumer<Boolean> listener : translationListeners.values()) {
            listener.accept(isArabic);
        }
    }
}