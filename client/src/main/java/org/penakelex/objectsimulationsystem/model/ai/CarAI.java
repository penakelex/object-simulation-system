package org.penakelex.objectsimulationsystem.model.ai;

import javafx.geometry.Point2D;
import org.penakelex.objectsimulationsystem.model.habitat.Configuration;
import org.penakelex.objectsimulationsystem.model.vehicle.Car;

import java.util.Iterator;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

public final class CarAI extends BaseAI<Car> {
    private final static double MIN_X_Y = 0.5;
    private final static double MAX_X_Y =
        1 - Configuration.VEHICLE_RELATIVE_SIZE;

    public CarAI(
        final Supplier<Iterator<Car>> carsSupplier,
        final double speed
    ) {
        super(carsSupplier, speed);
    }

    @Override
    protected boolean isVehicleArrivedAtSpawn(
        final double relativeX,
        final double relativeY
    ) {
        return relativeX >= MIN_X_Y && relativeY >= MIN_X_Y;
    }

    @Override
    protected Point2D getRelativeTargetPoint() {
        return new Point2D(
            ThreadLocalRandom
                .current()
                .nextDouble(MIN_X_Y, MAX_X_Y),
            ThreadLocalRandom
                .current()
                .nextDouble(MIN_X_Y, MAX_X_Y)
        );
    }
}