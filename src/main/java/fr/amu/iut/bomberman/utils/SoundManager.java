package fr.amu.iut.bomberman.utils;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.util.HashMap;
import java.util.Map;

/**
 * Gestionnaire des sons et musiques du jeu
 * Gère la lecture et le volume des effets sonores
 *
 * @author Groupe_3_6
 * @version 1.0
 */
public class SoundManager {

    private static SoundManager instance;
    private Map<String, MediaPlayer> soundCache;
    private MediaPlayer currentMusic;
    private double soundVolume = 0.7;
    private double musicVolume = 0.5;
    private boolean soundEnabled = true;
    private boolean musicEnabled = true;
    // Référence au gestionnaire de paramètres
    private final SettingsManager settingsManager;

    /**
     * Constructeur privé (Singleton)
     */
    private SoundManager() {
        soundCache = new HashMap<>();
        // Initialiser avec les paramètres sauvegardés
        settingsManager = SettingsManager.getInstance();
        loadSettings();
        loadSounds();
    }

    /**
     * Obtient l'instance unique du gestionnaire
     *
     * @return Instance du SoundManager
     */
    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    /**
     * Charge tous les sons du jeu
     */
    private void loadSounds() {
        // Sons d'interface - créer des sons de remplacement si les fichiers n'existent pas
        loadSoundSafe("menu_hover", "/sounds/effects/menu_hover.wav");
        loadSoundSafe("menu_select", "/sounds/effects/menu_select.mp3");

        // Sons de jeu
        loadSoundSafe("game_start", "/sounds/effects/game_start.wav");
        loadSoundSafe("bomb_place", "/sounds/effects/bomb_place.wav");
        loadSoundSafe("bomb_explode", "/sounds/effects/bomb_explode.aiff");
        loadSoundSafe("player_hit", "/sounds/effects/player_hit.mp3");
        loadSoundSafe("powerup_collect", "/sounds/effects/powerup_pickup.wav");
        loadSoundSafe("wall_break", "/sounds/effects/wall_break.wav");
        loadSoundSafe("round_end", "/sounds/effects/game_over.wav");
        loadSoundSafe("victory", "/sounds/effects/victory.wav");

        // Musiques
        loadSoundSafe("menu_theme", "/sounds/music/menu_theme.mp3");
        loadSoundSafe("game_theme", "/sounds/music/game_theme.mp3");
        loadSoundSafe("victory_theme", "/sounds/music/victory_theme.mp3");
    }

