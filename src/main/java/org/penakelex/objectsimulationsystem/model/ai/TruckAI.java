package org.penakelex.objectsimulationsystem.model.ai;

import javafx.geometry.Point2D;
import org.penakelex.objectsimulationsystem.model.vehicle.Truck;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

public final class TruckAI extends BaseAI<Truck> {
    private final double maxX, maxY;

    public TruckAI(
        final Supplier<List<Truck>> trucksSupplier,
        final double speed,
        final double canvasWidth,
        final double canvasHeight
    ) {
        super(trucksSupplier, speed);
        this.maxX = canvasWidth / 2.0;
        this.maxY = canvasHeight / 2.0;
    }

    @Override
    protected boolean isVehicleArrived(
        final double x,
        final double y
    ) {
        return x <= maxX && y <= maxY;
    }

    @Override
    protected Point2D getTargetPoint() {
        return new Point2D(
            ThreadLocalRandom
                .current()
                .nextDouble(0, maxX),
            ThreadLocalRandom
                .current()
                .nextDouble(0, maxY)
        );
    }
}