package org.penakelex.objectsimulationsystem.network;

import org.penakelex.shared.network.ServerToClientMessage;
import org.penakelex.shared.network.ClientToServerMessage;

import java.io.*;
import java.net.*;
import java.util.function.Consumer;

public final class NetworkClient implements AutoCloseable {
    private final Socket serverConnection;
    private final ObjectOutputStream outputStream;
    private final ObjectInputStream inputStream;
    private final Consumer<ServerToClientMessage> messageHandler;
    private volatile boolean connectionActive = true;

    public NetworkClient(
        final String serverHost,
        final int serverPort,
        final Consumer<ServerToClientMessage> messageHandler
    ) throws IOException {
        this.serverConnection = new Socket(serverHost, serverPort);
        this.outputStream =
            new ObjectOutputStream(serverConnection.getOutputStream());
        this.inputStream =
            new ObjectInputStream(serverConnection.getInputStream());
        this.messageHandler = messageHandler;

        final var listenerThread =
            new Thread(this::listenForMessages, "NetworkListener");
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    public synchronized void send(final ClientToServerMessage message)
    throws IOException {
        if (!connectionActive || serverConnection.isClosed()) {
            throw new IllegalStateException("Connection is closed");
        }

        outputStream.reset();
        outputStream.writeObject(message);
        outputStream.flush();
    }

    private void listenForMessages() {
        try {
            while (connectionActive && !serverConnection.isClosed()) {
                final var receivedMessage =
                    (ServerToClientMessage) inputStream.readObject();

                if (receivedMessage instanceof
                    ServerToClientMessage.ClientList clientList &&
                    clientList.assignedClientId() != null) {
                    final var assignedClientId =
                        clientList.assignedClientId();
                    IO.println(String.format(
                        "Connected to server. Client ID: %s",
                        assignedClientId
                    ));
                }

                messageHandler.accept(receivedMessage);
            }
        } catch (final EOFException | SocketException _) {
            connectionActive = false;
            IO.println("Disconnected from server");
        } catch (final ClassNotFoundException |
                       IOException networkException) {
            if (connectionActive) {
                IO.println(String.format("Network read error: %s",
                    networkException.getMessage()
                ));
            }
        }
    }

    @Override
    public void close() throws IOException {
        if (!connectionActive) {
            return;
        }

        connectionActive = false;

        if (!serverConnection.isClosed()) {
            try {
                outputStream.flush();
                outputStream.close();
                serverConnection.shutdownOutput();
            } catch (final IOException _) {
            } finally {
                serverConnection.close();
            }
        }
    }

    public boolean isConnected() {
        return connectionActive && !serverConnection.isClosed();
    }
}