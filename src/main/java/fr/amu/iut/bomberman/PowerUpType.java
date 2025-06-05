package fr.amu.iut.bomberman;

public enum PowerUpType {
    EXTRA_BOMB("Bombe +1", 1),
    INCREASED_RANGE("Portée +1", 1),
    SPEED_BOOST("Vitesse +1", 1),
    EXTRA_LIFE("Vie +1", 1);

    private final String description;
    private final int value;

    PowerUpType(String description, int value) {
        this.description = description;
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public int getValue() {
        return value;
    }
}
