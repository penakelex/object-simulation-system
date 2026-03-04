package org.penakelex.objectsimulationsystem.vehicle;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import org.penakelex.objectsimulationsystem.behaviour.IBehaviour;

public abstract sealed class Vehicle implements IBehaviour
    permits Truck, Car
{
    protected final int id;
    protected double x, y;
    protected final long birthTime;
    protected final Image image;

    public Vehicle(
        final int id,
        final double x,
        final double y,
        final long birthTime,
        final Image image
    ) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.birthTime = birthTime;
        this.image = image;
    }

    @Override
    public void update(final long time) {

    }

    @Override
    public void draw(final GraphicsContext context) {
        final var drawHeight = context.getCanvas().getHeight() * 0.05;
        context.drawImage(
            image,
            x,
            y,
            drawHeight * image.getWidth() / image.getHeight(),
            drawHeight
        );
    }
}
