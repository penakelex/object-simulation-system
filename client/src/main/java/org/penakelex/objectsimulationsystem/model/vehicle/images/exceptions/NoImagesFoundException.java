package org.penakelex.objectsimulationsystem.model.vehicle.images.exceptions;

public final class NoImagesFoundException
    extends VehicleImagesException
{
    public NoImagesFoundException(
        final String imagePath,
        final String vehicleType
    ) {
        super(
            String.format("No images found in %s for %s. " +
                    "Please add .png/.jpg/.jpeg files to the directory.",
                imagePath, vehicleType
            )
        );
    }
}
