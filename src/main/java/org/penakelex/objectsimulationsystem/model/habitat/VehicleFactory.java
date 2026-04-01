package org.penakelex.objectsimulationsystem.model.habitat;

import javafx.scene.image.Image;
import org.penakelex.objectsimulationsystem.model.vehicle.Vehicle;

@FunctionalInterface
public interface VehicleFactory<T extends Vehicle> {
    T create(
        final int id,
        final double relativeX,
        final double relativeY,
        final long spawnTime,
        final long lifeTime,
        final Image image
    );
}
