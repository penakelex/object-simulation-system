package org.penakelex.objectsimulationsystem.vehicle.images;

import java.util.List;

public final class CarImages extends VehicleImages {
    @Override
    protected List<String> getImagesNames() {
        return List.of(
            "car.png"
//            "car1.png",
//            "car2.png",
//            "car3.png"
        );
    }
}
