package dungeons;

import dungeons.entities.AttackType;
import dungeons.entities.monster.Monster;
import dungeons.entities.player.Player;
import dungeons.entities.position.GamePosition;
import dungeons.exception.PlayerCharAlreadyExistsException;
import dungeons.treasure.Shield;
import dungeons.treasure.Staff;
import dungeons.treasure.Sword;
import dungeons.treasure.Treasure;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class GameMap {
    private final int cols;
    private final int rows;

    private int treasureCount;
    private int monsterCount;

    private static final int MAX_TREASURE_COUNT = 5;
    private static final int MAX_MONSTER_COUNT = 5;

    private final List<Player> players;
    private final List<Treasure> treasures;
    private final List<Monster> monsters;

    private final Random random;

    private static final String MOVE_KEYS = "wasd";
    private static final int COUNTING_SYSTEM = 10;
    private static final int TREASURE_TYPES_COUNT = 3;

    private char[][] map = {
        {'#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#', '#'},
        {'#', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '#', '.', '.', '.', '#'},
        {'#', '.', '.', '.', '.', '.', '.', '#', '.', '.', '.', '.', '.', '#', '.', '.', '.', '#'},
        {'#', '.', '.', '.', '.', '.', '.', '#', '.', '.', '.', '.', '.', '#', '.', '.', '.', '#'},
        {'#', '.', '#', '.', '.', '.', '.', '#', '.', '.', '.', '.', '.', '#', '.', '.', '.', '#'},
        {'#', '.', '#', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '#', '.', '.', '.', '#'},
        {'#', '.', '#', '.', '.', '.', '.', '.', '.', '#', '.', '.', '#', '#', '.', '.', '.', '#'},
        {'#', '#', '.', '.', '.', '.', '.', '.', '.', '#', '.', '#', '#', '#', '.', '.', '.', '#'},
        {'#', '#', '.', '.', '.', '.', '.', '.', '#', '#', '.', '#', '#', '#', '.', '.', '.', '#'},
        {'#', '#', '.', '#', '.', '.', '.', '#', '#', '.', '.', '.', '.', '.', '.', '.', '.', '#'},
        {'#', '#', '#', '#', '#', '#', '#', '#', '#', '.', '.', '.', '.', '#', '.', '.', '.', '#'}
    };

    public GameMap() {
        cols = map[0].length;
        rows = map.length;
        treasureCount = 0;
        monsterCount = 0;
        monsters = new ArrayList<>();
        treasures = new ArrayList<>();
        players = new ArrayList<>();
        random = new Random();

        putTreasure();
        putMonster();
    }

    public GameMap(char[][] map) {
        this.map = map;
        cols = map[0].length;
        rows = map.length;
        treasureCount = 0;
        monsterCount = 0;
        monsters = new ArrayList<>();
        treasures = new ArrayList<>();
        players = new ArrayList<>();
        random = new Random();

        putTreasure();
        putMonster();

    }

    public void connectPlayer(Player newPlayer) throws PlayerCharAlreadyExistsException {
        if (newPlayer == null) {
            throw new IllegalArgumentException("newPlayer was null");
        }
        char newPlayerChar = newPlayer.getPlayerChar();

        if (getPlayerByChar(newPlayerChar) != null) {
            throw new PlayerCharAlreadyExistsException("Player with this char was already connected");
        }

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                if (isEmptySpace(x, y)) {
                    map[y][x] = newPlayerChar;
                    newPlayer.setPlayerPosition(new GamePosition(x, y));
                    players.add(newPlayer);
                    return;
                }
            }
        }
    }

    public void handleAction(Player player, String action) {
        if (player == null) {
            throw new IllegalArgumentException("Player was null");
        }

        if (action == null) {
            throw new IllegalArgumentException("Action was null");
        }

        String[] parts = action.split(" ");
        int len = action.length();

        if ("respawn".equals(action)) {
            respawnPlayer(player);
        } else if ("unequip".equals(action)) {
            player.unequipTreasure();
        } else if (parts[0].equals("drop") && parts.length > 1) {
            player.removeTreasureAtIndex(Integer.parseInt(parts[1]));
        } else if (len == 1 && MOVE_KEYS.contains(action)) {
            movePlayer(player, action.charAt(0));
        } else if (parts[0].equals("attack") && parts.length > 2) {
            handleAttack(player, parts);
        } else if (parts[0].equals("equip") && parts.length > 1) {
            player.equipTreasureAtIndex(Integer.parseInt(parts[1]));
        } else if (parts[0].equals("trade") && parts.length > 2) {
            handleTrade(player, parts);
        }
    }

    public void tradeTreasure(Player playerTrading, char charOfPlayerToBeTraded, int indexOfTreasureToBeTraded) {
        Player playerToBeTraded = getPlayerByChar(charOfPlayerToBeTraded);
        if (playerToBeTraded == null) {
            return;
        }
        playerTrading.tradeTreasureAtIndexToPlayer(indexOfTreasureToBeTraded, playerToBeTraded);
    }

    public void disconnectPlayer(Player player) {
        if (player == null) {
            throw new IllegalArgumentException("player was null");
        }
        GamePosition gamePosition = player.getGamePosition();
        map[gamePosition.y()][gamePosition.x()] = '.';
        players.remove(player);
    }

    public void broadcast(Selector selector) throws IOException {
        if (selector == null) {
            throw new IllegalArgumentException("selector was null");
        }

        Set<SelectionKey> keys = selector.keys();
        ByteBuffer mapInformation = mapToByteBuffer();
        for (SelectionKey key : keys) {
            if (key.isValid() && key.channel() instanceof SocketChannel channel) {

                Player currPlayer = (Player) key.attachment();
                byte[] bytes = currPlayer.toString().getBytes();
                ByteBuffer dataToSend =
                    ByteBuffer.allocate(mapInformation.capacity() + bytes.length);

                dataToSend.put(mapInformation.duplicate());

                dataToSend.put(bytes);
                dataToSend.flip();
                channel.write(dataToSend);
            }
        }
    }

    public void movePlayer(Player player, char way) {
        if (player.isDead()) {
            return;
        }
        GamePosition oldPlayerPosition = player.getGamePosition();
        GamePosition newGamePosition = getNewGamePosition(oldPlayerPosition, way);
        if (!isValidGamePosition(newGamePosition)) {
            return;
        }
        int newPlayerX = newGamePosition.x();
        int newPlayerY = newGamePosition.y();

        if (isEmptySpace(newPlayerX, newPlayerY)) {
            player.move(way);
        } else if (isTreasure(newPlayerX, newPlayerY) &&
            getTreasureAtPosition(new GamePosition(newPlayerX, newPlayerY)).getLevel() <= player.getLevel()) {
            player.move(way);
            GamePosition gamePosition = new GamePosition(newPlayerX, newPlayerY);
            Treasure treasure = getTreasureAtPosition(gamePosition);
            player.putTreasure(treasure);
            treasures.remove(treasure);
            treasureCount--;
            player.getXpForTreasurePickUp();
            putTreasure();
        }
        updatePlayerPosition(player, oldPlayerPosition, player.getGamePosition());
    }

    public boolean isEmptySpace(int x, int y) {
        return map[y][x] == '.';
    }

    public boolean isTreasure(int x, int y) {
        return map[y][x] == 'T';
    }

    public boolean isValidGamePosition(GamePosition gamePosition) {
        if (gamePosition == null) {
            return false;
        }
        int x = gamePosition.x();
        int y = gamePosition.y();
        return 0 <= x && x < cols && 0 <= y && y < rows;
    }

    public void checkForDeadMonster() {
        Monster deadMonster = null;
        for (Monster monster : monsters) {
            if (monster.isDead()) {
                GamePosition gamePosition = monster.getGamePosition();
                map[gamePosition.y()][gamePosition.x()] = '.';
                deadMonster = monster;
                break;
            }
        }
        if (deadMonster != null) {
            monsters.remove(deadMonster);
            monsterCount--;
            putMonster();
        }
    }

    public int getCols() {
        return cols;
    }

    public int getRows() {
        return rows;
    }

    public int getTreasureCount() {
        return treasureCount;
    }

    public int getMonsterCount() {
        return monsterCount;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public List<Treasure> getTreasures() {
        return treasures;
    }

    public List<Monster> getMonsters() {
        return monsters;
    }

    public Random getRandom() {
        return random;
    }

    public char[][] getMap() {
        return map;
    }

    public void setPosition(GamePosition gamePosition, char newChar) {
        if (!isValidGamePosition(gamePosition)) {
            return;
        }
        map[gamePosition.y()][gamePosition.x()] = newChar;

    }

    private void updatePlayerPosition(Player player, GamePosition oldPosition, GamePosition newPosition) {
        map[oldPosition.y()][oldPosition.x()] = '.';
        map[newPosition.y()][newPosition.x()] = player.getPlayerChar();
    }

    private ByteBuffer mapToByteBuffer() {

        ByteBuffer byteBuffer = ByteBuffer.allocate(Character.BYTES * (rows * cols + 2));
        byteBuffer.putChar((char) rows);
        byteBuffer.putChar((char) cols);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                byteBuffer.putChar(map[i][j]);
            }
        }
        byteBuffer.flip();
        return byteBuffer;
    }

    private void respawnPlayer(Player player) {
        player.respawn();
    }

    private void playerAttack(Player attackingPlayer, char defendingPlayerChar, AttackType attackType) {
        if (attackingPlayer.isDead()) {
            return;
        }

        Player toBeAttacked = getPlayerByChar(defendingPlayerChar);

        if (toBeAttacked == null) {
            return;
        }

        if (attackType == AttackType.MELEE) {
            attackingPlayer.attackActorMelee(toBeAttacked);
        } else if (attackType == AttackType.SPELL) {
            attackingPlayer.attackSpellPlayer(toBeAttacked);
        }
    }

    private void monsterAttack(Player attackingPlayer, char way) {
        GamePosition playerGamePosition = attackingPlayer.getGamePosition();

        GamePosition monsterGamePosition = getNewGamePosition(playerGamePosition, way);

        if (!isValidGamePosition(monsterGamePosition)) {
            return;
        }
        Monster monsterToAttack = getMonsterByPosition(monsterGamePosition);
        if (monsterToAttack == null) {
            return;
        }
        attackingPlayer.attackActorMelee(monsterToAttack);
        checkForDeadMonster();
    }

    private void putMonster() {
        int averageLevel = getAveragePlayerLevel();
        while (monsterCount < MAX_MONSTER_COUNT) {

            int randomX = random.nextInt(cols);
            int randomY = random.nextInt(rows);

            if (isEmptySpace(randomX, randomY)) {
                monsterCount++;
                GamePosition gamePosition = new GamePosition(randomX, randomY);
                Monster monster = new Monster(random.nextInt(averageLevel) + 1, gamePosition);
                monsters.add(monster);
                map[randomY][randomX] = monster.getCharForMonster();
            }
        }
    }

    private void putTreasure() {
        int averageLevel = getAveragePlayerLevel();
        while (treasureCount < MAX_TREASURE_COUNT) {

            int randomX = random.nextInt(cols);
            int randomY = random.nextInt(rows);
            if (isEmptySpace(randomX, randomY)) {
                treasureCount++;
                int randomWeapon = random.nextInt(TREASURE_TYPES_COUNT); //0 - Sword 1 - Staff 2 - Shield
                GamePosition gamePosition = new GamePosition(randomX, randomY);
                Treasure treasure;
                if (randomWeapon == 0) {
                    treasure = new Sword(random.nextInt(averageLevel) + 1, gamePosition);
                } else if (randomWeapon == 1) {
                    treasure = new Staff(random.nextInt(averageLevel) + 1, gamePosition);
                } else {
                    treasure = new Shield(random.nextInt(averageLevel) + 1, gamePosition);
                }
                treasures.add(treasure);
                map[randomY][randomX] = 'T';
            }
        }
    }

    private Treasure getTreasureAtPosition(GamePosition gamePosition) {
        if (gamePosition == null) {
            throw new IllegalArgumentException("gamePosition was null");
        }
        for (Treasure treasure : treasures) {
            if (treasure.getGamePosition().equals(gamePosition)) {
                return treasure;
            }
        }
        return null;
    }

    private int getAveragePlayerLevel() {
        if (players.isEmpty()) {
            return 1;
        }

        int average = 0;
        for (Player player : players) {
            average += player.getLevel();
        }
        return average / players.size();
    }

    private Monster getMonsterByPosition(GamePosition gamePosition) {
        if (gamePosition == null) {
            throw new IllegalArgumentException("gameposition was null");
        }
        for (Monster monster : monsters) {
            if (monster.getGamePosition().equals(gamePosition)) {
                return monster;
            }
        }
        return null;
    }

    private GamePosition getNewGamePosition(GamePosition oldGamePosition, char way) {
        int x = oldGamePosition.x();
        int y = oldGamePosition.y();
        return switch (way) {
            case 'w' -> new GamePosition(x, y - 1);
            case 's' -> new GamePosition(x, y + 1);
            case 'a' -> new GamePosition(x - 1, y);
            case 'd' -> new GamePosition(x + 1, y);
            default -> null;
        };
    }

    private Player getPlayerByChar(char c) {
        for (Player player : players) {
            if (player.getPlayerChar() == c) {
                return player;
            }
        }
        return null;
    }

    private void handleAttack(Player player, String[] parts) {
        char target = parts[2].charAt(0);

        if (parts[1].equals("melee")) {
            playerAttack(player, target, AttackType.MELEE);
        } else if (parts[1].equals("spell")) {
            playerAttack(player, target, AttackType.SPELL);
        } else if (parts[1].equals("monster")) {
            monsterAttack(player, target);
        }
    }

    private void handleTrade(Player player, String[] parts) {
        char playerToReceive = parts[1].charAt(0);
        char indexOfTreasure = Character.forDigit(parts[2].charAt(0), COUNTING_SYSTEM);
        tradeTreasure(player, playerToReceive, indexOfTreasure);
    }

}
