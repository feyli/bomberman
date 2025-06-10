package com.bomberman.utils;

import com.bomberman.model.PlayerProfile;
import java.io.*;
import java.util.*;

/**
 * Gestionnaire des profils de joueurs
 * Gère la sauvegarde et le chargement des profils
 *
 * @author Super Bomberman Team
 * @version 1.0
 */
public class ProfileManager {

    private static ProfileManager instance;
    private List<PlayerProfile> profiles;
    private final String PROFILES_FILE = "profiles.dat";
    private final String PROFILES_DIR = System.getProperty("user.home") + "/.bomberman/";

    /**
     * Constructeur privé (Singleton)
     */
    private ProfileManager() {
        profiles = new ArrayList<>();
        createDataDirectory();
        loadProfiles(); // Charger automatiquement les profils
    }

    /**
     * Obtient l'instance unique du gestionnaire
     *
     * @return Instance du ProfileManager
     */
    public static ProfileManager getInstance() {
        if (instance == null) {
            instance = new ProfileManager();
        }
        return instance;
    }

    /**
     * Crée le répertoire de données s'il n'existe pas
     */
    private void createDataDirectory() {
        File dir = new File(PROFILES_DIR);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (created) {
                System.out.println("Répertoire de données créé: " + PROFILES_DIR);
            } else {
                System.err.println("Impossible de créer le répertoire: " + PROFILES_DIR);
            }
        }
    }

    /**
     * Charge les profils depuis le fichier
     *
     * @return true si le chargement a réussi
     */
    @SuppressWarnings("unchecked")
    public boolean loadProfiles() {
        File file = new File(PROFILES_DIR + PROFILES_FILE);

        if (!file.exists()) {
            System.out.println("Aucun fichier de profils existant, création des profils par défaut");
            createDefaultProfiles();
            return true;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            List<PlayerProfile> loadedProfiles = (List<PlayerProfile>) ois.readObject();
            profiles.clear();
            profiles.addAll(loadedProfiles);
            System.out.println("Profils chargés avec succès: " + profiles.size() + " profil(s)");
            return true;
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des profils: " + e.getMessage());
            e.printStackTrace();
            createDefaultProfiles();
            return false;
        }
    }

    /**
     * Sauvegarde les profils dans le fichier
     *
     * @return true si la sauvegarde a réussi
     */
    public boolean saveProfiles() {
        File file = new File(PROFILES_DIR + PROFILES_FILE);

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(new ArrayList<>(profiles));
            System.out.println("Profils sauvegardés avec succès: " + profiles.size() + " profil(s)");
            return true;
        } catch (IOException e) {
            System.err.println("Erreur lors de la sauvegarde des profils: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Crée des profils par défaut
     */
    private void createDefaultProfiles() {
        profiles.clear();

        PlayerProfile player1 = new PlayerProfile("Joueur", "Un", "Player1");
        PlayerProfile player2 = new PlayerProfile("Joueur", "Deux", "Player2");
        PlayerProfile player3 = new PlayerProfile("Alice", "Dupont", "Alice");
        PlayerProfile player4 = new PlayerProfile("Bob", "Martin", "Bob");

        profiles.add(player1);
        profiles.add(player2);
        profiles.add(player3);
        profiles.add(player4);

        // Sauvegarder immédiatement
        saveProfiles();

        System.out.println("Profils par défaut créés: " + profiles.size() + " profil(s)");
    }

    /**
     * Ajoute un nouveau profil
     *
     * @param profile Profil à ajouter
     */
    public void addProfile(PlayerProfile profile) {
        if (profile != null && !profiles.contains(profile)) {
            profiles.add(profile);
            saveProfiles();
            System.out.println("Nouveau profil ajouté: " + profile.getDisplayName());
        }
    }

    /**
     * Supprime un profil
     *
     * @param profile Profil à supprimer
     * @return true si la suppression a réussi
     */
    public boolean removeProfile(PlayerProfile profile) {
        boolean removed = profiles.remove(profile);
        if (removed) {
            saveProfiles();
            System.out.println("Profil supprimé: " + profile.getDisplayName());
        }
        return removed;
    }

    /**
     * Met à jour un profil existant
     *
     * @param profile Profil à mettre à jour
     */
    public void updateProfile(PlayerProfile profile) {
        // Le profil est déjà modifié par référence
        saveProfiles();
        System.out.println("Profil mis à jour: " + profile.getDisplayName());
    }

    /**
     * Obtient un profil par son ID
     *
     * @param id ID du profil
     * @return Profil trouvé ou null
     */
    public PlayerProfile getProfileById(String id) {
        return profiles.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    /**
     * Obtient tous les profils
     *
     * @return Liste des profils
     */
    public List<PlayerProfile> getAllProfiles() {
        return new ArrayList<>(profiles);
    }

    /**
     * Recherche des profils par nom
     *
     * @param searchTerm Terme de recherche
     * @return Liste des profils correspondants
     */
    public List<PlayerProfile> searchProfiles(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllProfiles();
        }

        String term = searchTerm.toLowerCase();
        return profiles.stream()
                .filter(p -> p.getFirstName().toLowerCase().contains(term) ||
                        p.getLastName().toLowerCase().contains(term) ||
                        p.getNickname().toLowerCase().contains(term))
                .toList();
    }

    /**
     * Obtient les profils triés par nombre de victoires
     *
     * @param limit Nombre maximum de profils à retourner
     * @return Liste des meilleurs profils
     */
    public List<PlayerProfile> getTopProfiles(int limit) {
        return profiles.stream()
                .sorted((p1, p2) -> Integer.compare(p2.getGamesWon(), p1.getGamesWon()))
                .limit(limit)
                .toList();
    }

    /**
     * Exporte les statistiques en CSV
     *
     * @param filename Nom du fichier de sortie
     * @return true si l'export a réussi
     */
    public boolean exportStatistics(String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(PROFILES_DIR + filename))) {
            writer.println("Nom,Prénom,Pseudo,Parties jouées,Parties gagnées,Taux de victoire,Score total,Score moyen");

            for (PlayerProfile profile : profiles) {
                writer.printf("%s,%s,%s,%d,%d,%.2f%%,%d,%.2f%n",
                        profile.getLastName(),
                        profile.getFirstName(),
                        profile.getNickname(),
                        profile.getGamesPlayed(),
                        profile.getGamesWon(),
                        profile.getWinRate(),
                        profile.getTotalScore(),
                        profile.getAverageScore()
                );
            }

            System.out.println("Statistiques exportées vers: " + PROFILES_DIR + filename);
            return true;
        } catch (IOException e) {
            System.err.println("Erreur lors de l'export: " + e.getMessage());
            return false;
        }
    }

    /**
     * Recharge les profils depuis le fichier
     */
    public void reloadProfiles() {
        loadProfiles();
    }

    /**
     * Obtient le nombre de profils
     *
     * @return Nombre de profils
     */
    public int getProfileCount() {
        return profiles.size();
    }
}