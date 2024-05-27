package client.recipe.criterion;

public enum CuisineType {
    AMERICAN("american"),
    ASIAN("asian"),
    BRITISH("british"),
    CARIBBEAN("caribbean"),
    CENTRAL_EUROPE("central europe"),
    CHINESE("chinese"),
    EASTERN_EUROPE("eastern europe"),
    FRENCH("french"),
    GREEK("greek"),
    INDIAN("indian"),
    ITALIAN("italian"),
    JAPANESE("japanese"),
    KOREAN("korean"),
    KOSHER("kosher"),
    MEDITERRANEAN("mediterranean"),
    MEXICAN("mexican"),
    MIDDLE_EASTERN("middle eastern"),
    NORDIC("nordic"),
    SOUTH_AMERICAN("south american"),
    SOUTH_EAST_ASIAN("south east asian"),
    WORLD("world");

    private final String type;

    CuisineType(String type) {
        this.type = type;
    }

    public String getValue() {
        return type;
    }
}
