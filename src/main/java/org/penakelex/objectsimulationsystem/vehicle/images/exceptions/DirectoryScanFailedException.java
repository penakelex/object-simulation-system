package org.penakelex.objectsimulationsystem.vehicle.images.exceptions;

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
