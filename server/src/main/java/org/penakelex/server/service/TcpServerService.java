package org.penakelex.server.service;

import org.penakelex.shared.network.ClientToServerMessage;
import org.penakelex.shared.network.ServerToClientMessage;
import org.penakelex.shared.network.VehicleState;
import org.penakelex.shared.network.VehicleType;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

@Service
public final class TcpServerService {
    private static final int PORT_RANGE_START = 9000;
    private static final int PORT_RANGE_END = 9999;

    private final Map<String, ClientConnection> activeClients =
        new ConcurrentHashMap<>();
    private final Map<String, ExchangeContext> pendingExchanges =
        new ConcurrentHashMap<>();

    private ServerSocket serverSocket;
    private ExecutorService executorService;

    @PostConstruct
    public void start() throws IOException {
        final var selectedPort =
            findFreePort(PORT_RANGE_START, PORT_RANGE_END);
        serverSocket = new ServerSocket(selectedPort);

        IO.println(String.format("TCP Server started on port: %d",
            selectedPort
        ));

        executorService = Executors.newVirtualThreadPerTaskExecutor();
        executorService.submit(this::acceptIncomingConnections);
    }

    private static int findFreePort(
        final int rangeStart,
        final int rangeEnd
    ) {
        for (int port = rangeStart; port <= rangeEnd; port++) {
            try (final var _ = new ServerSocket(port)) {
                return port;
            } catch (final IOException _) {
            }
        }

        throw new IllegalStateException(String.format(
            "No free port in range [%d, %d]",
            rangeStart,
            rangeEnd
        ));
    }

    @PreDestroy
    public void stop() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (final IOException _) {
        }

