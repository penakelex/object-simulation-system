package org.penakelex.objectsimulationsystem.model.ai;

import javafx.geometry.Point2D;
import org.penakelex.objectsimulationsystem.model.vehicle.Car;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

public final class CarAI extends BaseAI<Car> {
    private final double minX, minY, canvasWidth, canvasHeight;

    public CarAI(
        final Supplier<List<Car>> carsSupplier,
        final double speed,
        final double canvasWidth,
        final double canvasHeight
    ) {
        super(carsSupplier, speed);
        this.canvasWidth = canvasWidth;
        this.canvasHeight = canvasHeight;
        this.minX = canvasWidth / 2.0;
        this.minY = canvasHeight / 2.0;
    }

    @Override
    protected boolean isVehicleArrived(
        final double x,
        final double y
    ) {
        return x >= minX && y >= minY;
    }

    @Override
    protected Point2D getTargetPoint() {
        return new Point2D(
            ThreadLocalRandom
                .current()
                .nextDouble(minX, canvasWidth),
            ThreadLocalRandom
                .current()
                .nextDouble(minY, canvasHeight)
        );
    }
}