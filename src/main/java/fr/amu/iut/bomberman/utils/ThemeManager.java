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
 * @author Groupe_3_6
 * @version 1.0
 */
public class ThemeManager {

    private static ThemeManager instance;
    private String currentTheme;
    private final Properties themeProperties;

    /**
     * Constructeur privé (Singleton)
     */
    private ThemeManager() {
        themeProperties = new Properties();
        currentTheme = "obscur";
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
     */
    public void loadTheme(String themeName) {
        try {
            // Définir directement currentTheme pour s'assurer qu'il est mis à jour
            currentTheme = themeName;

            String THEMES_PATH = "/themes/";
            String themePath = THEMES_PATH + themeName + "/theme.properties";
            InputStream is = getClass().getResourceAsStream(themePath);

            if (is != null) {
                themeProperties.clear();
                themeProperties.load(is);
                is.close();
                System.out.println("Thème chargé: " + themeName + " - CSS Path: " + getThemeCssPath());
            } else {
                // Si properties non trouvé, on garde quand même le thème mis à jour
                System.out.println("Thème sans properties chargé: " + themeName + " - CSS Path: " + getThemeCssPath());
            }
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement du thème: " + e.getMessage());
        }
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
     * Obtient le chemin du fichier CSS pour le thème actuel
     *
     * @return Chemin du fichier CSS
     */
    public String getThemeCssPath() {
        // Essaie d'abord de récupérer un CSS spécifique au thème depuis le fichier properties
        String cssFile = themeProperties.getProperty("css.file");

        // Si un CSS spécifique est défini dans le fichier properties du thème
        if (cssFile != null && !cssFile.isEmpty()) {
            return cssFile;
        }

        // Sinon, essaie un fichier CSS nommé d'après le thème actuel
        String themeCssPath = "/css/" + currentTheme + ".css";
        if (getClass().getResource(themeCssPath) != null) {
            return themeCssPath;
        }

        // Par défaut, retourne le CSS principal
        return "/css/main.css";
    }

    /**
     * Obtient le chemin de l'image d'arrière-plan pour le thème actuel
     *
     * @return Chemin de l'image d'arrière-plan
     */
    public String getBackgroundImagePath() {
        // Essaie d'abord de récupérer une image spécifique depuis le fichier properties
        String backgroundImage = themeProperties.getProperty("background.image");

        // Si une image spécifique est définie dans le fichier properties
        if (backgroundImage != null && !backgroundImage.isEmpty()) {
            if (getClass().getResource(backgroundImage) != null) {
                return backgroundImage;
            }
        }

        // Sinon, essaie une image nommée d'après le thème actuel
        String themeBackgroundPath = "/images/backgrounds/" + currentTheme + "_bg.jpg";
        if (getClass().getResource(themeBackgroundPath) != null) {
            return themeBackgroundPath;
        }

        // Si l'image pour le thème spécifique n'existe pas, utilise l'image par défaut
        return "/images/backgrounds/obscur_bg.jpg";
    }

    /**
     * Obtient une liste des thèmes disponibles
     *
     * @return Liste des noms de thèmes disponibles
     */
    public List<String> getAvailableThemes() {
        List<String> themes = new ArrayList<>();

        // Ajouter les thèmes disponibles
        themes.add("clair");
        themes.add("obscur");
        // Vous pouvez ajouter d'autres thèmes ici

        return themes;
    }
}