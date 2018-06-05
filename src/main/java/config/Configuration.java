package config;

/**
 * This is the main configuration file for the application.
 */
public enum Configuration {
    instance;

    // JPA persistence unit
    public static final String PERSISTENCE_UNIT_NAME = "GameUnit";

    // Time interval after which new questions are fetched (in minutes)
    public static final int UPDATE_INTERVAL = 60;

    /* Recommended:
       - "BEST" to compare with the actual website. Few questions per hour, you may use a high update interval, e.g. 60
       - "NEW" many questions per hour, use a low update interval, e.g. 5. Questions might have poor quality. */
    public static final RedditSortBy REDDIT_SORT_BY = RedditSortBy.BEST;

    // Do not change - AI input sequence length
    // only needed for testing different AI models.
    public static final int SEQUENCE_LENGTH = 100;
}
