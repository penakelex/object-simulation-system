package org.penakelex.objectsimulationsystem.vehicle;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import org.penakelex.objectsimulationsystem.behaviour.IBehaviour;
import org.penakelex.objectsimulationsystem.habitat.Configuration;
import org.penakelex.objectsimulationsystem.vehicle.exceptions.VehicleCreationException;
import org.penakelex.objectsimulationsystem.vehicle.exceptions.VehicleInvalidParameter;

import java.util.ArrayList;

public abstract sealed class Vehicle implements IBehaviour
    permits Truck, Car
{
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
        final var invalidParameters =
            new ArrayList<VehicleInvalidParameter>();

        if (id < 0) {
            invalidParameters.add(new VehicleInvalidParameter.Id(id));
        }

        if (relativeX < 0 || relativeX > 1) {
            invalidParameters.add(new VehicleInvalidParameter.RelativeX(
                relativeX
            ));
        }

        if (relativeY < 0 || relativeY > 1) {
            invalidParameters.add(new VehicleInvalidParameter.RelativeY(
                relativeY
            ));
        }

        if (spawnTime < 0) {
            invalidParameters.add(new VehicleInvalidParameter.SpawnTime(
                spawnTime
            ));
        }

        if (image == null) {
            invalidParameters.add(new VehicleInvalidParameter.Image());
        }

        if (!invalidParameters.isEmpty()) {
            throw new VehicleCreationException(invalidParameters);
        }

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
        final var canvas = context.getCanvas();
        final var imageWidth = image.getWidth();
        final var imageHeight = image.getHeight();

        final var scale = Math.min(
            canvas.getWidth() * Configuration.VEHICLE_RELATIVE_SIZE /
                imageWidth,
            canvas.getHeight() * Configuration.VEHICLE_RELATIVE_SIZE /
                imageHeight
        );

        context.drawImage(
            image,
            absoluteX,
            absoluteY,
            imageWidth * scale,
            imageHeight * scale
        );
    }
}
