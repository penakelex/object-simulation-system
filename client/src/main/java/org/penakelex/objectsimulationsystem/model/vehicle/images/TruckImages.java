package org.penakelex.objectsimulationsystem.model.vehicle.images;

import org.penakelex.objectsimulationsystem.model.habitat.Configuration;

public final class TruckImages extends VehicleImages {
    @Override
    protected String getImagesPath() {
        return Configuration.VEHICLE_IMAGES_TRUCKS_PATH;
    }
}
