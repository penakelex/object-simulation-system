package org.penakelex.objectsimulationsystem.model.vehicle;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import org.penakelex.objectsimulationsystem.model.behaviour.IBehaviour;
import org.penakelex.objectsimulationsystem.model.habitat.Configuration;
import org.penakelex.objectsimulationsystem.model.vehicle.exceptions.VehicleCreationException;
import org.penakelex.objectsimulationsystem.model.vehicle.exceptions.VehicleInvalidParameter;

import java.util.ArrayList;
import java.util.List;

public abstract sealed class Vehicle implements IBehaviour
    permits Truck, Car
{
    private final int id;
    private final long spawnTime;
    private final long lifetime;

    private final Image image;

    private volatile double relativeX, relativeY;
    private volatile double targetRelativeX, targetRelativeY;

    private double canvasWidth, canvasHeight;
    private double scaledWidth, scaledHeight;

    private double movementSpeed;
    private boolean hasTarget = false;
    private boolean arrived = false;

    public Vehicle(
        final int id,
        final double relativeX,
        final double relativeY,
        final long spawnTime,
        final long lifetime,
        final Image image
    ) {
        final var invalidParameters = validateParameters(
            id, relativeX, relativeY, spawnTime, lifetime, image
        );

        if (invalidParameters != null) {
            throw new VehicleCreationException(invalidParameters);
        }

        this.id = id;
        this.relativeX = relativeX;
        this.relativeY = relativeY;
        this.spawnTime = spawnTime;
        this.lifetime = lifetime;
        this.image = image;
    }

    private static List<VehicleInvalidParameter> validateParameters(
        final int id,
        final double relativeX,
        final double relativeY,
        final long spawnTime,
        final long lifetime,
        final Image image
    ) {
        List<VehicleInvalidParameter> invalidParameters = null;

        if (id < 0) {
            invalidParameters = new ArrayList<>(6);
            invalidParameters.add(new VehicleInvalidParameter.Id(id));
        }

        if (relativeX < 0 || relativeX > 1) {
            if (invalidParameters == null) {
                invalidParameters = new ArrayList<>(5);
            }

            invalidParameters.add(
                new VehicleInvalidParameter.RelativeX(relativeX)
            );
        }

        if (relativeY < 0 || relativeY > 1) {
            if (invalidParameters == null) {
                invalidParameters = new ArrayList<>(4);
            }

            invalidParameters.add(
                new VehicleInvalidParameter.RelativeY(relativeY)
            );
        }

        if (spawnTime < 0) {
            if (invalidParameters == null) {
                invalidParameters = new ArrayList<>(3);
            }

            invalidParameters.add(
                new VehicleInvalidParameter.SpawnTime(spawnTime)
            );
        }

        if (lifetime <= 0) {
            if (invalidParameters == null) {
                invalidParameters = new ArrayList<>(2);
            }
            invalidParameters
                .add(new VehicleInvalidParameter.LifeTime(lifetime));
        }

        if (image == null) {
            if (invalidParameters == null) {
                invalidParameters = new ArrayList<>(1);
            }

            invalidParameters
                .add(new VehicleInvalidParameter.Image());
        }

        return invalidParameters;
    }

    public boolean isExpired(final long currentTime) {
        return currentTime - spawnTime >= lifetime;
    }

    public int getId() {
        return id;
    }

    public long getSpawnTime() {
        return spawnTime;
    }

    public synchronized void onCanvasSizeUpdated(
        final double canvasWidth,
        final double canvasHeight
    ) {
        this.canvasWidth = canvasWidth;
        this.canvasHeight = canvasHeight;

        final var imageScale = Math.min(
            canvasWidth * Configuration.VEHICLE_RELATIVE_SIZE /
                image.getWidth(),
            canvasHeight * Configuration.VEHICLE_RELATIVE_SIZE /
                image.getHeight()
        );

        this.scaledWidth = image.getWidth() * imageScale;
        this.scaledHeight = image.getHeight() * imageScale;
    }

    public void setMovementSpeed(
        final double movementSpeed
    ) {
        this.movementSpeed = movementSpeed;
    }

    public void setTarget(
        final double targetRelativeX,
        final double targetRelativeY
    ) {
        this.targetRelativeX = targetRelativeX;
        this.targetRelativeY = targetRelativeY;
        this.hasTarget = true;
    }

    public boolean hasTarget() {
        return hasTarget;
    }

    public boolean isArrived() {
        return arrived;
    }

    public double getRelativeX() {
        return relativeX;
    }

    public double getRelativeY() {
        return relativeY;
    }

    public void markArrived() {
        this.arrived = true;
        this.hasTarget = false;
    }

    @Override
    public synchronized void update(final long currentTime) {
    }

    public void move(final double deltaTimeSeconds) {
        if (!hasTarget || arrived
            || canvasWidth <= 0 || canvasHeight <= 0) {
            return;
        }

        double absoluteX = relativeX * canvasWidth,
            absoluteY = relativeY * canvasHeight;
        final double absoluteTargetX = targetRelativeX * canvasWidth,
            absoluteTargetY = targetRelativeY * canvasHeight;

        final double dx = absoluteTargetX - absoluteX,
            dy = absoluteTargetY - absoluteY;
        final double distance = Math.hypot(dx, dy);
        final double step = movementSpeed * deltaTimeSeconds;

        if (distance <= step) {
            absoluteX = absoluteTargetX;
            absoluteY = absoluteTargetY;
            arrived = true;
        } else {
            absoluteX += (dx / distance) * step;
            absoluteY += (dy / distance) * step;
        }

        relativeX = absoluteX / canvasWidth;
        relativeY = absoluteY / canvasHeight;
    }

    @Override
    public void draw(final GraphicsContext context) {
        context.drawImage(
            image,
            relativeX * canvasWidth,
            relativeY * canvasHeight,
            scaledWidth,
            scaledHeight
        );
    }
}