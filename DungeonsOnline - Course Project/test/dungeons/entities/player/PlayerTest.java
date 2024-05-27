package dungeons.entities.player;

import dungeons.entities.monster.Monster;
import dungeons.entities.position.GamePosition;
import dungeons.treasure.Shield;
import dungeons.treasure.Staff;
import dungeons.treasure.Sword;
import dungeons.treasure.Treasure;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PlayerTest {
    @Test
    void testMoveForward() {
        Player player = new Player();
        player.setPlayerPosition(new GamePosition(1, 1));
        player.move('w');
        GamePosition expectedGamePosition = new GamePosition(1, 0);
        assertEquals(expectedGamePosition, player.getGamePosition(),
            "\"w\" should move the player to y - 1");
    }

    @Test
    void testMoveRight() {
        Player player = new Player();
        player.setPlayerPosition(new GamePosition(1, 1));
        player.move('d');
        GamePosition expectedGamePosition = new GamePosition(2, 1);
        assertEquals(expectedGamePosition, player.getGamePosition(),
            "\"d\" should move the player to x + 1");
    }

    @Test
    void testPlayerIsDeadTrue() {
        Player player = new Player();
        player.setCurrHealth(0);
        assertTrue(player.isDead(),
            "Player with health == 0 should be considered dead");
    }

    @Test
    void testPlayerIsDeadFalse() {
        Player player = new Player();
        player.setCurrHealth(40);
        assertFalse(player.isDead(),
            "Player with health > 0 should not be considered dead");
    }

    @Test
    void testPlayerMoveWhileIsDead() {
        Player player = new Player();
        player.setPlayerPosition(new GamePosition(1, 1));
        player.setCurrHealth(0);
        player.move('d');
        assertEquals(new GamePosition(1, 1), player.getGamePosition(),
            "Player should not be able to move while is dead");
    }

    @Test
    void testPutTreasure() {
        Treasure treasure = new Sword(1, null);
        Player player = new Player();
        player.putTreasure(treasure);
        assertEquals(player.getInventory().getTreasureAtIndex(0), treasure,
            "putTreasure() should put the treasure in the inventory of the player should there be enough space");
    }

    @Test
    void testPlayerRespawn() {
        Player player = new Player();
        player.setCurrHealth(0);
        player.respawn();
        assertEquals(player.getCurrHealth(), player.getTotalHealth(),
            "respawn() should return the player to their total health");
    }

    @Test
    void testPlayerRespawnRemoveTreasureFromInventory() {
        Treasure treasure = new Sword(1, null);
        Player player = new Player();
        player.putTreasure(treasure);
        player.setCurrHealth(0);
        player.respawn();
        assertTrue(player.getInventory().isEmpty(),
            "respawn() should remove a treasure from player's inventory");
    }

    @Test
    void testPlayerTotalDamageAttack() {
        Sword sword = new Sword(10, null);
        Player player = new Player();
        player.putTreasure(sword);
        int attackBeforeEquippingSword = player.getTotalDamageAttack();
        player.equipTreasureAtIndex(0);
        int attackAfterEquippingSword = player.getTotalDamageAttack();
        assertTrue(attackAfterEquippingSword > attackBeforeEquippingSword,
            "Having an equipped sword should increase the total damage done");
    }

    @Test
    void testPlayerTotalSpellDamage() {
        Staff staff = new Staff(10, null);
        Player player = new Player();
        player.putTreasure(staff);
        int spellDamageBeforeEquippingStaff = player.getTotalSpellDamage();
        player.equipTreasureAtIndex(0);
        int spellDamageAfterEquippingStaff = player.getTotalSpellDamage();
        assertTrue(spellDamageAfterEquippingStaff > spellDamageBeforeEquippingStaff,
            "Having an equipped staff should increase the total spell damage done");
    }

    @Test
    void testPlayerEquippedShield() {
        Shield shield = new Shield(10, null);
        Player player = new Player();
        player.putTreasure(shield);
        int defenceBeforeEquippingShield = player.getTotalDefence();
        player.equipTreasureAtIndex(0);
        int defenceAfterEquippingShield = player.getTotalSpellDamage();
        assertTrue(defenceAfterEquippingShield > defenceBeforeEquippingShield,
            "Having an equipped shiled should increase the total defence");
    }

    @Test
    void testGetDistanceBetweenPlayers() {
        Player player1 = new Player();
        player1.setPlayerPosition(new GamePosition(0, 0));
        Player player2 = new Player();
        player2.setPlayerPosition(new GamePosition(3, 0));
        assertEquals(3, player1.getGamePosition().getDistance(player2.getGamePosition()),
            "Distance between players([0,0] and [0,3]) should be 3");
    }

    @Test
    void testAttackPlayerInRangeMelee() {
        //assuming RANGE_FOR_MELEE_ATTACK > 0;
        Player attackingPlayer = new Player();
        Player defendingPlayer = new Player();
        attackingPlayer.setPlayerPosition(new GamePosition(0, 0));
        defendingPlayer.setPlayerPosition(new GamePosition(1, 0));
        attackingPlayer.attackActorMelee(defendingPlayer);
        assertTrue(defendingPlayer.getCurrHealth() < defendingPlayer.getTotalHealth(),
            "Attack between players should be possible should they be in RANGE_FOR_MELEE_ATTACK");
    }

    @Test
    void testAttackPlayerNotInRangeMelee() {
        //assuming RANGE_FOR_MELEE_ATTACK < 3;
        Player attackingPlayer = new Player();
        Player defendingPlayer = new Player();
        attackingPlayer.setPlayerPosition(new GamePosition(0, 0));
        defendingPlayer.setPlayerPosition(new GamePosition(3, 0));
        attackingPlayer.attackActorMelee(defendingPlayer);
        assertEquals(defendingPlayer.getCurrHealth(), defendingPlayer.getTotalHealth(),
            "Attack between players should not be possible should they not be in RANGE_FOR_MELEE_ATTACK");
    }

    @Test
    void testAttackPlayerInRangeSpell() {
        //assuming RANGE_FOR_SPELL_ATTACK > 1;
        Player attackingPlayer = new Player();
        Player defendingPlayer = new Player();
        attackingPlayer.setPlayerPosition(new GamePosition(0, 0));
        defendingPlayer.setPlayerPosition(new GamePosition(2, 0));
        attackingPlayer.attackSpellPlayer(defendingPlayer);
        assertTrue(defendingPlayer.getCurrHealth() < defendingPlayer.getTotalHealth(),
            "Spell attack between players should be possible should they be in RANGE_FOR_SPELL_ATTACK");
    }

    @Test
    void testAttackPlayerNotInRangeSpell() {
        //assuming RANGE_FOR_SPELL_ATTACK < 2;
        Player attackingPlayer = new Player();
        Player defendingPlayer = new Player();
        attackingPlayer.setPlayerPosition(new GamePosition(0, 0));
        defendingPlayer.setPlayerPosition(new GamePosition(3, 0));
        attackingPlayer.attackSpellPlayer(defendingPlayer);
        assertEquals(defendingPlayer.getCurrHealth(), defendingPlayer.getTotalHealth(),
            "Spell attack between players should not be possible should they not be in RANGE_FOR_SPELL_ATTACK");
    }
    @Test
    void testAttackMonster() {
        Player player = new Player();
        Monster monster = new Monster(1, new GamePosition(0, 0));
        player.setPlayerPosition(new GamePosition(0, 0));
        player.attackActorMelee(monster);
        assertTrue(player.getCurrHealth() < player.getTotalHealth(),
            "Melee attacking a monster should make monster attack back");
    }
    @Test
    void testPlayerLevelUp() {
        Player player = new Player();
        int levelBeforeLevelingUp =  player.getLevel();
        player.levelUp();
        assertEquals(levelBeforeLevelingUp + 1, player.getLevel(),
            "levelUp() should increase player's level by one");
    }
}
