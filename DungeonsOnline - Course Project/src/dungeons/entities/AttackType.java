package dungeons.entities;

public enum AttackType {
    MELEE("Melee"),
    SPELL("Spell");

    private final String type;

    AttackType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
