package org.penakelex.objectsimulationsystem.vehicle;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import org.penakelex.objectsimulationsystem.behaviour.IBehaviour;
import org.penakelex.objectsimulationsystem.habitat.Configuration;
import org.penakelex.objectsimulationsystem.vehicle.exceptions.VehicleCreationException;
import org.penakelex.objectsimulationsystem.vehicle.exceptions.VehicleInvalidParameter;

import java.util.ArrayList;
import java.util.List;

public abstract sealed class Vehicle implements IBehaviour
    permits Truck, Car
{
    protected final int id;
    protected final double relativeX, relativeY;
    protected final long spawnTime;
    protected final Image image;

    private final double imageWidth, imageHeight;

    private double absoluteX, absoluteY;
    private double scaledWidth, scaledHeight;

    public Vehicle(
        final int id,
        final double relativeX,
        final double relativeY,
        final long spawnTime,
        final Image image
    ) {
        final var invalidParameters = validateParameters(
            id,
            relativeX,
            relativeY,
            spawnTime,
            image
        );

        if (invalidParameters != null) {
            throw new VehicleCreationException(invalidParameters);
        }

        this.id = id;
        this.relativeX = relativeX;
        this.relativeY = relativeY;
        this.spawnTime = spawnTime;
        this.image = image;

        this.imageWidth = image.getWidth();
        this.imageHeight = image.getHeight();
    }

    private static List<VehicleInvalidParameter> validateParameters(
        final int id,
        final double relativeX,
        final double relativeY,
        final long spawnTime,
        final Image image
    ) {
        List<VehicleInvalidParameter> invalidParameters = null;

        if (id < 0) {
            invalidParameters = new ArrayList<>(5);
            invalidParameters.add(new VehicleInvalidParameter.Id(id));
        }

        if (relativeX < 0 || relativeX > 1) {
            if (invalidParameters == null) {
                invalidParameters = new ArrayList<>(4);
            }

            invalidParameters.add(new VehicleInvalidParameter.RelativeX(
                relativeX
            ));
        }

        if (relativeY < 0 || relativeY > 1) {
            if (invalidParameters == null) {
                invalidParameters = new ArrayList<>(3);
            }

            invalidParameters.add(new VehicleInvalidParameter.RelativeY(
                relativeY
            ));
        }

        if (spawnTime < 0) {
            if (invalidParameters == null) {
                invalidParameters = new ArrayList<>(2);
            }

            invalidParameters.add(new VehicleInvalidParameter.SpawnTime(
                spawnTime
            ));
        }

        if (image == null) {
            if (invalidParameters == null) {
                invalidParameters = new ArrayList<>(1);
            }

            invalidParameters.add(new VehicleInvalidParameter.Image());
        }

        return invalidParameters;
    }

    public void onCanvasSizeUpdated(
        final double canvasWidth,
        final double canvasHeight
    ) {
        this.absoluteX = this.relativeX * canvasWidth;
        this.absoluteY = this.relativeY * canvasHeight;

        final var imageScale = Math.min(
            canvasWidth * Configuration.VEHICLE_RELATIVE_SIZE /
                imageWidth,
            canvasHeight * Configuration.VEHICLE_RELATIVE_SIZE /
                imageHeight
        );

        scaledWidth = imageWidth * imageScale;
        scaledHeight = imageHeight * imageScale;
    }

    @Override
    public void update(final long time) {
    }

    @Override
    public void draw(final GraphicsContext context) {
        context.drawImage(
            image,
            absoluteX,
            absoluteY,
            scaledWidth,
            scaledHeight
        );
    }
}
