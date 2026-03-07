package org.penakelex.objectsimulationsystem.vehicle;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import org.penakelex.objectsimulationsystem.behaviour.IBehaviour;

public abstract sealed class Vehicle implements IBehaviour
    permits Truck, Car
{
    public static final double RELATIVE_SIZE = 0.05;

    protected final int id;
    protected final double relativeX, relativeY;
    protected final long spawnTime;
    protected final Image image;

    private double absoluteX, absoluteY;

    public Vehicle(
        final int id,
        final double relativeX,
        final double relativeY,
        final long spawnTime,
        final Image image
    ) {
        this.id = id;
        this.relativeX = relativeX;
        this.relativeY = relativeY;
        this.spawnTime = spawnTime;
        this.image = image;
    }

    public void updateAbsoluteXPosition(final double canvasWidth) {
        this.absoluteX = this.relativeX * canvasWidth;
    }

    public void updateAbsoluteYPosition(final double canvasHeight) {
        this.absoluteY = this.relativeY * canvasHeight;

    }

    @Override
    public void update(final long time) {
    }

    @Override
    public void draw(final GraphicsContext context) {
        final var drawHeight =
            context.getCanvas().getHeight() * RELATIVE_SIZE;
        final var drawWidth =
            drawHeight * image.getWidth() / image.getHeight();

        context.drawImage(
            image,
            absoluteX,
            absoluteY,
            drawWidth,
            drawHeight
        );
    }
}
