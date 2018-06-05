package config;

/**
 * This enum is used to specify different game difficulties.
 */
public enum GameDifficulty {
    EASY("Easy", 4),
    NORMAL("Normal", 10),
    HARD("Hard", 27);

    private final String description;
    private final int choices;

    GameDifficulty(String description, int choices){
        this.description = description;
        this.choices = choices;
    }

    public int getChoices() {
        return choices;
    }

    public String getDescription() {
        return description;
    }
}