    /**
     * Charge un son de manière sécurisée (sans planter si le fichier n'existe pas)
     *
     * @param name Nom du son
     * @param path Chemin du fichier
     */
    private void loadSoundSafe(String name, String path) {
        try {
            // Vérifier que le fichier existe
            if (getClass().getResource(path) != null) {
                Media media = new Media(getClass().getResource(path).toExternalForm());
                MediaPlayer player = new MediaPlayer(media);
                soundCache.put(name, player);
                System.out.println("Son chargé: " + name + " -> " + path);
            } else {
                System.out.println("Fichier audio manquant: " + path + " (son désactivé pour " + name + ")");
                // On peut créer un MediaPlayer silencieux ou simplement ignorer
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement du son " + name + ": " + e.getMessage());
        }
    }

    /**
     * Charge un son dans le cache (méthode originale conservée pour compatibilité)
     *
     * @param name Nom du son
     * @param path Chemin du fichier
     */
    private void loadSound(String name, String path) {
        loadSoundSafe(name, path);
    }

    /**
     * Joue un effet sonore
     *
     * @param soundName Nom du son à jouer
     */
    public void playSound(String soundName) {
        if (!soundEnabled) return;

        try {
            MediaPlayer player = soundCache.get(soundName);
            if (player != null) {
                player.stop();
                player.setVolume(soundVolume);
                player.play();
            } else {
                System.out.println("Son non trouvé: " + soundName);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la lecture du son " + soundName + ": " + e.getMessage());
        }
    }

    /**
     * Joue une musique en boucle
     *
     * @param musicName Nom de la musique
     */
    public void playMusic(String musicName) {
        if (!musicEnabled) return;

        try {
            // Arrêter la musique actuelle
            if (currentMusic != null) {
                currentMusic.stop();
            }

            MediaPlayer player = soundCache.get(musicName);
            if (player != null) {
                currentMusic = player;
                currentMusic.setVolume(musicVolume);
                currentMusic.setCycleCount(MediaPlayer.INDEFINITE);
                currentMusic.play();
            } else {
                System.out.println("Musique non trouvée: " + musicName);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la lecture de la musique " + musicName + ": " + e.getMessage());
        }
    }

    /**
     * Arrête la musique actuelle
     */
    public void stopMusic() {
        try {
            if (currentMusic != null) {
                currentMusic.stop();
                currentMusic = null;
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de l'arrêt de la musique: " + e.getMessage());
        }
    }

    /**
     * Met la musique en pause
     */
    public void pauseMusic() {
        try {
            if (currentMusic != null) {
                currentMusic.pause();
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la mise en pause de la musique: " + e.getMessage());
        }
    }

    /**
     * Reprend la musique
     */
    public void resumeMusic() {
        try {
            if (currentMusic != null && musicEnabled) {
                currentMusic.play();
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la reprise de la musique: " + e.getMessage());
        }
    }

    /**
     * Définit le volume des effets sonores
     *
     * @param volume Volume entre 0.0 et 1.0
     */
    public void setSoundVolume(double volume) {
        soundVolume = Math.max(0.0, Math.min(1.0, volume));
        // Sauvegarder le nouveau volume
        saveSettings();
    }

    /**
     * Définit le volume de la musique
     *
     * @param volume Volume entre 0.0 et 1.0
     */
    public void setMusicVolume(double volume) {
        musicVolume = Math.max(0.0, Math.min(1.0, volume));
        if (currentMusic != null) {
            currentMusic.setVolume(musicVolume);
        }
        // Sauvegarder le nouveau volume
        saveSettings();
    }

    /**
     * Active ou désactive les effets sonores
     *
     * @param enabled true pour activer
     */
    public void setSoundEnabled(boolean enabled) {
        soundEnabled = enabled;
        // Sauvegarder le nouvel état
        saveSettings();
    }

    /**
     * Active ou désactive la musique
     *
     * @param enabled true pour activer
     */
    public void setMusicEnabled(boolean enabled) {
        musicEnabled = enabled;
        if (!enabled) {
            stopMusic();
        } else if (currentMusic != null) {
            currentMusic.play();
        }
        // Sauvegarder le nouvel état
        saveSettings();
    }

    /**
     * Charge les paramètres audio depuis le gestionnaire de paramètres
     */
    private void loadSettings() {
        soundEnabled = settingsManager.getBooleanSetting("audio.soundEnabled");
        musicEnabled = settingsManager.getBooleanSetting("audio.musicEnabled");
        soundVolume = settingsManager.getDoubleSetting("audio.soundVolume");
        musicVolume = settingsManager.getDoubleSetting("audio.musicVolume");

        System.out.println("Paramètres audio chargés: musique=" + (musicEnabled ? "activée" : "désactivée") +
                ", son=" + (soundEnabled ? "activé" : "désactivé") +
                ", volume musique=" + musicVolume +
                ", volume son=" + soundVolume);
    }

    /**
     * Sauvegarde les paramètres audio dans le gestionnaire de paramètres
     */
    private void saveSettings() {
        settingsManager.setBooleanSetting("audio.soundEnabled", soundEnabled);
        settingsManager.setBooleanSetting("audio.musicEnabled", musicEnabled);
        settingsManager.setDoubleSetting("audio.soundVolume", soundVolume);
        settingsManager.setDoubleSetting("audio.musicVolume", musicVolume);
    }

    // Getters

    public double getSoundVolume() {
        return soundVolume;
    }

    public double getMusicVolume() {
        return musicVolume;
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }
}