package org.penakelex.shared.network;

import java.io.Serializable;
import java.util.List;

public sealed interface ServerToClientMessage extends Serializable {
    record ClientList(List<String> clientIds, String assignedClientId)
        implements ServerToClientMessage
    {
    }

    record VehicleStateRequest(
        String exchangeId,
        String requesterClientId,
        String targetClientId,
        VehicleType vehicleType
    ) implements ServerToClientMessage
    {
    }

    record VehicleExchangeResponse(
        String senderClientId,
        VehicleType vehicleType,
        List<VehicleState> vehicles
    ) implements ServerToClientMessage
    {
    }

    record ErrorMessage(String message)
        implements ServerToClientMessage
    {
    }
}