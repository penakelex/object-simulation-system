package org.penakelex.objectsimulationsystem.vehicle;

import javafx.scene.image.Image;

public final class Truck extends Vehicle {
    public Truck(
        final int id,
        final double absoluteX,
        final double absoluteY,
        final long spawnTime,
        final Image image
    ) {
        super(id, absoluteX, absoluteY, spawnTime, image);
    }
}
