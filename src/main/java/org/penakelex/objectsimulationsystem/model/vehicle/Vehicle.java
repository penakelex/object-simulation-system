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
    protected final int id;
    protected final double spawnRelativeX, spawnRelativeY;
    protected final long spawnTime;
    protected final long lifetime;
    protected final Image image;

    private final double imageWidth, imageHeight;

    private double currentRelativeX, currentRelativeY;
    private double targetRelativeX, targetRelativeY;

    private double canvasWidth, canvasHeight;
    private double scaledWidth, scaledHeight;

    private double movementSpeed;
    private volatile boolean hasTarget = false;
    private volatile boolean arrived = false;
    private volatile boolean initialized = false;

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
        this.spawnRelativeX = relativeX;
        this.spawnRelativeY = relativeY;
        this.spawnTime = spawnTime;
        this.lifetime = lifetime;
        this.image = image;
        this.imageWidth = image.getWidth();
        this.imageHeight = image.getHeight();
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

    public long getLifetime() {
        return lifetime;
    }

    public synchronized void onCanvasSizeUpdated(
        final double width,
        final double height
    ) {
        this.canvasWidth = width;
        this.canvasHeight = height;

        if (!initialized) {
            this.currentRelativeX = this.spawnRelativeX;
            this.currentRelativeY = this.spawnRelativeY;
            this.initialized = true;
        }

        final var imageScale = Math.min(
            width * Configuration.VEHICLE_RELATIVE_SIZE / imageWidth,
            height * Configuration.VEHICLE_RELATIVE_SIZE / imageHeight
        );

        this.scaledWidth = imageWidth * imageScale;
        this.scaledHeight = imageHeight * imageScale;
    }

    public synchronized void setMovementSpeed(final double speed) {
        this.movementSpeed = speed;
    }

    public synchronized void setTarget(
        final double absoluteTargetX,
        final double absoluteTargetY
    ) {
        if (canvasWidth > 0 && canvasHeight > 0) {
            this.targetRelativeX = absoluteTargetX / canvasWidth;
            this.targetRelativeY = absoluteTargetY / canvasHeight;
            this.hasTarget = true;
            this.arrived = false;
        }
    }

    public synchronized boolean hasTarget() {
        return hasTarget;
    }

    public boolean isArrived() {
        return arrived;
    }

    public synchronized double getAbsoluteX() {
        return currentRelativeX * canvasWidth;
    }

    public synchronized double getAbsoluteY() {
        return currentRelativeY * canvasHeight;
    }

    public synchronized void markArrived() {
        this.arrived = true;
        this.hasTarget = false;
    }

    @Override
    public synchronized void update(final long currentTime) {
    }

    public synchronized void move(final double deltaTimeSeconds) {
        if (!hasTarget || arrived || canvasWidth <= 0) {
            return;
        }

        double absoluteX = currentRelativeX * canvasWidth;
        double absoluteY = currentRelativeY * canvasHeight;
        final double absoluteTargetX = targetRelativeX * canvasWidth;
        final double absoluteTargetY = targetRelativeY * canvasHeight;

        final double dx = absoluteTargetX - absoluteX;
        final double dy = absoluteTargetY - absoluteY;
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

        currentRelativeX = absoluteX / canvasWidth;
        currentRelativeY = absoluteY / canvasHeight;
    }

    @Override
    public synchronized void draw(final GraphicsContext context) {
        context.drawImage(image,
            currentRelativeX * canvasWidth,
            currentRelativeY * canvasHeight,
            scaledWidth,
            scaledHeight
        );
    }
}