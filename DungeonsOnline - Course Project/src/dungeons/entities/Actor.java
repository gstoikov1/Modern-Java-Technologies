package dungeons.entities;

import dungeons.entities.position.GamePosition;

public interface Actor {

    GamePosition getGamePosition();

    int getCurrHealth();

    int getTotalHealth();

    boolean isDead();

    void setCurrHealth(int newCurrHealth);

    default void checkHealth() {
        setCurrHealth(Math.max(getCurrHealth(), 0));
    }

    int getAttack();

    int getDefence();

    int getTotalDefence();

}
