package dungeons.treasure;

public enum TreasureType {
    APPRENTICE("Apprentice"),
    JOURNEYMAN("Journeyman"),
    EXPERT("Expert"),
    ARTISAN("Artisan");

    private final String type;

    TreasureType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

}
