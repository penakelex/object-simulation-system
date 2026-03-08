package org.penakelex.objectsimulationsystem.habitat;

import javafx.scene.image.Image;
import org.penakelex.objectsimulationsystem.vehicle.Vehicle;

@FunctionalInterface
public interface VehicleFactory<T extends Vehicle> {
    T create(
        final int id,
        final double relativeX,
        final double relativeY,
        final long spawnTime,
        final Image image
    );
}
