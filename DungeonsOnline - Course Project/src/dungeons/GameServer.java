package dungeons;

import dungeons.entities.player.Player;
import dungeons.exception.PlayerCharAlreadyExistsException;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Set;

public class GameServer {
    private static final String FILE_NAME_FOR_EXCEPTION_LOGS = "ServerLog";

    public static final int SERVER_PORT = 7777;
    private static final String SERVER_HOST = "localhost";
    private static final int BUFFER_SIZE = 1_024;
    private Selector selector;
    private ByteBuffer buffer;
    private GameMap gameMap;

    public static void main(String[] args) {
        GameServer gameServer = new GameServer();
        gameServer.startServer();
    }

    private void startServer() {
        gameMap = new GameMap();
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            serverSocketChannel.bind(new InetSocketAddress(SERVER_HOST, SERVER_PORT));
            serverSocketChannel.configureBlocking(false);
            selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            buffer = ByteBuffer.allocate(BUFFER_SIZE);
            while (true) {
                int readyChannels = selector.select();
                if (readyChannels == 0) {
                    continue;
                }
                iterateThroughKeys();
            }
        } catch (IOException e) {
            logException(e);
            throw new UncheckedIOException(
                "There was a problem with the communication with the clients. " +
                    "Exception Logs saved to file: " +
                    Paths.get(FILE_NAME_FOR_EXCEPTION_LOGS).toAbsolutePath(),
                e);
        } catch (PlayerCharAlreadyExistsException e) {
            logException(e);
            throw new RuntimeException(e);
        }
    }

    private void iterateThroughKeys()
        throws IOException, PlayerCharAlreadyExistsException {
        Set<SelectionKey> selectedKeys = selector.selectedKeys();
        Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
        while (keyIterator.hasNext()) {
            SelectionKey key = keyIterator.next();
            if (key.isReadable()) {
                handleQuery(key);
            } else if (key.isAcceptable()) {
                handleNewConnection(key);
            }
            gameMap.broadcast(selector);
            keyIterator.remove();
        }
    }

    private void handleQuery(SelectionKey key) throws IOException {
        SocketChannel sc = (SocketChannel) key.channel();

        buffer.clear();
        int r = sc.read(buffer);
        if (r < 0) {
            System.out.println("Client has closed the connection");
            sc.close();
            gameMap.disconnectPlayer((Player) key.attachment());
            gameMap.broadcast(selector);
            return;
        }
        buffer.flip();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);

        String newContent = new String(bytes, StandardCharsets.UTF_8);
        Player currPlayer = (Player) key.attachment();
        gameMap.handleAction(currPlayer, newContent);
    }

    private void handleNewConnection(SelectionKey key)
        throws IOException, PlayerCharAlreadyExistsException {
        ServerSocketChannel sockChannel = (ServerSocketChannel) key.channel();
        SocketChannel accept = sockChannel.accept();
        accept.configureBlocking(false);
        Player newPlayer = new Player();
        accept.register(selector, SelectionKey.OP_READ, newPlayer);
        gameMap.connectPlayer(newPlayer);
    }

    private static void logException(Exception e) {
        try (PrintWriter writer = new PrintWriter(
            new BufferedWriter(new FileWriter(FILE_NAME_FOR_EXCEPTION_LOGS, true)))) {
            writer.println(LocalDateTime.now());
            writer.println("DungeonsOnline.Exception occurred: " + e.getMessage());
            e.printStackTrace(writer);
        } catch (IOException ioException) {
            throw new UncheckedIOException(
                "Could not write logs to:" + Paths.get(FILE_NAME_FOR_EXCEPTION_LOGS).toAbsolutePath(), ioException);
        }
    }
}
