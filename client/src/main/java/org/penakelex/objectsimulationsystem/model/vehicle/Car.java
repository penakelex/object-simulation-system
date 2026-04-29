package org.penakelex.objectsimulationsystem.model.vehicle;

import javafx.scene.image.Image;

public final class Car extends Vehicle {
    public Car(
        final int id,
        final double relativeX,
        final double relativeY,
        final long spawnTime,
        final long lifeTime,
        final Image image,
        final short imageResourceIndex
    ) {
        super(id,
            relativeX,
            relativeY,
            spawnTime,
            lifeTime,
            image,
            imageResourceIndex
        );
    }
}
