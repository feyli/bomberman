package fr.amu.iut.bomberman.model;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Classe représentant le profil d'un joueur
 * Stocke les statistiques et informations persistantes
 *
 * @author Super Bomberman Team
 * @version 1.0
 */
public class PlayerProfile implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String id;
    private String firstName;
    private String lastName;
    private String nickname;
    private String avatarPath;
    private int gamesPlayed;
    private int gamesWon;
    private int totalScore;
    private final LocalDateTime createdDate;
    private LocalDateTime lastPlayedDate;

    /**
     * Constructeur pour un nouveau profil
     *
     * @param firstName Prénom du joueur
     * @param lastName  Nom du joueur
     * @param nickname  Pseudo du joueur
     */
    public PlayerProfile(String firstName, String lastName, String nickname) {
        this.id = UUID.randomUUID().toString();
        this.firstName = firstName;
        this.lastName = lastName;
        this.nickname = nickname;
        this.avatarPath = "/images/avatars/default.png";
        this.gamesPlayed = 0;
        this.gamesWon = 0;
        this.totalScore = 0;
        this.createdDate = LocalDateTime.now();
        this.lastPlayedDate = LocalDateTime.now();
    }

    /**
     * Met à jour les statistiques après une partie
     *
     * @param won   Si le joueur a gagné
     * @param score Score obtenu
     */
    public void updateStats(boolean won, int score) {
        gamesPlayed++;
        if (won) {
            gamesWon++;
        }
        totalScore += score;
        lastPlayedDate = LocalDateTime.now();
    }

    /**
     * Calcule le taux de victoire
     *
     * @return Pourcentage de victoires
     */
    public double getWinRate() {
        if (gamesPlayed == 0) {
            return 0.0;
        }
        return (double) gamesWon / gamesPlayed * 100;
    }

    /**
     * Calcule le score moyen par partie
     *
     * @return Score moyen
     */
    public double getAverageScore() {
        if (gamesPlayed == 0) {
            return 0.0;
        }
        return (double) totalScore / gamesPlayed;
    }

    /**
     * Obtient le nom complet du joueur
     *
     * @return Nom complet
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Obtient le nom d'affichage (pseudo ou prénom)
     *
     * @return Nom d'affichage
     */
    public String getDisplayName() {
        return (nickname != null && !nickname.isEmpty()) ? nickname : firstName;
    }

    // Getters et Setters

    public String getId() {
        return id;
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

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatarPath() {
        return avatarPath;
    }

    public void setAvatarPath(String avatarPath) {
        this.avatarPath = avatarPath;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public int getGamesWon() {
        return gamesWon;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public LocalDateTime getLastPlayedDate() {
        return lastPlayedDate;
    }

    @Override
    public String toString() {
        return String.format("%s (%s) - %d parties, %.1f%% victoires",
                getDisplayName(), getFullName(),
                gamesPlayed, getWinRate());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PlayerProfile profile = (PlayerProfile) obj;
        return id.equals(profile.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}