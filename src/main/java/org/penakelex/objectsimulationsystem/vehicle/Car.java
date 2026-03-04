package org.penakelex.objectsimulationsystem.vehicle;

import javafx.scene.image.Image;

public final class Car extends Vehicle {
    public Car(
        final int id,
        final double x,
        final double y,
        final long birthTime,
        final Image image
    ) {
        super(id, x, y, birthTime, image);
    }
}
