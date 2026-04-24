package org.penakelex.objectsimulationsystem.model.habitat;

import org.apache.commons.lang3.tuple.Pair;
import org.penakelex.objectsimulationsystem.model.vehicle.Vehicle;
import org.penakelex.objectsimulationsystem.model.vehicle.images.VehicleImages;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

public final class VehicleSpawner<T extends Vehicle> {
    private int period;
    private TimeUnit periodTimeUnit;

    private double probability;

    private int lifeTime;
    private TimeUnit lifeTimeUnit;

    private final VehicleFactory<T> factory;
    private final VehicleImages images;

    private int periodMillis;
    private long lifeTimeMillis;

    private final Class<T> vehicleType;

    private long lastSpawnTime = 0;
    private final Random random = ThreadLocalRandom.current();

    public VehicleSpawner(
        final int period,
        final TimeUnit periodTimeUnit,
        final double probability,
        final int lifeTime,
        final TimeUnit lifeTimeUnit,
        final VehicleFactory<T> factory,
        final VehicleImages images,
        final Class<T> vehicleType
    ) {
        this.period = period;
        this.periodTimeUnit = periodTimeUnit;
        this.probability = probability;
        this.lifeTime = lifeTime;
        this.lifeTimeUnit = lifeTimeUnit;
        this.factory = factory;
        this.images = images;
        this.vehicleType = vehicleType;

        updatePeriodMillis();
        updateLifeTimeMillis();
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

    public void updateLifeTime(final int lifeTime) {
        this.lifeTime = lifeTime;
        updateLifeTimeMillis();
    }

    public void updateLifeTimeUnit(final TimeUnit lifeTimeUnit) {
        this.lifeTimeUnit = lifeTimeUnit;
        updateLifeTimeMillis();
    }

    private void updateLifeTimeMillis() {
        lifeTimeMillis =
            (long) lifeTime * lifeTimeUnit.millisModifier;
    }

    public List<T> trySpawn(
        final long currentTimeMillis,
        final Supplier<Pair<Double, Double>> relativePositionGenerator,
        final Supplier<Integer> idSupplier
    ) {
        final var spawnsNeeded =
            (int) ((currentTimeMillis - lastSpawnTime) /
                periodMillis);

        if (spawnsNeeded == 0) {
            return List.of();
        }

        final var newVehicles = new ArrayList<T>(spawnsNeeded);

        for (int i = 0; i < spawnsNeeded; i++) {
            lastSpawnTime += periodMillis;

            if (random.nextDouble() > probability) {
                continue;
            }

            final var position = relativePositionGenerator.get();

            newVehicles.add(
                factory.create(
                    idSupplier.get(),
                    position.getLeft(),
                    position.getRight(),
                    lastSpawnTime,
                    lifeTimeMillis,
                    images.getRandomImage()
                )
            );
        }

        return newVehicles;
    }

    public void reset() {
        lastSpawnTime = 0;
    }
}
