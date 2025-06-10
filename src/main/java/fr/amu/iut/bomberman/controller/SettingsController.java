package fr.amu.iut.bomberman.controller;

import fr.amu.iut.bomberman.utils.SoundManager;
import fr.amu.iut.bomberman.utils.ThemeManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.prefs.Preferences;

/**
 * Contrôleur pour les paramètres du jeu
 * Gère l'audio, les graphismes et les contrôles
 *
 * @author Super Bomberman Team
 * @version 1.0
 */
public class SettingsController {

    // Onglet Audio
    @FXML
    private Slider soundVolumeSlider;
    @FXML
    private Label soundVolumeLabel;
    @FXML
    private Slider musicVolumeSlider;
    @FXML
    private Label musicVolumeLabel;
    @FXML
    private CheckBox soundEnabledCheckBox;
    @FXML
    private CheckBox musicEnabledCheckBox;

    // Onglet Graphismes
    @FXML
    private ComboBox<String> themeComboBox;
    @FXML
    private HBox themePreviewBox;
    @FXML
    private CheckBox fullscreenCheckBox;
    @FXML
    private CheckBox vsyncCheckBox;

    // Onglet Contrôles
    @FXML
    private Button p1UpButton;
    @FXML
    private Button p1DownButton;
    @FXML
    private Button p1LeftButton;
    @FXML
    private Button p1RightButton;
    @FXML
    private Button p1BombButton;

    @FXML
    private Button p2UpButton;
    @FXML
    private Button p2DownButton;
    @FXML
    private Button p2LeftButton;
    @FXML
    private Button p2RightButton;
    @FXML
    private Button p2BombButton;

    private SoundManager soundManager;
    private ThemeManager themeManager;
    private Preferences preferences;

    // Map des contrôles
    private Map<String, KeyCode> keyBindings;
    private Button currentKeyButton;

    /**
     * Initialisation du contrôleur
     */
    @FXML
    public void initialize() {
        soundManager = SoundManager.getInstance();
        themeManager = ThemeManager.getInstance();
        preferences = Preferences.userNodeForPackage(SettingsController.class);
        keyBindings = new HashMap<>();

        loadSettings();
        setupListeners();
    }

    /**
     * Charge les paramètres sauvegardés
     */
    private void loadSettings() {
        // Audio
        double soundVolume = preferences.getDouble("soundVolume", 0.7) * 100;
        double musicVolume = preferences.getDouble("musicVolume", 0.5) * 100;
        boolean soundEnabled = preferences.getBoolean("soundEnabled", true);
        boolean musicEnabled = preferences.getBoolean("musicEnabled", true);

        soundVolumeSlider.setValue(soundVolume);
        musicVolumeSlider.setValue(musicVolume);
        soundEnabledCheckBox.setSelected(soundEnabled);
        musicEnabledCheckBox.setSelected(musicEnabled);

        updateVolumeLabels();

        // Graphismes
        String currentTheme = preferences.get("theme", "classic");
        themeComboBox.getItems().addAll(themeManager.getAvailableThemes());
        themeComboBox.setValue(currentTheme);

        boolean fullscreen = preferences.getBoolean("fullscreen", false);
        boolean vsync = preferences.getBoolean("vsync", true);
        fullscreenCheckBox.setSelected(fullscreen);
        vsyncCheckBox.setSelected(vsync);

        // Contrôles
        loadKeyBindings();
        updateControlButtons();
    }

    /**
     * Configure les listeners
     */
    private void setupListeners() {
        // Sliders audio
        soundVolumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> soundVolumeLabel.setText(String.format("%.0f%%", newVal.doubleValue())));

        musicVolumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> musicVolumeLabel.setText(String.format("%.0f%%", newVal.doubleValue())));

        // Changement de thème
        themeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateThemePreview(newVal);
            }
        });

        // Boutons de contrôle
        setupControlButton(p1UpButton, "p1.up");
        setupControlButton(p1DownButton, "p1.down");
        setupControlButton(p1LeftButton, "p1.left");
        setupControlButton(p1RightButton, "p1.right");
        setupControlButton(p1BombButton, "p1.bomb");

        setupControlButton(p2UpButton, "p2.up");
        setupControlButton(p2DownButton, "p2.down");
        setupControlButton(p2LeftButton, "p2.left");
        setupControlButton(p2RightButton, "p2.right");
        setupControlButton(p2BombButton, "p2.bomb");
    }

    /**
     * Configure un bouton de contrôle
     *
     * @param button Bouton à configurer
     * @param key    Clé de la touche
     */
    private void setupControlButton(Button button, String key) {
        button.setOnAction(e -> startKeyCapture(button, key));
    }

    /**
     * Démarre la capture d'une touche
     *
     * @param button Bouton concerné
     * @param key    Clé de la touche
     */
    private void startKeyCapture(Button button, String key) {
        currentKeyButton = button;
        button.setText("...");

        // Capturer la prochaine touche
        button.getScene().setOnKeyPressed(event -> {
            if (currentKeyButton == button) {
                KeyCode keyCode = event.getCode();

                // Vérifier que la touche n'est pas déjà utilisée
                if (!isKeyUsed(keyCode, key)) {
                    keyBindings.put(key, keyCode);
                    button.setText(getKeyName(keyCode));
                } else {
                    showWarning(
                    );
                    button.setText(getKeyName(keyBindings.get(key)));
                }

                currentKeyButton = null;
                button.getScene().setOnKeyPressed(null);
            }
        });
    }

    /**
     * Vérifie si une touche est déjà utilisée
     *
     * @param keyCode    Code de la touche
     * @param excludeKey Clé à exclure de la vérification
     * @return true si la touche est utilisée
     */
    private boolean isKeyUsed(KeyCode keyCode, String excludeKey) {
        for (Map.Entry<String, KeyCode> entry : keyBindings.entrySet()) {
            if (!entry.getKey().equals(excludeKey) && entry.getValue() == keyCode) {
                return true;
            }
        }
        return false;
    }

    /**
     * Obtient le nom d'affichage d'une touche
     *
     * @param keyCode Code de la touche
     * @return Nom de la touche
     */
    private String getKeyName(KeyCode keyCode) {
        if (keyCode == null) return "?";

        return switch (keyCode) {
            case SPACE -> "ESPACE";
            case ENTER -> "ENTRÉE";
            case UP -> "↑";
            case DOWN -> "↓";
            case LEFT -> "←";
            case RIGHT -> "→";
            default -> keyCode.toString();
        };
    }

    /**
     * Charge les raccourcis clavier
     */
    private void loadKeyBindings() {
        // Contrôles par défaut
        keyBindings.put("p1.up", KeyCode.valueOf(preferences.get("key.p1.up", "Z")));
        keyBindings.put("p1.down", KeyCode.valueOf(preferences.get("key.p1.down", "S")));
        keyBindings.put("p1.left", KeyCode.valueOf(preferences.get("key.p1.left", "Q")));
        keyBindings.put("p1.right", KeyCode.valueOf(preferences.get("key.p1.right", "D")));
        keyBindings.put("p1.bomb", KeyCode.valueOf(preferences.get("key.p1.bomb", "SPACE")));

        keyBindings.put("p2.up", KeyCode.valueOf(preferences.get("key.p2.up", "UP")));
        keyBindings.put("p2.down", KeyCode.valueOf(preferences.get("key.p2.down", "DOWN")));
        keyBindings.put("p2.left", KeyCode.valueOf(preferences.get("key.p2.left", "LEFT")));
        keyBindings.put("p2.right", KeyCode.valueOf(preferences.get("key.p2.right", "RIGHT")));
        keyBindings.put("p2.bomb", KeyCode.valueOf(preferences.get("key.p2.bomb", "ENTER")));
    }

    /**
     * Met à jour l'affichage des boutons de contrôle
     */
    private void updateControlButtons() {
        p1UpButton.setText(getKeyName(keyBindings.get("p1.up")));
        p1DownButton.setText(getKeyName(keyBindings.get("p1.down")));
        p1LeftButton.setText(getKeyName(keyBindings.get("p1.left")));
        p1RightButton.setText(getKeyName(keyBindings.get("p1.right")));
        p1BombButton.setText(getKeyName(keyBindings.get("p1.bomb")));

        p2UpButton.setText(getKeyName(keyBindings.get("p2.up")));
        p2DownButton.setText(getKeyName(keyBindings.get("p2.down")));
        p2LeftButton.setText(getKeyName(keyBindings.get("p2.left")));
        p2RightButton.setText(getKeyName(keyBindings.get("p2.right")));
        p2BombButton.setText(getKeyName(keyBindings.get("p2.bomb")));
    }

    /**
     * Met à jour les labels de volume
     */
    private void updateVolumeLabels() {
        soundVolumeLabel.setText(String.format("%.0f%%", soundVolumeSlider.getValue()));
        musicVolumeLabel.setText(String.format("%.0f%%", musicVolumeSlider.getValue()));
    }

    /**
     * Met à jour l'aperçu du thème
     *
     * @param themeName Nom du thème
     */
    private void updateThemePreview(String themeName) {
        // TODO: Implémenter l'aperçu du thème
        themePreviewBox.getChildren().clear();
        Label previewLabel = new Label("Aperçu du thème: " + themeName);
        themePreviewBox.getChildren().add(previewLabel);
    }

    /**
     * Teste le son
     */
    @FXML
    private void handleTestSound() {
        soundManager.playSound("menu_select");
    }

    /**
     * Réinitialise les contrôles par défaut
     */
    @FXML
    private void handleResetControls() {
        keyBindings.clear();
        keyBindings.put("p1.up", KeyCode.Z);
        keyBindings.put("p1.down", KeyCode.S);
        keyBindings.put("p1.left", KeyCode.Q);
        keyBindings.put("p1.right", KeyCode.D);
        keyBindings.put("p1.bomb", KeyCode.SPACE);

        keyBindings.put("p2.up", KeyCode.UP);
        keyBindings.put("p2.down", KeyCode.DOWN);
        keyBindings.put("p2.left", KeyCode.LEFT);
        keyBindings.put("p2.right", KeyCode.RIGHT);
        keyBindings.put("p2.bomb", KeyCode.ENTER);

        updateControlButtons();
    }

    /**
     * Applique les paramètres
     */
    @FXML
    private void handleApply() {
        // Audio
        double soundVolume = soundVolumeSlider.getValue() / 100.0;
        double musicVolume = musicVolumeSlider.getValue() / 100.0;
        boolean soundEnabled = soundEnabledCheckBox.isSelected();
        boolean musicEnabled = musicEnabledCheckBox.isSelected();

        soundManager.setSoundVolume(soundVolume);
        soundManager.setMusicVolume(musicVolume);
        soundManager.setSoundEnabled(soundEnabled);
        soundManager.setMusicEnabled(musicEnabled);

        preferences.putDouble("soundVolume", soundVolume);
        preferences.putDouble("musicVolume", musicVolume);
        preferences.putBoolean("soundEnabled", soundEnabled);
        preferences.putBoolean("musicEnabled", musicEnabled);

        // Graphismes
        String selectedTheme = themeComboBox.getValue();
        if (selectedTheme != null && !selectedTheme.equals(themeManager.getCurrentTheme())) {
            themeManager.loadTheme(selectedTheme);
            preferences.put("theme", selectedTheme);
        }

        boolean fullscreen = fullscreenCheckBox.isSelected();
        boolean vsync = vsyncCheckBox.isSelected();
        preferences.putBoolean("fullscreen", fullscreen);
        preferences.putBoolean("vsync", vsync);

        // Appliquer le plein écran
        Stage stage = (Stage) soundVolumeSlider.getScene().getWindow();
        stage.setFullScreen(fullscreen);

        // Contrôles
        for (Map.Entry<String, KeyCode> entry : keyBindings.entrySet()) {
            preferences.put("key." + entry.getKey(), entry.getValue().toString());
        }

        showInfo();
    }

    /**
     * Annule les modifications
     */
    @FXML
    private void handleCancel() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainMenu.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/main.css")).toExternalForm());

            Stage stage = (Stage) soundVolumeSlider.getScene().getWindow();
            stage.setScene(scene);

        } catch (IOException e) {
            showError("Impossible de retourner au menu: " + e.getMessage());
        }
    }

    /**
     * Affiche une information
     */
    private void showInfo() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Paramètres appliqués");
        alert.setHeaderText(null);
        alert.setContentText("Les paramètres ont été enregistrés avec succès.");
        alert.showAndWait();
    }

    /**
     * Affiche un avertissement
     */
    private void showWarning() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Touche déjà utilisée");
        alert.setHeaderText(null);
        alert.setContentText("Cette touche est déjà assignée à une autre action.");
        alert.showAndWait();
    }

    /**
     * Affiche une erreur
     *
     * @param message Message
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}