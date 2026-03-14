package org.penakelex.objectsimulationsystem.vehicle.images.exceptions;

public final class ImageLoadException extends VehicleImagesException {
    public ImageLoadException(
        final String imageName,
        final Throwable cause
    ) {
        super("Failed to load image: %s".formatted(imageName), cause);
    }
}
