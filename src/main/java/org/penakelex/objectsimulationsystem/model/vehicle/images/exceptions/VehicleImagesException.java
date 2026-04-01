package org.penakelex.objectsimulationsystem.model.vehicle.images.exceptions;

public class VehicleImagesException extends RuntimeException {
    public VehicleImagesException(final String message) {
        super(message);
    }

    public VehicleImagesException(
        final String message,
        final Throwable cause
    ) {
        super(message, cause);
    }
}
