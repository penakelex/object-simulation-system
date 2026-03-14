package org.penakelex.objectsimulationsystem.vehicle.images;

import org.penakelex.objectsimulationsystem.habitat.Configuration;

public final class CarImages extends VehicleImages {
    @Override
    protected String getImagesPath() {
        return Configuration.VEHICLE_IMAGES_CARS_PATH;
    }
}
