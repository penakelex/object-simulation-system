package org.penakelex.objectsimulationsystem.vehicle.images.exceptions;

public final class InvalidImagesUriException
    extends VehicleImagesException
{
    public InvalidImagesUriException(
        final String imagesPath,
        final String message,
        final Throwable cause
    ) {
        super(
            String.format("Invalid URI for %s: %s",
                imagesPath,
                message
            ),
            cause
        );
    }
}
