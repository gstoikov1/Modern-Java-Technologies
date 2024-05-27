package dungeons.entities.player;

import dungeons.entities.Actor;
import dungeons.entities.position.GamePosition;
import dungeons.treasure.Treasure;
import dungeons.treasure.Staff;
import dungeons.entities.monster.Monster;
import dungeons.treasure.Shield;

public class Player implements Actor {
    private static final int COUNTING_SYSTEM = 10;

    private static final int STARTING_HEALTH = 100;
    private static final int STARTING_MANA = 124;
    private static final int STARTING_LEVEL = 1;
    private static final int STARTING_ATTACK = 50;
    private static final int STARTING_DEFENCE = 50;
    private static final int STARTING_SPELL_DAMAGE = 50;
    private static final int ATTACK_PER_LEVEL = 5;
    private static final int DEFENCE_PER_LEVEL = 5;
    private static final int MANA_PER_LEVEL = 10;
    private static final int HEALTH_PER_LEVEL = 10;
    private static final int SPELL_DAMAGE_PER_LEVEL = 5;
    private static final double XP_PER_TREASURE_PICK_UP = 0.1;
    private static final double DAMAGE_MITIGATE_PER_ARMOR = 0.1;
    private static final int RANGE_FOR_MELEE_ATTACK = 1;
    private static final int RANGE_FOR_SPELL_ATTACK = 2;
    private static final int RANGE_FOR_TRADING = 1;
    private static final int MANA_FOR_SPELL_ATTACK = 20;

    private static final double ROUNDING = 100;

    private static int playersCount = 0;

    private final int playerChar;
    private final Inventory inventory;
    private GamePosition gamePosition;
    private int totalHealth;
    private int currHealth;
    private int totalMana;
    private int currMana;
    private int level;
    private double currLevel;
    private int attack;
    private int defence;
    private Treasure equippedTreasure;
    private int spellDamage;

    public Player() {
        playerChar = playersCount++;
        totalHealth = STARTING_HEALTH;
        totalMana = STARTING_MANA;
        currMana = totalMana;
        currHealth = totalHealth;
        level = STARTING_LEVEL;
        currLevel = STARTING_LEVEL;
        attack = STARTING_ATTACK;
        defence = STARTING_DEFENCE;
        inventory = new Inventory();
        equippedTreasure = null;
        spellDamage = STARTING_SPELL_DAMAGE;
    }

    public void move(char way) {
        if (isDead()) {
            return;
        }
        int x = getGamePosition().x();
        int y = getGamePosition().y();
        switch (way) {
            case 'w' -> y--;
            case 's' -> y++;
            case 'a' -> x--;
            case 'd' -> x++;
        }
        gamePosition = new GamePosition(x, y);
    }

    public void unequipTreasure() {
        equippedTreasure = null;
    }

    public void removeTreasureAtIndex(int index) {
        inventory.removeTreasureAtIndex(index);
    }

    public void equipTreasureAtIndex(int index) {
        if (hasEquippedTreasure() && equippedTreasure instanceof Staff) {
            int staffMana = ((Staff) equippedTreasure).getMana();
            totalMana -= staffMana;
            currMana -= staffMana;
        }
        Treasure toEquip = inventory.getTreasureAtIndex(index);

        if (toEquip != null) {
            equippedTreasure = toEquip;

            if (equippedTreasure instanceof Staff) {
                int staffMana = ((Staff) equippedTreasure).getMana();
                totalMana += staffMana;
                currMana += staffMana;
            }
        }
    }

    public void getXpForTreasurePickUp() {
        currLevel += XP_PER_TREASURE_PICK_UP;
        checkLevelUp();
    }

    public void attackActorMelee(Actor actor) {
        if (isDead()) {
            return;
        }

        if (this.equals(actor)) {
            return;
        }

        if (inRangeForMeleeAttack(actor)) {
            int totalAttack = getTotalDamageAttack();
            int currHealth = actor.getCurrHealth();

            int mitigatedDamage = actor.getTotalDefence();
            int actualAttack = mitigatedDamage > totalAttack ? 0 : totalAttack - mitigatedDamage;
            actor.setCurrHealth(currHealth - actualAttack);
        }
        actor.checkHealth();
        if (actor instanceof Monster) {
            currHealth -= actor.getAttack();
            if (actor.isDead()) {
                currLevel += ((Monster) actor).getXpForMonsterKill();
                checkLevelUp();
            }
        }

        checkHealth();
    }

