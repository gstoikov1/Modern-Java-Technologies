package dungeons.treasure;

import dungeons.entities.position.GamePosition;

public abstract class Treasure {
    private static final int APPRENTICE_LEVEL_TREASURE = 5;
    private static final int JOURNEYMAN_LEVEL_TREASURE = 10;
    private static final int EXPERT_LEVEL_TREASURE = 15;

    private final TreasureType type;
    private final int level;
    private final GamePosition gamePosition;

    public Treasure(int level, GamePosition gamePosition) {
        this.level = level;
        this.gamePosition = gamePosition;

        if (level <= APPRENTICE_LEVEL_TREASURE) {
            type = TreasureType.APPRENTICE;
        } else if (level <= JOURNEYMAN_LEVEL_TREASURE) {
            type = TreasureType.JOURNEYMAN;
        } else if (level <= EXPERT_LEVEL_TREASURE) {
            type = TreasureType.EXPERT;
        } else {
            type = TreasureType.ARTISAN;
        }
    }

    public int getLevel() {
        return level;
    }

    public GamePosition getGamePosition() {
        return gamePosition;
    }

    public String getType() {
        return type.getType();
    }

    public abstract int getDamage();
}
