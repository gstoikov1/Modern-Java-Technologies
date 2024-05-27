package dungeons.entities.monster;

public enum MonsterType {
    MINION("Minion"),
    BEAST("Beast"),
    UNDEAD("Undead"),
    DRAGON("Dragon");

    private final String type;

    MonsterType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

}
