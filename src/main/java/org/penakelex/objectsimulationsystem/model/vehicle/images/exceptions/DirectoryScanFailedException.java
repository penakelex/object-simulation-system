package org.penakelex.objectsimulationsystem.model.vehicle.images.exceptions;

public final class DirectoryScanFailedException
    extends VehicleImagesException
{
    public DirectoryScanFailedException(
        final String imagesPath,
        final String message,
        final Throwable cause
    ) {
        super(
            String.format("Failed to scan directory %s: %s",
                imagesPath,
                message
            ),
            cause
        );
    }
}
