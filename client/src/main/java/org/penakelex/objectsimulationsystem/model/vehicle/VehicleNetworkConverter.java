package org.penakelex.objectsimulationsystem.model.vehicle;

import org.penakelex.shared.network.VehicleState;
import org.penakelex.objectsimulationsystem.model.vehicle.images.CarImages;
import org.penakelex.objectsimulationsystem.model.vehicle.images.TruckImages;
import org.apache.commons.lang3.tuple.Pair;
import org.penakelex.shared.network.VehicleType;

import java.util.function.Supplier;

public final class VehicleNetworkConverter {
    private VehicleNetworkConverter() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static Vehicle fromState(
        final VehicleState state,
        final Supplier<Integer> idSupplier,
        final TruckImages truckImages,
        final CarImages carImages,
        final double canvasWidth,
        final double canvasHeight
    ) {
        final var vehicleImages = switch (state.type()) {
            case Truck -> truckImages;
            case Car -> carImages;
        };

        final var imageWithIndex = vehicleImages
            .getImageForResourceIndex(state.imageResourceIndex())
            .map(image -> Pair.of(image, state.imageResourceIndex()))
            .orElseGet(vehicleImages::getRandomImageWithIndex);

        final var vehicleId = idSupplier.get();
        final var currentTime = System.currentTimeMillis();

        final var vehicle = switch (state.type()) {
            case Truck -> new Truck(
                vehicleId,
                state.relativeX(),
                state.relativeY(),
                currentTime,
                state.remainingLifetime(),
                imageWithIndex.getLeft(),
                imageWithIndex.getRight()
            );
            case Car -> new Car(
                vehicleId,
                state.relativeX(),
                state.relativeY(),
                currentTime,
                state.remainingLifetime(),
                imageWithIndex.getLeft(),
                imageWithIndex.getRight()
            );
        };

        if (state.hasTarget()) {
            vehicle.setTarget(state.targetRelativeX(),
                state.targetRelativeY()
            );

            if (state.arrived()) {
                vehicle.markArrived();
            }
        }

        vehicle.onCanvasSizeUpdated(canvasWidth, canvasHeight);
        return vehicle;
    }

    public static VehicleState toState(final Vehicle vehicle) {

        return new VehicleState(
            vehicle.getRelativeX(),
            vehicle.getRelativeY(),
            vehicle.hasTarget() ? vehicle.getTargetRelativeX() : 0.0,
            vehicle.hasTarget() ? vehicle.getTargetRelativeY() : 0.0,
            Math.max(
                1L,
                vehicle.getLifetime() - System.currentTimeMillis() -
                    vehicle.getSpawnTime()
            ),
            vehicle.getImageResourceIndex(),
            switch (vehicle) {
                case Truck _ -> VehicleType.Truck;
                case Car _ -> VehicleType.Car;
            },
            vehicle.hasTarget(),
            vehicle.isArrived()
        );
    }
}