        if (executorService != null) {
            executorService.shutdownNow();
        }
    }

    private void acceptIncomingConnections() {
        while (!serverSocket.isClosed()) {
            try {
                final var assignedClientId =
                    UUID.randomUUID().toString().substring(0, 8);
                final var connection = new ClientConnection(
                    serverSocket.accept(),
                    assignedClientId,
                    this
                );
                activeClients.put(assignedClientId, connection);

                broadcastClientList();
                connection.sendMessage(
                    new ServerToClientMessage.ClientList(
                        new ArrayList<>(activeClients.keySet()),
                        assignedClientId
                    )
                );
                executorService.submit(connection::handleIncomingMessages);
            } catch (final IOException socketException) {
                if (!serverSocket.isClosed()) {
                    IO.println(String.format(
                        "Failed to accept connection: %s",
                        socketException.getMessage()
                    ));
                }
            }
        }
    }

    public void broadcastClientList() {
        final var updatedList = new ServerToClientMessage.ClientList(
            new ArrayList<>(activeClients.keySet()),
            null
        );

        for (final var connection : activeClients.values()) {
            if (connection != null) {
                connection.sendMessage(updatedList);
            }
        }
    }

    public void handleExchangeRequest(
        final String requesterClientId,
        final ClientToServerMessage.VehicleExchangeRequest request
    ) {
        final var targetConnection =
            activeClients.get(request.targetClientId());
        final var requesterConnection =
            activeClients.get(requesterClientId);

        if (targetConnection == null || requesterConnection == null) {
            if (requesterConnection != null) {
                requesterConnection.sendMessage(
                    new ServerToClientMessage.ErrorMessage(
                        "Target client not found"
                    )
                );
            }
            return;
        }

        final var exchangeId = UUID.randomUUID().toString();
        final var exchangeContext = new ExchangeContext(
            requesterClientId,
            request.targetClientId(),
            request.vehicleType()
        );
        pendingExchanges.put(exchangeId, exchangeContext);

        final var stateRequest =
            new ServerToClientMessage.VehicleStateRequest(
                exchangeId,
                requesterClientId,
                request.targetClientId(),
                request.vehicleType()
            );

        requesterConnection.sendMessage(stateRequest);
        targetConnection.sendMessage(stateRequest);
    }

    public void handleStateResponse(final ClientToServerMessage.VehicleStateResponse response) {
        final var exchangeContext =
            pendingExchanges.get(response.exchangeId());

        if (exchangeContext == null) {
            return;
        }

        synchronized (exchangeContext) {
            if (response.clientName()
                .equals(exchangeContext.requesterClientId)
            ) {
                exchangeContext.requesterVehicles =
                    response.vehicles();
            } else if (response.clientName()
                .equals(exchangeContext.targetClientId)
            ) {
                exchangeContext.targetVehicles = response.vehicles();
            }

            if (exchangeContext.requesterVehicles != null &&
                exchangeContext.targetVehicles != null
            ) {
                final var requesterConnection =
                    activeClients.get(exchangeContext.requesterClientId);
                final var targetConnection =
                    activeClients.get(exchangeContext.targetClientId);

                if (requesterConnection != null &&
                    targetConnection != null) {
                    requesterConnection.sendMessage(
                        new ServerToClientMessage
                            .VehicleExchangeResponse(
                            exchangeContext.targetClientId,
                            exchangeContext.vehicleType,
                            exchangeContext.targetVehicles
                        )
                    );
                    targetConnection.sendMessage(
                        new ServerToClientMessage
                            .VehicleExchangeResponse(
                            exchangeContext.requesterClientId,
                            exchangeContext.vehicleType,
                            exchangeContext.requesterVehicles
                        )
                    );
                }
                pendingExchanges.remove(response.exchangeId());
            }
        }
    }

    public void removeClient(final String clientId) {
        activeClients.remove(clientId);
        broadcastClientList();
    }

    private static final class ExchangeContext {
        final String requesterClientId;
        final String targetClientId;
        final VehicleType vehicleType;
        List<VehicleState> requesterVehicles;
        List<VehicleState> targetVehicles;

        ExchangeContext(
            final String requesterClientId,
            final String targetClientId,
            final VehicleType vehicleType
        ) {
            this.requesterClientId = requesterClientId;
            this.targetClientId = targetClientId;
            this.vehicleType = vehicleType;
            this.requesterVehicles = null;
            this.targetVehicles = null;
        }
    }

    private static final class ClientConnection {
        private final Socket clientSocket;
        private final String assignedClientId;
        private final TcpServerService serverService;
        private final ObjectOutputStream outputStream;
        private final ObjectInputStream inputStream;

        public ClientConnection(
            final Socket clientSocket,
            final String assignedClientId,
            final TcpServerService serverService
        ) throws IOException {
            this.clientSocket = clientSocket;
            this.assignedClientId = assignedClientId;
            this.serverService = serverService;
            this.outputStream = new ObjectOutputStream(
                clientSocket.getOutputStream()
            );
            this.inputStream =
                new ObjectInputStream(clientSocket.getInputStream());

            clientSocket.setKeepAlive(true);
            clientSocket.setTcpNoDelay(true);
        }

        void handleIncomingMessages() {
            try {
                while (!clientSocket.isClosed()) {
                    final var incomingMessage =
                        (ClientToServerMessage)
                            inputStream.readObject();
                    switch (incomingMessage) {
                        case ClientToServerMessage
                                 .VehicleExchangeRequest request ->
                            serverService.handleExchangeRequest(
                                assignedClientId,
                                request
                            );
                        case ClientToServerMessage
                                 .VehicleStateResponse response ->
                            serverService.handleStateResponse(response);
                    }
                }
            } catch (final EOFException | SocketException _) {
            } catch (final ClassNotFoundException |
                           IOException networkException) {
                IO.println(String.format(
                    "Network error for client %s: %s",
                    assignedClientId,
                    networkException.getMessage()
                ));
            } finally {
                closeConnection();
            }
        }

        synchronized void sendMessage(
            final ServerToClientMessage message
        ) {
            try {
                outputStream.reset();
                outputStream.writeObject(message);
                outputStream.flush();
            } catch (final IOException writeException) {
                closeConnection();
            }
        }

        void closeConnection() {
            try {
                clientSocket.close();
            } catch (final IOException _) {
            }

            serverService.removeClient(assignedClientId);
        }
    }
}