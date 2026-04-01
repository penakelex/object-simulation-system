package org.penakelex.objectsimulationsystem.model.vehicle.images;

import org.penakelex.objectsimulationsystem.model.habitat.Configuration;

public final class CarImages extends VehicleImages {
    @Override
    protected String getImagesPath() {
        return Configuration.VEHICLE_IMAGES_CARS_PATH;
    }
}
