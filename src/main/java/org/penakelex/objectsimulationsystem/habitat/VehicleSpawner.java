package org.penakelex.objectsimulationsystem.habitat;

import org.apache.commons.lang3.tuple.Pair;
import org.penakelex.objectsimulationsystem.vehicle.Vehicle;
import org.penakelex.objectsimulationsystem.vehicle.images.VehicleImages;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public final class VehicleSpawner<T extends Vehicle> {
    private int period;
    private TimeUnit periodTimeUnit;
    private double probability;
    private final VehicleFactory<T> factory;
    private final VehicleImages images;

    private int periodMillis;

    private final Class<T> vehicleType;

    private long lastSpawnTime = 0;
    private final Random random = ThreadLocalRandom.current();

    public VehicleSpawner(
        final int period,
        final TimeUnit periodTimeUnit,
        final double probability,
        final VehicleFactory<T> factory,
        final VehicleImages images,
        final Class<T> vehicleType
    ) {
        this.period = period;
        this.periodTimeUnit = periodTimeUnit;
        this.probability = probability;
        this.factory = factory;
        this.images = images;
        this.vehicleType = vehicleType;

        updatePeriodMillis();
    }

    public Class<T> getVehicleType() {
        return vehicleType;
    }

    public void updatePeriod(final int period) {
        this.period = period;
        updatePeriodMillis();
    }

    public void updatePeriodTimeUnit(final TimeUnit periodTimeUnit) {
        this.periodTimeUnit = periodTimeUnit;
        updatePeriodMillis();
    }

    private void updatePeriodMillis() {
        this.periodMillis = period * periodTimeUnit.millisModifier;
    }

    public void updateProbability(final double probability) {
        this.probability = probability;
    }

    public List<T> trySpawn(
        final long currentTimeMillis,
        final RelativePositionGenerator relativePositionGenerator,
        final IdSupplier idSupplier
    ) {
        final var elapsedSinceLastSpawn =
            currentTimeMillis - lastSpawnTime;

        if (elapsedSinceLastSpawn < periodMillis) {
            return List.of();
        }

        final var spawnsNeeded =
            (int) (elapsedSinceLastSpawn / periodMillis);

        lastSpawnTime += (long) spawnsNeeded * periodMillis;

        final var newVehicles = new ArrayList<T>(spawnsNeeded);

        for (int i = 0; i < spawnsNeeded; i++) {
            if (random.nextDouble() > probability) {
                continue;
            }

            final var position =
                relativePositionGenerator.generate();

            newVehicles.add(
                factory.create(
                    idSupplier.getNextId(),
                    position.getLeft(),
                    position.getRight(),
                    currentTimeMillis,
                    images.getRandomImage()
                )
            );
        }

        return newVehicles;
    }

    public void reset() {
        lastSpawnTime = 0;
    }

    @FunctionalInterface
    public interface RelativePositionGenerator {
        Pair<Double, Double> generate();
    }

    @FunctionalInterface
    public interface IdSupplier {
        int getNextId();
    }
}
