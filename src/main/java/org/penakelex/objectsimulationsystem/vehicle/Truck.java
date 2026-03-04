package org.penakelex.objectsimulationsystem.vehicle;

import javafx.scene.image.Image;

public final class Truck extends Vehicle {
    public Truck(
        final int id,
        final double x,
        final double y,
        final long birthTime,
        final Image image
    ) {
        super(id, x, y, birthTime, image);
    }
}
