package org.penakelex.objectsimulationsystem.model.vehicle.images.exceptions;

public final class ResourcePathNotFoundException
    extends VehicleImagesException
{
    public ResourcePathNotFoundException(
        final String imagesPath,
        final String vehicleType
    ) {
        super(String.format(
            "Resource path not found: %s for %s. " +
                "Please create the directory and add image files.",
            imagesPath,
            vehicleType
        ));
    }
}
