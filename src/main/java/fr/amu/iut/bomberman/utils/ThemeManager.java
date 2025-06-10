package fr.amu.iut.bomberman.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Gestionnaire de thèmes pour l'application
 * Permet de charger et changer les thèmes graphiques
 *
 * @author Super Bomberman Team
 * @version 1.0
 */
public class ThemeManager {

    private static ThemeManager instance;
    private String currentTheme;
    private Properties themeProperties;
    private final String THEMES_PATH = "/themes/";

    /**
     * Constructeur privé (Singleton)
     */
    private ThemeManager() {
        themeProperties = new Properties();
        currentTheme = "classic";
    }

    /**
     * Obtient l'instance unique du gestionnaire
     *
     * @return Instance du ThemeManager
     */
    public static ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }

    /**
     * Charge un thème
     *
     * @param themeName Nom du thème à charger
     * @return true si le chargement a réussi
     */
    public boolean loadTheme(String themeName) {
        try {
            String themePath = THEMES_PATH + themeName + "/theme.properties";
            InputStream is = getClass().getResourceAsStream(themePath);

            if (is != null) {
                themeProperties.clear();
                themeProperties.load(is);
                currentTheme = themeName;
                is.close();
                return true;
            }
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement du thème: " + e.getMessage());
        }
        return false;
    }

    /**
     * Obtient le chemin d'une image du thème actuel
     *
     * @param imageName Nom de l'image
     * @return Chemin complet de l'image
     */
    public String getImagePath(String imageName) {
        String customPath = themeProperties.getProperty(imageName);
        if (customPath != null) {
            return THEMES_PATH + currentTheme + "/" + customPath;
        }
        // Chemin par défaut
        return "/images/" + imageName;
    }

    /**
     * Obtient une couleur du thème
     *
     * @param colorKey Clé de la couleur
     * @return Code couleur hexadécimal
     */
    public String getColor(String colorKey) {
        return themeProperties.getProperty("color." + colorKey, "#000000");
    }

    /**
     * Obtient une police du thème
     *
     * @param fontKey Clé de la police
     * @return Nom de la police
     */
    public String getFont(String fontKey) {
        return themeProperties.getProperty("font." + fontKey, "Arial");
    }

    /**
     * Obtient la liste des thèmes disponibles
     *
     * @return Liste des noms de thèmes
     */
    public List<String> getAvailableThemes() {
        List<String> themes = new ArrayList<>();
        themes.add("classic");
        themes.add("modern");
        themes.add("retro");
        themes.add("dark");
        return themes;
    }

    /**
     * Obtient le nom du thème actuel
     *
     * @return Nom du thème
     */
    public String getCurrentTheme() {
        return currentTheme;
    }

    /**
     * Obtient le chemin CSS du thème actuel
     *
     * @return Chemin du fichier CSS
     */
    public String getThemeCSS() {
        return THEMES_PATH + currentTheme + "/style.css";
    }
}