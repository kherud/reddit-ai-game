package config;

/**
 * This enum is used to select different sorting methods
 * for retrieving content from the official wesite www.reddit.com
 */
public enum RedditSortBy {
    BEST("best"),
    TOP("top"),
    NEW("new"),
    CONTROVERSIAL("controversial"),
    HOT("hot");

    private final String sortString;

    RedditSortBy(String sortString){
        this.sortString = sortString;
    }

    @Override
    public String toString(){
        return this.sortString;
    }
}
