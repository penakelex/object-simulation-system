package org.penakelex.objectsimulationsystem.model.habitat;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.apache.commons.lang3.tuple.Pair;
import org.penakelex.objectsimulationsystem.model.ai.CarAI;
import org.penakelex.objectsimulationsystem.model.ai.TruckAI;
import org.penakelex.objectsimulationsystem.model.collection.VehicleCollection;
import org.penakelex.objectsimulationsystem.model.habitat.exceptions.HabitatCreationException;
import org.penakelex.objectsimulationsystem.model.habitat.exceptions.HabitatInvalidParameter;
import org.penakelex.objectsimulationsystem.model.vehicle.Car;
import org.penakelex.objectsimulationsystem.model.vehicle.Truck;
import org.penakelex.objectsimulationsystem.model.vehicle.Vehicle;
import org.penakelex.objectsimulationsystem.model.vehicle.images.CarImages;
import org.penakelex.objectsimulationsystem.model.vehicle.images.TruckImages;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public final class Habitat {
    private static final double MAX_RELATIVE_VEHICLE_POSITION =
        1 - Configuration.VEHICLE_RELATIVE_SIZE;

    private boolean statisticsDirty = true;
    private double width, height;

    private final VehicleSpawner<Truck> truckSpawner;
    private final VehicleSpawner<Car> carSpawner;
    private final
    List<VehicleSpawner<? extends Vehicle>> vehicleSpawners;

    private final TruckAI truckAI;
    private final CarAI carAI;

    private final Random random = ThreadLocalRandom.current();
    private final VehicleCollection vehicleCollection;

    private final AtomicInteger
        trucksCount = new AtomicInteger(0),
        carsCount = new AtomicInteger(0),
        currentTrucksCount = new AtomicInteger(0),
        currentCarsCount = new AtomicInteger(0);

    private final List<Integer> expiredIdsBuffer =
        new ArrayList<>(16);

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
        this.vehicleCollection = VehicleCollection.getInstance();
        this.vehicleCollection.clear();

        truckSpawner = new VehicleSpawner<>(
            Configuration.TRUCK_SPAWN_PERIOD,
            Configuration.TRUCK_SPAWN_TIME_UNIT,
            Configuration.TRUCK_SPAWN_PROBABILITY,
            Configuration.TRUCK_LIFETIME,
            Configuration.TRUCK_LIFETIME_TIME_UNIT,
            Truck::new,
            truckImages,
            Truck.class
        );
        carSpawner = new VehicleSpawner<>(
            Configuration.CAR_SPAWN_PERIOD,
            Configuration.CAR_SPAWN_TIME_UNIT,
            Configuration.CAR_SPAWN_PROBABILITY,
            Configuration.CAR_LIFETIME,
            Configuration.CAR_LIFETIME_TIME_UNIT,
            Car::new,
            carImages,
            Car.class
        );
        vehicleSpawners = List.of(truckSpawner, carSpawner);

        truckAI = new TruckAI(
            () -> vehicleCollection.getAll()
                .stream()
                .filter(vehicle -> vehicle instanceof Truck)
                .map(vehicle -> (Truck) vehicle)
                .iterator(),
            Configuration.TRUCK_MOVEMENT_SPEED
        );

        carAI = new CarAI(
            () -> vehicleCollection.getAll()
                .stream()
                .filter(vehicle -> vehicle instanceof Car)
                .map(vehicle -> (Car) vehicle)
                .iterator(),
            Configuration.CAR_MOVEMENT_SPEED
        );
    }

    private static List<HabitatInvalidParameter> validateParameters(
        final double width,
        final double height
    ) {
        List<HabitatInvalidParameter> invalidParameters = null;

        if (width < 0) {
            invalidParameters = new ArrayList<>(2);
            invalidParameters.add(
                new HabitatInvalidParameter.Width(width)
            );
        }

        if (height < 0) {
            if (invalidParameters == null) {
                invalidParameters = new ArrayList<>(1);
            }

            invalidParameters.add(
                new HabitatInvalidParameter.Height(height)
            );
        }

        return invalidParameters;
    }

    public void updateTruckPeriod(final int truckPeriod) {
        truckSpawner.updatePeriod(truckPeriod);
    }

    public void updateTruckPeriodTimeUnit(
        final TimeUnit truckPeriodTimeUnit
    ) {
        truckSpawner.updatePeriodTimeUnit(truckPeriodTimeUnit);
    }

    public void updateTruckProbability(final double truckProbability) {
        truckSpawner.updateProbability(truckProbability);
    }

    public void updateTruckLifeTime(final int truckLifeTime) {
        truckSpawner.updateLifeTime(truckLifeTime);
    }

    public void updateTruckLifeTimeUnit(
        final TimeUnit truckLifeTimeUnit
    ) {
        truckSpawner.updateLifeTimeUnit(truckLifeTimeUnit);
    }

    public void updateCarPeriod(final int carPeriod) {
        carSpawner.updatePeriod(carPeriod);
    }

    public void updateCarPeriodTimeUnit(
        final TimeUnit carPeriodTimeUnit
    ) {
        carSpawner.updatePeriodTimeUnit(carPeriodTimeUnit);
    }

    public void updateCarProbability(final double carProbability) {
        carSpawner.updateProbability(carProbability);
    }

    public void updateCarLifeTime(final int carLifeTime) {
        carSpawner.updateLifeTime(carLifeTime);
    }

    public void updateCarLifeTimeUnit(
        final TimeUnit carLifeTimeUnit
    ) {
        carSpawner.updateLifeTimeUnit(carLifeTimeUnit);
    }

    public VehicleCollection getVehicleCollection() {
        return vehicleCollection;
    }

    public void update(final long currentTimeMillis) {
        for (final var spawner : vehicleSpawners) {
            final var spawnedVehicles = spawner.trySpawn(
                currentTimeMillis,
                this::generateVehicleStartingRelativePosition,
                vehicleCollection::getNextId
            );

            if (spawnedVehicles.isEmpty()) {
                continue;
            }

            statisticsDirty = true;

            if (spawner.getVehicleType() == Truck.class) {
                trucksCount.addAndGet(spawnedVehicles.size());
                currentTrucksCount.addAndGet(spawnedVehicles.size());
            } else if (spawner.getVehicleType() == Car.class) {
                carsCount.addAndGet(spawnedVehicles.size());
                currentCarsCount.addAndGet(spawnedVehicles.size());
            }

            for (final var newVehicle : spawnedVehicles) {
                newVehicle.onCanvasSizeUpdated(width, height);
                vehicleCollection.add(newVehicle);
            }
        }

        for (final var vehicle : vehicleCollection.getAll()) {
            if (!vehicle.isExpired(currentTimeMillis)) {
                continue;
            }

            expiredIdsBuffer.add(vehicle.getId());

            switch (vehicle) {
                case Truck _ -> currentTrucksCount.decrementAndGet();
                case Car _ -> currentCarsCount.decrementAndGet();
            }
        }

        if (!expiredIdsBuffer.isEmpty()) {
            statisticsDirty = true;
            vehicleCollection.removeByIds(expiredIdsBuffer);
            expiredIdsBuffer.clear();
        }
    }

    private Pair<Double, Double>
    generateVehicleStartingRelativePosition() {
        return Pair.of(
            random.nextDouble() * MAX_RELATIVE_VEHICLE_POSITION,
            random.nextDouble() * MAX_RELATIVE_VEHICLE_POSITION
        );
    }

    public void startAI() {
        truckAI.start();
        carAI.start();
    }

    public void pauseAI() {
        truckAI.onSimulationPause();
        carAI.onSimulationPause();
    }

    public void pauseTruckAI() {
        truckAI.pauseByUser();
    }

    public void resumeTruckAI() {
        truckAI.resumeByUser();
    }

    public void pauseCarAI() {
        carAI.pauseByUser();
    }

    public void resumeCarAI() {
        carAI.resumeByUser();
    }

    public void setTruckAIPriority(final int priority) {
        truckAI.setPriority(priority);
    }

    public void setCarAIPriority(final int priority) {
        carAI.setPriority(priority);
    }

    public void updateTruckSpeed(final double speed) {
        truckAI.updateSpeed(speed);
    }

    public void updateCarSpeed(final double speed) {
        carAI.updateSpeed(speed);
    }

    public void draw(final GraphicsContext context) {
        drawEnvironment(context);
        for (final var vehicle : vehicleCollection.getAll()) {
            vehicle.draw(context);
        }
    }

    private void drawEnvironment(final GraphicsContext context) {
        final double halfWidth = width * 0.5;
        final double halfHeight = height * 0.5;
        final double roadWidth =
            Math.max(24.0, Math.min(width, height) * 0.04);

        context.setFill(Color.rgb(230, 230, 230));
        context.fillRect(0, 0, width, height);

        context.setFill(Color.rgb(70, 70, 70));
        context.fillRect(halfWidth - roadWidth / 2.0,
            0,
            roadWidth,
            height
        );
        context.fillRect(0,
            halfHeight - roadWidth / 2.0,
            width,
            roadWidth
        );

        context.setStroke(Color.YELLOW);
        context.setLineWidth(2);
        context.setLineDashes(8, 8);
        context.strokeLine(halfWidth, 0, halfWidth, height);
        context.strokeLine(0, halfHeight, width, halfHeight);
        context.setLineDashes();

        drawParkingZone(context,
            0,
            0,
            halfWidth,
            halfHeight,
            Color.rgb(76, 175, 80, 0.15),
            Color.GREEN
        );
        drawParkingZone(context,
            halfWidth,
            halfHeight,
            halfWidth,
            halfHeight,
            Color.rgb(33, 150, 243, 0.15),
            Color.BLUE
        );
    }

    private void drawParkingZone(
        final GraphicsContext context,
        final double x,
        final double y,
        final double width,
        final double height,
        final Color fill,
        final Color stroke
    ) {
        context.setFill(fill);
        context.fillRect(x + 6, y + 6, width - 12, height - 12);

        context.setStroke(stroke);
        context.setLineWidth(3);
        context.setLineDashes(12, 6);
        context.strokeRect(x + 6, y + 6, width - 12, height - 12);
        context.setLineDashes();
    }

    public void reset() {
        vehicleCollection.clear();
        trucksCount.set(0);
        carsCount.set(0);
        currentCarsCount.set(0);
        currentTrucksCount.set(0);
        statisticsDirty = true;
        vehicleSpawners.forEach(VehicleSpawner::reset);
    }

    public void setSize(final double width, final double height) {
        if (this.width == width && this.height == height) {
            return;
        }

        this.width = width;
        this.height = height;

        for (final var vehicle : vehicleCollection.getAll()) {
            vehicle.onCanvasSizeUpdated(width, height);
        }
    }

    public boolean isStatisticsDirty() {
        return statisticsDirty;
    }

    public VehicleStatistics getStatistics() {
        statisticsDirty = false;

        final var trucksCount = this.trucksCount.get();
        final var carsCount = this.carsCount.get();

        return new VehicleStatistics(
            trucksCount,
            carsCount,
            trucksCount + carsCount,
            currentTrucksCount.get(),
            currentCarsCount.get()
        );
    }
}