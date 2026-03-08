package org.penakelex.objectsimulationsystem.habitat;

import javafx.scene.canvas.GraphicsContext;
import org.apache.commons.lang3.tuple.Pair;
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
    private final List<Vehicle> vehicles = new ArrayList<>();
    private int idCounter = 0;

    private double width, height;

    private final TruckImages truckImages = new TruckImages();
    private final CarImages carImages = new CarImages();

    private final VehicleSpawner<Truck> truckSpawner =
        new VehicleSpawner<>(
            Configuration.TRUCK_SPAWN_PERIOD,
            Configuration.TRUCK_SPAWN_PROBABILITY,
            Truck::new,
            truckImages
        );
    private final VehicleSpawner<Car> carSpawner =
        new VehicleSpawner<>(
            Configuration.CAR_SPAWN_PERIOD,
            Configuration.CAR_SPAWN_PROBABILITY,
            Car::new,
            carImages
        );

    private final List<VehicleSpawner<?>> vehicleSpawners =
        List.of(truckSpawner, carSpawner);

    private final Random random = ThreadLocalRandom.current();

    public Habitat(final double width, final double height) {
        this.width = width;
        this.height = height;
    }

    public void update(final long currentTimeMillis) {
        for (final var spawner : vehicleSpawners) {
            spawner
                .trySpawn(
                    currentTimeMillis,
                    this::generateVehicleStartingRelativePosition,
                    this::getNextId
                ).ifPresent((vehicle) -> {
                    vehicle.updateAbsoluteXPosition(this.width);
                    vehicle.updateAbsoluteYPosition(this.height);
                    vehicles.add(vehicle);
                });
        }

        for (final var vehicle : vehicles) {
            vehicle.update(currentTimeMillis);
        }
    }

    private int getNextId() {
        return ++idCounter;
    }

    private Pair<Double, Double>
    generateVehicleStartingRelativePosition() {
        return Pair.of(
            random.nextDouble() *
                (1 - Configuration.VEHICLE_RELATIVE_SIZE),
            random.nextDouble() *
                (1 - Configuration.VEHICLE_RELATIVE_SIZE)
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
        vehicleSpawners.forEach(VehicleSpawner::reset);
    }

    public void setWidth(final double width) {
        if (this.width == width) {
            return;
        }

        this.width = width;

        for (final var vehicle : vehicles) {
            vehicle.updateAbsoluteXPosition(this.width);
        }
    }

    public void setHeight(final double height) {
        if (this.height == height) {
            return;
        }

        this.height = height;

        for (final var vehicle : vehicles) {
            vehicle.updateAbsoluteYPosition(this.height);
        }
    }

    public VehicleStatistics getStatistics() {
        int trucks = 0, cars = 0;

        for (final var vehicle : vehicles) {
            switch (vehicle) {
                case Car _ -> cars++;
                case Truck _ -> trucks++;
            }
        }

        return new VehicleStatistics(trucks, cars, trucks + cars);
    }
}
