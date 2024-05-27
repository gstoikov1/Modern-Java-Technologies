package dungeons;

import dungeons.exception.UnableToConnectToServerException;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class GameClient {
    private static final String FILE_NAME_FOR_EXCEPTION_LOGS = "ClientLog";
    private static final int SERVER_PORT = 7777;
    private static final String SERVER_HOST = "localhost";
    private static final int BUFFER_SIZE = 2000;

    private ByteBuffer receiveBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
    private ByteBuffer sendBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE);

    private AtomicBoolean running;

    public static void main(String[] args) throws UnableToConnectToServerException {
        GameClient gameClient = new GameClient();
        gameClient.startClient();
    }

    public void startClient() throws UnableToConnectToServerException {
        running = new AtomicBoolean(true);

        try (SocketChannel socketChannel = SocketChannel.open();
             Scanner scanner = new Scanner(System.in)) {
            socketChannel.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT));
            System.out.println("Connected to the server.");
            Thread sendThread = createSendThread(scanner, socketChannel);

            Thread receiveThread = createReceiveThread(scanner, socketChannel);

            sendThread.start();
            receiveThread.start();

            sendThread.join();
            receiveThread.join();

        } catch (IOException | InterruptedException e) {
            logException(e);
            throw new UnableToConnectToServerException(
                "Unable to connect to the server." +
                    " Try again later or contact administrator by providing the logs in " +
                    Paths.get(FILE_NAME_FOR_EXCEPTION_LOGS).toAbsolutePath(),
                e);
        }
    }

    private Thread createSendThread(Scanner scanner, SocketChannel socketChannel) {
        return new Thread(() -> {
            while (running.get()) {
                String message = scanner.nextLine(); // read a line from the console
                System.out.println();
                if ("quit".equals(message)) {
                    running.set(false);
                }

                try {
                    sendToServer(socketChannel, message);
                } catch (IOException e) {
                    throw new UncheckedIOException(
                        "Unable to send data to the server." +
                            " Try again later or contact administrator by providing the logs in " +
                            Paths.get(FILE_NAME_FOR_EXCEPTION_LOGS).toAbsolutePath(),
                        e);
                }
            }
        });
    }

    private Thread createReceiveThread(Scanner scanner, SocketChannel socketChannel) {
        return new Thread(() -> {
            try {
                while (running.get()) {
                    receiveFromServer(socketChannel);
                }
            } catch (IOException e) {
                throw new UncheckedIOException(
                    "Unable to receive data to the server." +
                        " Try again later or contact administrator by providing the logs in " +
                        Paths.get(FILE_NAME_FOR_EXCEPTION_LOGS).toAbsolutePath(),
                    e);
            }
        });
    }

    private void sendToServer(SocketChannel socketChannel, String message) throws IOException {
        sendBuffer.clear();
        sendBuffer.put(message.getBytes());
        sendBuffer.flip();
        socketChannel.write(sendBuffer);
    }

    private void receiveFromServer(SocketChannel socketChannel) throws IOException {
        receiveBuffer.clear();
        socketChannel.read(receiveBuffer);
        receiveBuffer.flip();
        char[][] receivedMatrix = fromByteBuffer(receiveBuffer);
        printMatrix(receivedMatrix, receivedMatrix.length, receivedMatrix[0].length);
        byte[] remainingBytes = new byte[receiveBuffer.remaining()];
        receiveBuffer.get(remainingBytes);
        String playerInformation = new String(remainingBytes, StandardCharsets.UTF_8);
        System.out.println(playerInformation);
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

    private static char[][] fromByteBuffer(ByteBuffer byteBuffer) {
        int rows = byteBuffer.getChar();
        int cols = byteBuffer.getChar();
        char[][] restoredMatrix = new char[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                restoredMatrix[i][j] = byteBuffer.getChar();
            }
        }

        return restoredMatrix;
    }

    private static void printMatrix(char[][] a, int rows, int cols) {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                System.out.print(a[i][j]);
            }
            System.out.print(System.lineSeparator());
        }
    }
}
