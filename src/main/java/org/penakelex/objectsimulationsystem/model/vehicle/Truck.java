package org.penakelex.objectsimulationsystem.model.vehicle;

import javafx.scene.image.Image;

public final class Truck extends Vehicle {
    public Truck(
        final int id,
        final double relativeX,
        final double relativeY,
        final long spawnTime,
        final long lifeTime,
        final Image image
    ) {
        super(id, relativeX, relativeY, spawnTime, lifeTime, image);
    }
}
