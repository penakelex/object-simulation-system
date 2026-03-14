package org.penakelex.objectsimulationsystem.habitat;

import javafx.scene.canvas.GraphicsContext;
import org.apache.commons.lang3.tuple.Pair;
import org.penakelex.objectsimulationsystem.habitat.exceptions.HabitatCreationException;
import org.penakelex.objectsimulationsystem.habitat.exceptions.HabitatInvalidParameter;
import org.penakelex.objectsimulationsystem.vehicle.Car;
import org.penakelex.objectsimulationsystem.vehicle.Truck;
import org.penakelex.objectsimulationsystem.vehicle.Vehicle;
import org.penakelex.objectsimulationsystem.vehicle.images.CarImages;
import org.penakelex.objectsimulationsystem.vehicle.images.TruckImages;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public final class Habitat {
    private static final double MAX_RELATIVE_VEHICLE_POSITION =
        1 - Configuration.VEHICLE_RELATIVE_SIZE;

    private final List<Vehicle> vehicles = new ArrayList<>(500);
    private int idCounter = 0;

    private int trucksCount = 0, carsCount = 0;
    private boolean statisticsDirty = true;

    private double width, height;

    private final VehicleSpawner<Truck> truckSpawner;
    private final VehicleSpawner<Car> carSpawner;

    private final List<VehicleSpawner<? extends Vehicle>>
        vehicleSpawners;

    private final Random random = ThreadLocalRandom.current();

    public Habitat(
        final double width,
        final double height,
        final TruckImages truckImages,
        final CarImages carImages
    ) {
        final var invalidParameters =
            validateParameters(width, height);

        if (invalidParameters != null) {
            throw new HabitatCreationException(invalidParameters);
        }

        this.width = width;
        this.height = height;

        truckSpawner = new VehicleSpawner<>(
            Configuration.TRUCK_SPAWN_PERIOD_MILLIS,
            Configuration.TRUCK_SPAWN_PROBABILITY,
            Truck::new,
            truckImages,
            Truck.class
        );
        carSpawner = new VehicleSpawner<>(
            Configuration.CAR_SPAWN_PERIOD_MILLIS,
            Configuration.CAR_SPAWN_PROBABILITY,
            Car::new,
            carImages,
            Car.class
        );

        vehicleSpawners = List.of(truckSpawner, carSpawner);
    }

    private static List<HabitatInvalidParameter> validateParameters(
        final double width,
        final double height
    ) {
        List<HabitatInvalidParameter> invalidParameters = null;

        if (width < 0) {
            invalidParameters = new ArrayList<>(2);
            invalidParameters.add(new HabitatInvalidParameter.Width(
                width
            ));
        }

        if (height < 0) {
            if (invalidParameters == null) {
                invalidParameters = new ArrayList<>(1);
            }

            invalidParameters.add(new HabitatInvalidParameter.Height(
                height
            ));
        }

        return invalidParameters;
    }

    public void update(final long currentTimeMillis) {
        for (final var spawner : vehicleSpawners) {
            final var spawnedVehicles = spawner.trySpawn(
                currentTimeMillis,
                this::generateVehicleStartingRelativePosition,
                this::getNextId
            );

            if (!spawnedVehicles.isEmpty()) {
                statisticsDirty = true;

                if (spawner.getVehicleType() == Truck.class) {
                    trucksCount += spawnedVehicles.size();
                } else if (spawner.getVehicleType() == Car.class) {
                    carsCount += spawnedVehicles.size();
                }
            }

            for (final var newVehicle : spawnedVehicles) {
                newVehicle
                    .onCanvasSizeUpdated(this.width, this.height);
                vehicles.add(newVehicle);
            }
        }

        for (final var vehicle : vehicles) {
            vehicle.update(currentTimeMillis);
        }
    }

    private int getNextId() {
        return idCounter++;
    }

    private Pair<Double, Double>
    generateVehicleStartingRelativePosition() {
        return Pair.of(
            random.nextDouble() * MAX_RELATIVE_VEHICLE_POSITION,
            random.nextDouble() * MAX_RELATIVE_VEHICLE_POSITION
        );
    }

    public void draw(final GraphicsContext context) {
        for (final var vehicle : vehicles) {
            vehicle.draw(context);
        }
    }

    public void reset() {
        vehicles.clear();
        idCounter = 0;
        trucksCount = 0;
        carsCount = 0;
        statisticsDirty = true;
        vehicleSpawners.forEach(VehicleSpawner::reset);
    }

    public void setSize(final double width, final double height) {
        if (this.width == width && this.height == height) {
            return;
        }

        this.width = width;
        this.height = height;

        for (final var vehicle : vehicles) {
            vehicle.onCanvasSizeUpdated(width, height);
        }
    }

    public boolean isStatisticsDirty() {
        return statisticsDirty;
    }

    public VehicleStatistics getStatistics() {
        statisticsDirty = false;
        return new VehicleStatistics(
            trucksCount,
            carsCount,
            trucksCount + carsCount
        );
    }
}
