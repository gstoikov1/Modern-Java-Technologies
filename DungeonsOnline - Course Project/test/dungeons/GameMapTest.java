package dungeons;

import dungeons.entities.monster.Monster;
import dungeons.entities.player.Player;
import dungeons.entities.position.GamePosition;
import dungeons.exception.PlayerCharAlreadyExistsException;
import dungeons.treasure.Sword;
import dungeons.treasure.Treasure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GameMapTest {
    private static char[][] testMap = {
        {'.', '.', '.', '.', '.'},
        {'#', '.', '.', '.', '.'},
        {'#', '.', '.', '.', '.'},
        {'#', '.', '.', '.', '.'},
    };

    private static GameMap testGameMap;

    private static char[][] deepCopyCharArray(char[][] original) {
        int rows = original.length;
        int cols = original[0].length;

        char[][] copy = new char[rows][cols];

        for (int i = 0; i < rows; i++) {
            System.arraycopy(original[i], 0, copy[i], 0, cols);
        }

        return copy;
    }

    @BeforeEach
    void init() {
        Player.setPlayersCount(0);
        testGameMap = new GameMap(deepCopyCharArray(testMap));
    }

    @Test
    void testConnectPlayer() throws PlayerCharAlreadyExistsException {
        Player player = new Player();
        testGameMap.connectPlayer(player);
        assertTrue(testGameMap.getPlayers().contains(player),
            "connectPlayer should correctly add player to the map");
    }

    @Test
    void testIsEmptySpace() {
        testGameMap.setPosition(new GamePosition(1, 1), '.');
        assertTrue(testGameMap.isEmptySpace(1, 1),
            " '.' should be considered an empty space");
    }

    @Test
    void testIsValidGamePositionTrue() {
        GamePosition gamePosition = new GamePosition(1, 1);
        assertTrue(testGameMap.isValidGamePosition(gamePosition),
            "isValidGamePosition() should return true for a position in bounds of the map");

    }

    @Test
    void testIsValidGamePositionFalse() {
        GamePosition gamePosition = new GamePosition(-1, 1);
        assertFalse(testGameMap.isValidGamePosition(gamePosition),
            "isValidGamePosition() should return false for a position out of bounds of the map");
    }

    @Test
    void testMovePlayerToEmptySpace() throws PlayerCharAlreadyExistsException {
        Player player = new Player();
        testGameMap.connectPlayer(player);
        player.setPlayerPosition(new GamePosition(1, 1));
        testGameMap.setPosition(new GamePosition(1, 0), '.');
        testGameMap.movePlayer(player, 'w');
        assertEquals(new GamePosition(1, 0), player.getGamePosition());
    }

    @Test
    void testConnectMultiplePlayers() throws PlayerCharAlreadyExistsException {

        testGameMap.connectPlayer(new Player());
        testGameMap.connectPlayer(new Player());
        testGameMap.connectPlayer(new Player());

        assertEquals(3, testGameMap.getPlayers().size(),
            "Multiple players should be connected correctly");
    }

    @Test
    void testDisconnectPlayers() throws PlayerCharAlreadyExistsException {
        GameMap gameMap = new GameMap();
        gameMap.connectPlayer(new Player());
        gameMap.connectPlayer(new Player());
        Player playerToBeDisconnected = new Player();
        gameMap.connectPlayer(playerToBeDisconnected);
        gameMap.disconnectPlayer(playerToBeDisconnected);
        assertEquals(2, gameMap.getPlayers().size(),
            "Players should be able to be disconnected correctly");
    }

    @Test
    void testCheckForDeadMonster() {
        Monster monster = testGameMap.getMonsters().get(0);
        monster.setCurrHealth(0);
        testGameMap.checkForDeadMonster();
        assertFalse(testGameMap.getMonsters().contains(monster),
            "Should there be a dead monster it should be remove and replaced");
    }

    @Test
    void testHandleActionRespawn() throws PlayerCharAlreadyExistsException {
        Player player = new Player();
        testGameMap.connectPlayer(player);
        player.setCurrHealth(0);
        String action = "respawn";
        testGameMap.handleAction(player, action);
        assertEquals(player.getCurrHealth(), player.getTotalHealth(),
            "Dead Player prompting respawn should get player to full health");

    }

    @Test
    void testHandleActionMove() {
        Player player = new Player();
        testGameMap.setPosition(new GamePosition(0, 0), '.');
        player.setPlayerPosition(new GamePosition(0, 1));
        String action = "w";
        testGameMap.handleAction(player, action);
        GamePosition expectedGamePosition = new GamePosition(0, 0);
        assertEquals(expectedGamePosition, player.getGamePosition(),
            "GameMap should handle move player when prompt is a valid one");
    }

    @Test
    void testHandleActionAttack() throws PlayerCharAlreadyExistsException {
        Player attackingPlayer = new Player();
        Player defendingPlayer = new Player();
        testGameMap.connectPlayer(attackingPlayer);
        testGameMap.connectPlayer(defendingPlayer);
        char defendingPlayerChar = defendingPlayer.getPlayerChar();
        String action = "attack melee " + defendingPlayerChar;
        attackingPlayer.setPlayerPosition(new GamePosition(0, 0));
        defendingPlayer.setPlayerPosition(new GamePosition(0, 0));
        testGameMap.handleAction(attackingPlayer, action);
        assertTrue(defendingPlayer.getCurrHealth() < defendingPlayer.getTotalHealth(),
            "Attack action should be handled correctly when players are in enough range");
    }

    @Test
    void testHandleActionTrade() throws PlayerCharAlreadyExistsException {
        Player playerToSend = new Player();
        Player playerToReceive = new Player();
        testGameMap.connectPlayer(playerToSend);
        testGameMap.connectPlayer(playerToReceive);
        char playerToReceiveChar = playerToReceive.getPlayerChar();
        Treasure treasureToBeTraded = new Sword(1, null);
        playerToSend.putTreasure(treasureToBeTraded);
        String action = "trade " + playerToReceiveChar + " 0";
        playerToSend.setPlayerPosition(new GamePosition(0, 0));
        playerToReceive.setPlayerPosition(new GamePosition(0, 0));
        testGameMap.handleAction(playerToSend, action);
        assertTrue(!playerToSend.getInventory().getTreasures().contains(treasureToBeTraded) &&
                playerToReceive.getInventory().getTreasures().contains(treasureToBeTraded),
            "After successful trade treasure is remove from sending player's inventory and is added to receicing player's inventory");
    }
}
