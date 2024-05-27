package dungeons.entities.monster;

import dungeons.entities.Actor;
import dungeons.entities.position.GamePosition;

public class Monster implements Actor {
    private static final int STARTING_ATTACK = 10;
    private static final int STARTING_HEALTH = 10;
    private static final int STARTING_DEFENCE = 10;

    private static final double ATTACK_MULTIPLIER_PER_LEVEL = 1.2;
    private static final double DAMAGE_MITIGATE_PER_DEFENCE = 0.1;

    private static final double MINION_KILL_XP = 0.2;
    private static final double BEAST_KILL_XP = 0.5;
    private static final double UNDEAD_KILL_XP = 1.0;
    private static final double DRAGON_KILL_XP = 1.5;

    private static final int MINION_LEVEL = 5;
    private static final int BEAST_LEVEL = 10;
    private static final int UNDEAD_LEVEL = 15;

    private final GamePosition gamePosition;
    private final MonsterType monsterType;

    private final int attack;
    private final int totalHealth;
    private int currHealth;
    private final int defence;

    public Monster(int level, GamePosition gamePosition) {
        this.gamePosition = gamePosition;
        this.attack = (int) (STARTING_ATTACK + (ATTACK_MULTIPLIER_PER_LEVEL * (level - 1)));
        totalHealth = STARTING_HEALTH * level;
        defence = STARTING_DEFENCE * level;
        currHealth = totalHealth;
        if (level <= MINION_LEVEL) {
            monsterType = MonsterType.MINION;
        } else if (level <= BEAST_LEVEL) {
            monsterType = MonsterType.BEAST;
        } else if (level <= UNDEAD_LEVEL) {
            monsterType = MonsterType.UNDEAD;
        } else {
            monsterType = MonsterType.DRAGON;
        }
    }

    public char getCharForMonster() {
        return switch (monsterType) {
            case MINION -> 'M';
            case BEAST -> 'B';
            case UNDEAD -> 'U';
            case DRAGON -> 'D';
        };
    }

    public int getAttack() {
        return attack;
    }

    public double getXpForMonsterKill() {
        return switch (monsterType) {

            case MINION -> MINION_KILL_XP;
            case BEAST -> BEAST_KILL_XP;
            case UNDEAD -> UNDEAD_KILL_XP;
            case DRAGON -> DRAGON_KILL_XP;
        };
    }

    public boolean isDead() {
        return currHealth <= 0;
    }

    @Override
    public void setCurrHealth(int newCurrHealth) {
        currHealth = newCurrHealth;
    }

    @Override
    public GamePosition getGamePosition() {
        return gamePosition;
    }

    @Override
    public int getCurrHealth() {
        return currHealth;
    }

    @Override
    public int getTotalHealth() {
        return totalHealth;
    }

    @Override
    public int getDefence() {
        return defence;
    }

    @Override
    public int getTotalDefence() {
        return (int) (defence * DAMAGE_MITIGATE_PER_DEFENCE);
    }
}
