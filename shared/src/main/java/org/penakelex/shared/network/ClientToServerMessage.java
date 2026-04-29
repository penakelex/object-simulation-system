package org.penakelex.shared.network;

import java.io.Serializable;
import java.util.List;

public sealed interface ClientToServerMessage extends Serializable {
    record VehicleExchangeRequest(
        String targetClientId,
        VehicleType vehicleType
    ) implements ClientToServerMessage
    {
    }

    record VehicleStateResponse(
        String exchangeId,
        String clientName,
        VehicleType vehicleType,
        List<VehicleState> vehicles
    ) implements ClientToServerMessage
    {
    }
}