package org.penakelex.objectsimulationsystem.model.ai;

import javafx.geometry.Point2D;
import org.penakelex.objectsimulationsystem.model.habitat.Configuration;
import org.penakelex.objectsimulationsystem.model.vehicle.Truck;

import java.util.Iterator;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

public final class TruckAI extends BaseAI<Truck> {
    private final static double MAX_X_Y =
        0.5 - Configuration.VEHICLE_RELATIVE_SIZE;

    public TruckAI(
        final Supplier<Iterator<Truck>> trucksSupplier,
        final double speed
    ) {
        super(trucksSupplier, speed);
    }

    @Override
    protected boolean isVehicleArrivedAtSpawn(
        final double relativeX,
        final double relativeY
    ) {
        return relativeX <= MAX_X_Y && relativeY <= MAX_X_Y;
    }

    @Override
    protected Point2D getRelativeTargetPoint() {
        return new Point2D(
            ThreadLocalRandom
                .current()
                .nextDouble(0, MAX_X_Y),
            ThreadLocalRandom
                .current()
                .nextDouble(0, MAX_X_Y)
        );
    }
}