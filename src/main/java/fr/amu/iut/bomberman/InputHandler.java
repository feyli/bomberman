package fr.amu.iut.bomberman;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.HashMap;
import java.util.Map;

public class InputHandler {
    private final GameController controller;
    private final Map<KeyCode, PlayerAction> keyBindings;

    // Classe interne pour représenter une action de joueur
    private static class PlayerAction {
        public final int playerId;
        public final ActionType actionType;
        public final Direction direction;

        public PlayerAction(int playerId, ActionType actionType) {
            this.playerId = playerId;
            this.actionType = actionType;
            this.direction = null;
        }

        public PlayerAction(int playerId, ActionType actionType, Direction direction) {
            this.playerId = playerId;
            this.actionType = actionType;
            this.direction = direction;
        }
    }

    private enum ActionType {
        MOVE, PLACE_BOMB
    }

    public InputHandler(GameController controller) {
        this.controller = controller;
        this.keyBindings = new HashMap<>();
        setupKeyBindings();
    }

    private void setupKeyBindings() {
        // Joueur 1: WASD + Space
        keyBindings.put(KeyCode.Z, new PlayerAction(1, ActionType.MOVE, Direction.UP));
        keyBindings.put(KeyCode.Q, new PlayerAction(1, ActionType.MOVE, Direction.LEFT));
        keyBindings.put(KeyCode.S, new PlayerAction(1, ActionType.MOVE, Direction.DOWN));
        keyBindings.put(KeyCode.D, new PlayerAction(1, ActionType.MOVE, Direction.RIGHT));
        keyBindings.put(KeyCode.SPACE, new PlayerAction(1, ActionType.PLACE_BOMB));

        // Joueur 2: Flèches + Enter
        keyBindings.put(KeyCode.UP, new PlayerAction(2, ActionType.MOVE, Direction.UP));
        keyBindings.put(KeyCode.LEFT, new PlayerAction(2, ActionType.MOVE, Direction.LEFT));
        keyBindings.put(KeyCode.DOWN, new PlayerAction(2, ActionType.MOVE, Direction.DOWN));
        keyBindings.put(KeyCode.RIGHT, new PlayerAction(2, ActionType.MOVE, Direction.RIGHT));
        keyBindings.put(KeyCode.ENTER, new PlayerAction(2, ActionType.PLACE_BOMB));

        // Joueur 3: IJKL + U
        keyBindings.put(KeyCode.I, new PlayerAction(3, ActionType.MOVE, Direction.UP));
        keyBindings.put(KeyCode.J, new PlayerAction(3, ActionType.MOVE, Direction.LEFT));
        keyBindings.put(KeyCode.K, new PlayerAction(3, ActionType.MOVE, Direction.DOWN));
        keyBindings.put(KeyCode.L, new PlayerAction(3, ActionType.MOVE, Direction.RIGHT));
        keyBindings.put(KeyCode.U, new PlayerAction(3, ActionType.PLACE_BOMB));

        // Joueur 4: Numpad + 0
        keyBindings.put(KeyCode.NUMPAD8, new PlayerAction(4, ActionType.MOVE, Direction.UP));
        keyBindings.put(KeyCode.NUMPAD4, new PlayerAction(4, ActionType.MOVE, Direction.LEFT));
        keyBindings.put(KeyCode.NUMPAD5, new PlayerAction(4, ActionType.MOVE, Direction.DOWN));
        keyBindings.put(KeyCode.NUMPAD6, new PlayerAction(4, ActionType.MOVE, Direction.RIGHT));
        keyBindings.put(KeyCode.NUMPAD0, new PlayerAction(4, ActionType.PLACE_BOMB));
    }

    public void handleKeyPressed(KeyEvent event) {
        KeyCode keyCode = event.getCode();

        // Touches globales du jeu
        switch (keyCode) {
            case ESCAPE:
                handleEscape();
                return;
            case P:
                controller.pauseGame();
                return;
            case R:
                if (controller.getGame().getCurrentState() == GameState.GAME_OVER) {
                    controller.startNewGame();
                }
                return;
            case M:
                if (controller.getGame().getCurrentState() == GameState.GAME_OVER ||
                        controller.getGame().getCurrentState() == GameState.PAUSED) {
                    controller.returnToMenu();
                }
                return;
        }

        // Actions des joueurs
        PlayerAction action = keyBindings.get(keyCode);
        if (action != null) {
            executePlayerAction(action);
        }
    }

    private void handleEscape() {
        GameState currentState = controller.getGame().getCurrentState();

        switch (currentState) {
            case PLAYING:
                controller.pauseGame();
                break;
            case PAUSED:
                controller.pauseGame(); // Reprendre le jeu
                break;
            case GAME_OVER:
                controller.returnToMenu();
                break;
            case MENU:
                // Peut-être quitter l'application
                System.exit(0);
                break;
        }
    }

    private void executePlayerAction(PlayerAction action) {
        switch (action.actionType) {
            case MOVE:
                controller.movePlayer(action.playerId, action.direction);
                break;
            case PLACE_BOMB:
                controller.playerPlaceBomb(action.playerId);
                break;
        }
    }

    public Map<KeyCode, String> getKeyBindingDescriptions() {
        Map<KeyCode, String> descriptions = new HashMap<>();

        descriptions.put(KeyCode.W, "Joueur 1 - Haut");
        descriptions.put(KeyCode.A, "Joueur 1 - Gauche");
        descriptions.put(KeyCode.S, "Joueur 1 - Bas");
        descriptions.put(KeyCode.D, "Joueur 1 - Droite");
        descriptions.put(KeyCode.SPACE, "Joueur 1 - Placer bombe");

        descriptions.put(KeyCode.UP, "Joueur 2 - Haut");
        descriptions.put(KeyCode.LEFT, "Joueur 2 - Gauche");
        descriptions.put(KeyCode.DOWN, "Joueur 2 - Bas");
        descriptions.put(KeyCode.RIGHT, "Joueur 2 - Droite");
        descriptions.put(KeyCode.ENTER, "Joueur 2 - Placer bombe");

        descriptions.put(KeyCode.I, "Joueur 3 - Haut");
        descriptions.put(KeyCode.J, "Joueur 3 - Gauche");
        descriptions.put(KeyCode.K, "Joueur 3 - Bas");
        descriptions.put(KeyCode.L, "Joueur 3 - Droite");
        descriptions.put(KeyCode.U, "Joueur 3 - Placer bombe");

        descriptions.put(KeyCode.NUMPAD8, "Joueur 4 - Haut");
        descriptions.put(KeyCode.NUMPAD4, "Joueur 4 - Gauche");
        descriptions.put(KeyCode.NUMPAD5, "Joueur 4 - Bas");
        descriptions.put(KeyCode.NUMPAD6, "Joueur 4 - Droite");
        descriptions.put(KeyCode.NUMPAD0, "Joueur 4 - Placer bombe");

        descriptions.put(KeyCode.ESCAPE, "Menu/Pause");
        descriptions.put(KeyCode.P, "Pause");
        descriptions.put(KeyCode.R, "Redémarrer");
        descriptions.put(KeyCode.M, "Menu principal");

        return descriptions;
    }
}