    public void tradeTreasureAtIndexToPlayer(int index, Player player) {
        if (player == null) {
            throw new IllegalArgumentException("player was null");
        }

        if (!inRangeForTrading(player)) {
            return;
        }

        if (player.inventory.isFull()) {
            return;
        }

        Treasure treasureToBeDonated = inventory.getTreasureAtIndex(index);
        if (treasureToBeDonated != null) {
            removeTreasureAtIndex(index);
            player.putTreasure(treasureToBeDonated);
            if (treasureToBeDonated.equals(equippedTreasure)) {
                equippedTreasure = null;
            }
        }
    }

    public void putTreasure(Treasure treasure) {
        if (!inventoryIsFull()) {
            inventory.putTreasure(treasure);
        }
    }

    public char getPlayerChar() {
        return Character.forDigit(playerChar, COUNTING_SYSTEM);
    }

    public void respawn() {
        if (isDead()) {
            currHealth = totalHealth;
            removeRandomTreasure();
            if (!inventory.getTreasures().contains(equippedTreasure)) {
                equippedTreasure = null;
            }
        }
    }

    public boolean isDead() {
        return currHealth <= 0;
    }

    public int getTotalHealth() {
        return totalHealth;
    }

    public int getTotalMana() {
        return totalMana;
    }

    public int getCurrHealth() {
        return currHealth;
    }

    public void setPlayerPosition(GamePosition playerPosition) {
        this.gamePosition = playerPosition;
    }

    public int getLevel() {
        return level;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public boolean hasEquippedTreasure() {
        return equippedTreasure != null;
    }

    private void removeRandomTreasure() {
        inventory.removeRandomTreasure();
    }

    private boolean inventoryIsFull() {
        return inventory.isFull();
    }

    public void levelUp() {
        level++;
        attack += ATTACK_PER_LEVEL;
        spellDamage += SPELL_DAMAGE_PER_LEVEL;
        defence += DEFENCE_PER_LEVEL;
        totalMana += MANA_PER_LEVEL;
        currMana = totalMana;
        totalHealth += HEALTH_PER_LEVEL;
        currHealth = totalHealth;

    }

    private void checkLevelUp() {
        if (currLevel > level + 1) {
            levelUp();
        }
    }

    private boolean inRangeForMeleeAttack(Actor actor) {
        return this.gamePosition.getDistance(actor.getGamePosition()) <= RANGE_FOR_MELEE_ATTACK;
    }

    private boolean inRangeForTrading(Player player) {
        return this.gamePosition.getDistance(player.getGamePosition()) <= RANGE_FOR_TRADING;
    }

    private boolean inRageForSpellAttack(Player player) {
        return this.gamePosition.getDistance(player.getGamePosition()) <= RANGE_FOR_SPELL_ATTACK;

    }

    public int getTotalDefence() {
        int totalDefence = getDefence();
        if (this.equippedTreasure instanceof Shield) {
            totalDefence += ((Shield) this.equippedTreasure).getDefence();
        }
        return (int) (totalDefence * DAMAGE_MITIGATE_PER_ARMOR);
    }

    public int getTotalDamageAttack() {
        int totalAttack = this.attack;
        if (hasEquippedTreasure()) {
            totalAttack += equippedTreasure.getDamage();
        }
        return totalAttack;
    }

    public int getTotalSpellDamage() {
        int totalSpellAttack = spellDamage;
        if (equippedTreasure instanceof Staff s) {
            totalSpellAttack += s.getSpellDamage();
        }
        return totalSpellAttack;
    }

    public void attackSpellPlayer(Player player) {
        if (isDead() || !inRageForSpellAttack(player) || currMana < MANA_FOR_SPELL_ATTACK || this.equals(player)) {
            return;
        }
        int spellDamage = getTotalSpellDamage();

        player.setCurrHealth(player.getCurrHealth() - spellDamage);
        player.checkHealth();
        currMana -= MANA_FOR_SPELL_ATTACK;
    }

    @Override
    public String toString() {
        String treasure = "No equipped treasure";
        if (hasEquippedTreasure()) {
            treasure = equippedTreasure.toString();
        }
        return "Level:" + (Math.round(currLevel * ROUNDING) / ROUNDING) + "/" + (level + 1) + " | Health:" +
            currHealth + "/" + totalHealth +
            " | Mana:" +
            currMana + "/" +
            totalMana + System.lineSeparator() + inventory + System.lineSeparator() +
            "Equipped Treasure: " +
            treasure;
    }

    @Override
    public void setCurrHealth(int newCurrHealth) {
        currHealth = newCurrHealth;
    }

    @Override
    public void checkHealth() {
        Actor.super.checkHealth();
    }

    @Override
    public int getAttack() {
        return 0;
    }

    @Override
    public int getDefence() {
        return defence;
    }

    @Override
    public GamePosition getGamePosition() {
        return gamePosition;
    }

    public Treasure getEquippedTreasure() {
        return equippedTreasure;
    }

    public static void setPlayersCount(int count) {
        playersCount = count;
    }
}
