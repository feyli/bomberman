package fr.amu.iut.bomberman.utils;

import fr.amu.iut.bomberman.model.PlayerProfile;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

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
     * Met à jour le score d'un joueur
     *
     * @param playerId Identifiant du joueur
     * @param score Score à ajouter
     */
    public void updatePlayerScore(String playerId, int score) {
        for (PlayerProfile profile : profiles) {
            if (profile.getId().equals(playerId)) {
                profile.updateStats(true, score); // Met à jour les stats avec victoire et score
                saveProfiles(); // Sauvegarde les modifications
                return;
            }
        }
        System.err.println("Profil introuvable pour l'ID: " + playerId);
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
     * Obtient un profil par son pseudo (nickname)
     *
     * @param nickname Pseudo du profil
     * @return Profil trouvé ou null
     */
    public PlayerProfile getProfileByNickname(String nickname) {
        return profiles.stream()
                .filter(p -> p.getNickname().equalsIgnoreCase(nickname))
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
     * @param filePath Chemin complet du fichier de sortie
     * @return true si l'export a réussi
     */
    public boolean exportStatistics(String filePath) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
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

            System.out.println("Statistiques exportées vers: " + filePath);
            return true;
        } catch (IOException e) {
            System.err.println("Erreur lors de l'export: " + e.getMessage());
            return false;
        }
    }

    /**
     * Importe les statistiques depuis un fichier CSV
     *
     * @param filePath Chemin complet du fichier à importer
     * @return true si l'import a réussi
     */
    public boolean importStatistics(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            // Ignorer l'en-tête
            String line = reader.readLine();

            // Lire les lignes de données
            while ((line = reader.readLine()) != null) {
                try {
                    System.out.println("Analyse de la ligne: " + line); // Debug

                    // Format attendu: Nom,Prénom,Pseudo,Parties jouées,Parties gagnées,Taux de victoire (peut être divisé en plusieurs colonnes),Score total,Score moyen
                    String[] data = line.split(",");

                    // Vérifier qu'il y a assez de données
                    if (data.length >= 7) {
                        String lastName = data[0].trim();
                        String firstName = data[1].trim();
                        String nickname = data[2].trim();

                        // Vérifier si le profil existe déjà
                        PlayerProfile existingProfile = getProfileByNickname(nickname);

                        try {
                            // Nettoyer et convertir les données numériques - peut-être à des positions différentes selon le format exact
                            int gamesPlayed = Integer.parseInt(data[3].trim());
                            int gamesWon = Integer.parseInt(data[4].trim());

                            // Pour le score total, chercher la première valeur numérique après le taux de victoire
                            int totalScore = 0;
                            for (int i = 5; i < data.length; i++) {
                                try {
                                    totalScore = Integer.parseInt(data[i].trim());
                                    break; // Prend la première valeur numérique valide
                                } catch (NumberFormatException ignored) {
                                    // Continue de chercher
                                }
                            }

                            if (existingProfile != null) {
                                // Si le profil existe déjà, mettre à jour ses statistiques
                                int currentGamesPlayed = existingProfile.getGamesPlayed();
                                int currentGamesWon = existingProfile.getGamesWon();
                                int currentTotalScore = existingProfile.getTotalScore();

                                // Calculer les différences pour n'ajouter que les nouvelles statistiques
                                int newGamesPlayed = Math.max(0, gamesPlayed - currentGamesPlayed);
                                int newGamesWon = Math.max(0, gamesWon - currentGamesWon);
                                int newTotalScore = Math.max(0, totalScore - currentTotalScore);

                                // Si des nouvelles parties ont été jouées
                                if (newGamesPlayed > 0) {
                                    // Mettre à jour pour les parties gagnées
                                    for (int i = 0; i < newGamesWon; i++) {
                                        existingProfile.updateStats(true,
                                            newGamesPlayed > 0 ? newTotalScore / newGamesPlayed : 0);
                                    }

                                    // Et pour les parties perdues
                                    for (int i = 0; i < newGamesPlayed - newGamesWon; i++) {
                                        existingProfile.updateStats(false, 0);
                                    }
                                }
                            } else {
                                // Créer un nouveau profil
                                PlayerProfile newProfile = new PlayerProfile(firstName, lastName, nickname);

                                // Ajouter le profil à la liste
                                profiles.add(newProfile);

                                // Mettre à jour les statistiques
                                for (int i = 0; i < gamesWon; i++) {
                                    newProfile.updateStats(true,
                                        gamesPlayed > 0 ? totalScore / gamesPlayed : 0);
                                }

                                for (int i = 0; i < gamesPlayed - gamesWon; i++) {
                                    newProfile.updateStats(false, 0);
                                }
                            }

                            System.out.println("Profil importé avec succès: " + nickname);
                        } catch (NumberFormatException e) {
                            System.err.println("Erreur lors de la conversion des statistiques pour " + nickname + ": " + e.getMessage());
                        }
                    } else {
                        System.err.println("Format de ligne invalide (pas assez de colonnes): " + line);
                    }
                } catch (Exception e) {
                    // Afficher un message d'erreur détaillé mais continuer avec les autres lignes
                    System.err.println("Erreur lors de l'analyse de la ligne: " + line);
                    System.err.println("Détail de l'erreur: " + e.getMessage());
                }
            }

            // Sauvegarder les modifications
            saveProfiles();

            System.out.println("Statistiques importées depuis: " + filePath);
            return true;
        } catch (IOException e) {
            System.err.println("Erreur lors de l'import: " + e.getMessage());
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