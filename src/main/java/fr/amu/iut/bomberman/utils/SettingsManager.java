package fr.amu.iut.bomberman.utils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Gestionnaire des paramètres du jeu
 * Stocke et charge les préférences de l'utilisateur
 *
 * @author Groupe_3_6
 * @version 1.0
 */
public class SettingsManager {

    private static final String SETTINGS_FILE = System.getProperty("user.home") + "/.bomberman/settings.properties";
    private static SettingsManager instance;
    private final Properties properties;
    private final Map<String, Object> settingsCache;

    /**
     * Constructeur privé (Singleton)
     * Initialise et charge les paramètres
     */
    private SettingsManager() {
        properties = new Properties();
        settingsCache = new HashMap<>();
        loadSettings();
    }

    /**
     * Obtient l'instance unique du gestionnaire de paramètres
     *
     * @return Instance du SettingsManager
     */
    public static synchronized SettingsManager getInstance() {
        if (instance == null) {
            instance = new SettingsManager();
        }
        return instance;
    }

    /**
     * Charge les paramètres depuis le fichier
     */
    public void loadSettings() {
        File file = new File(SETTINGS_FILE);

        // Crée le dossier si nécessaire
        File dir = file.getParentFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // Charge les paramètres s'ils existent, sinon utilise les valeurs par défaut
        if (file.exists()) {
            try (InputStream input = new FileInputStream(file)) {
                properties.load(input);
                System.out.println("Paramètres chargés avec succès depuis " + SETTINGS_FILE);
            } catch (IOException e) {
                System.err.println("Erreur lors du chargement des paramètres : " + e.getMessage());
                setDefaultSettings();
            }
        } else {
            setDefaultSettings();
        }

        // Charge les valeurs dans le cache pour un accès plus rapide
        updateCache();
    }

    /**
     * Enregistre les paramètres dans le fichier
     */
    public void saveSettings() {
        try (OutputStream output = new FileOutputStream(SETTINGS_FILE)) {
            properties.store(output, "Bomberman Game Settings");
            System.out.println("Paramètres sauvegardés avec succès dans " + SETTINGS_FILE);
        } catch (IOException e) {
            System.err.println("Erreur lors de la sauvegarde des paramètres : " + e.getMessage());
        }
    }

    /**
     * Définit les paramètres par défaut
     */
    private void setDefaultSettings() {
        // Audio
        properties.setProperty("audio.musicEnabled", "true");
        properties.setProperty("audio.soundEnabled", "true");
        properties.setProperty("audio.musicVolume", "0.5");
        properties.setProperty("audio.soundVolume", "0.7");

        // Vidéo
        properties.setProperty("video.fullscreen", "true");
        properties.setProperty("video.showFPS", "false");

        // Gameplay
        properties.setProperty("gameplay.roundsToWin", "3");
        properties.setProperty("gameplay.timeLimit", "180");

        System.out.println("Paramètres par défaut appliqués");
    }

    /**
     * Met à jour le cache avec les valeurs des propriétés
     */
    private void updateCache() {
        settingsCache.put("audio.musicEnabled", getBooleanSetting("audio.musicEnabled"));
        settingsCache.put("audio.soundEnabled", getBooleanSetting("audio.soundEnabled"));
        settingsCache.put("audio.musicVolume", getDoubleSetting("audio.musicVolume"));
        settingsCache.put("audio.soundVolume", getDoubleSetting("audio.soundVolume"));
        settingsCache.put("video.fullscreen", getBooleanSetting("video.fullscreen"));
        settingsCache.put("video.showFPS", getBooleanSetting("video.showFPS"));
        settingsCache.put("gameplay.roundsToWin", getIntSetting("gameplay.roundsToWin"));
        settingsCache.put("gameplay.timeLimit", getIntSetting("gameplay.timeLimit"));
    }

    /**
     * Obtient un paramètre booléen
     *
     * @param key Clé du paramètre
     * @return Valeur booléenne du paramètre
     */
    public boolean getBooleanSetting(String key) {
        return Boolean.parseBoolean(properties.getProperty(key, "false"));
    }

    /**
     * Définit un paramètre booléen
     *
     * @param key   Clé du paramètre
     * @param value Valeur booléenne
     */
    public void setBooleanSetting(String key, boolean value) {
        properties.setProperty(key, String.valueOf(value));
        settingsCache.put(key, value);
        saveSettings();
    }

    /**
     * Obtient un paramètre entier
     *
     * @param key Clé du paramètre
     * @return Valeur entière du paramètre
     */
    public int getIntSetting(String key) {
        try {
            return Integer.parseInt(properties.getProperty(key, "0"));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Obtient un paramètre décimal
     *
     * @param key Clé du paramètre
     * @return Valeur décimale du paramètre
     */
    public double getDoubleSetting(String key) {
        try {
            return Double.parseDouble(properties.getProperty(key, "0.0"));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    /**
     * Définit un paramètre décimal
     *
     * @param key   Clé du paramètre
     * @param value Valeur décimale
     */
    public void setDoubleSetting(String key, double value) {
        properties.setProperty(key, String.valueOf(value));
        settingsCache.put(key, value);
        saveSettings();
    }

}
