package org.penakelex.objectsimulationsystem.vehicle.images;

import java.util.List;

public final class TruckImages extends VehicleImages {
    @Override
    protected List<String> getImagesNames() {
        return List.of(
            "truck.png"
//            "truck1.png",
//            "truck2.png",
//            "truck3.png"
        );
    }
}
