package dungeons.treasure;

import dungeons.entities.position.GamePosition;

public class Sword extends Treasure {
    private static final int DAMAGE_PER_LEVEL_MULTIPLIER = 2;

    private final int damage;
    private final String name;

    public Sword(int level, GamePosition gamePosition) {
        super(level, gamePosition);
        damage = DAMAGE_PER_LEVEL_MULTIPLIER * level;
        name = super.getType() + " Sword";
    }

    public int getDamage() {
        return damage;
    }

    public String getSwordName() {
        return name;
    }

    @Override
    public String toString() {
        return "Name: " + getSwordName() + " Level:" + getLevel() + " Damage:" + getDamage();
    }

}
