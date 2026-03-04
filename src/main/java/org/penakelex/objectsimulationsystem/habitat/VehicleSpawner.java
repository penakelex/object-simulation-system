package org.penakelex.objectsimulationsystem.habitat;

import org.apache.commons.lang3.tuple.Pair;
import org.penakelex.objectsimulationsystem.vehicle.Vehicle;
import org.penakelex.objectsimulationsystem.vehicle.images.VehicleImages;

import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public final class VehicleSpawner<T extends Vehicle> {
    private final int periodMillis;
    private final double probability;
    private final VehicleFactory<T> factory;
    private final VehicleImages images;

    private long lastSpawnTime = 0;
    private final Random random = ThreadLocalRandom.current();

    public VehicleSpawner(
        final int periodMillis,
        final double probability,
        final VehicleFactory<T> factory,
        final VehicleImages images
    ) {
        this.factory = factory;
        this.images = images;
        this.periodMillis = periodMillis;
        this.probability = probability;
    }

    public Optional<T> trySpawn(
        final long currentTimeMillis,
        final PositionGenerator positionGenerator,
        final IdSupplier idSupplier
    ) {
        if (currentTimeMillis - lastSpawnTime < periodMillis) {
            return Optional.empty();
        }

        lastSpawnTime = currentTimeMillis;

        if (random.nextDouble() > probability) {
            return Optional.empty();
        }

        final var image = images.getRandomImage();

        final var position = positionGenerator.generate(
            image.getWidth(),
            image.getHeight()

        );

        return Optional.of(factory.create(
            idSupplier.getNextId(),
            position.getLeft(),
            position.getRight(),
            currentTimeMillis,
            image
        ));
    }

    public void reset() {
        lastSpawnTime = 0;
    }

    @FunctionalInterface
    public interface PositionGenerator {
        Pair<Double, Double> generate(
            final double width, final double height
        );
    }

    @FunctionalInterface
    public interface IdSupplier {
        int getNextId();
    }
}
