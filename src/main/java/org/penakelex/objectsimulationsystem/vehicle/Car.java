package org.penakelex.objectsimulationsystem.vehicle;

import javafx.scene.image.Image;

public final class Car extends Vehicle {
    public Car(
        final int id,
        final double relativeX,
        final double relativeY,
        final long spawnTime,
        final Image image
    ) {
        super(id, relativeX, relativeY, spawnTime, image);
    }
}
