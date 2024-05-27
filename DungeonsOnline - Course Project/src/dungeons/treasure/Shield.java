package dungeons.treasure;

import dungeons.entities.position.GamePosition;

public class Shield extends Treasure {

    private final int damage;
    private final int defence;
    private final String name;

    private static final int DEFENCE_PER_LEVEL_MULTIPLIER = 10;
    private static final int DAMAGE_PER_LEVEL_MULTIPLIER = 1;

    public Shield(int level, GamePosition gamePosition) {
        super(level, gamePosition);
        damage = DAMAGE_PER_LEVEL_MULTIPLIER * level;
        defence = DEFENCE_PER_LEVEL_MULTIPLIER * level;
        name = super.getType() + " Shield";
    }

    public int getDamage() {
        return damage;
    }

    public int getDefence() {
        return defence;
    }

    public String getShieldName() {
        return name;
    }

    @Override
    public String toString() {
        return "Name: " + getShieldName() + " Level:" + getLevel() + " Damage:" + getDamage() + " Defence:" +
            getDefence();
    }

}
