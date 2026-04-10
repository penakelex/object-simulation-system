package org.penakelex.objectsimulationsystem.model.habitat;

import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public final class Configuration {
    public static final int TRUCK_SPAWN_PERIOD;
    public static final TimeUnit TRUCK_SPAWN_TIME_UNIT;
    public static final double TRUCK_SPAWN_PROBABILITY;
    public static final int TRUCK_LIFETIME;
    public static final TimeUnit TRUCK_LIFETIME_TIME_UNIT;
    public static final int TRUCK_MOVEMENT_SPEED;

    public static final int CAR_SPAWN_PERIOD;
    public static final TimeUnit CAR_SPAWN_TIME_UNIT;
    public static final double CAR_SPAWN_PROBABILITY;
    public static final int CAR_LIFETIME;
    public static final TimeUnit CAR_LIFETIME_TIME_UNIT;
    public static final int CAR_MOVEMENT_SPEED;

    public static final double VEHICLE_RELATIVE_SIZE;
    public static final int VEHICLE_IMAGE_SIZE;
    public static final String VEHICLE_IMAGES_BASE_PATH;
    public static final String VEHICLE_IMAGES_CARS_PATH;
    public static final String VEHICLE_IMAGES_TRUCKS_PATH;
    public static final String[] VEHICLE_IMAGE_EXTENSIONS;

    static {
        final var configuration =
            ResourceBundle.getBundle("configuration");

        TRUCK_SPAWN_PERIOD = validatePositiveInt(
            configuration,
            "habitat.truck.spawn.period",
            "Truck spawn period"
        );
        TRUCK_SPAWN_TIME_UNIT = validateTimeUnitString(
            configuration,
            "habitat.truck.spawn.period.time.unit"
        );
        TRUCK_SPAWN_PROBABILITY = validateProbability(
            configuration,
            "habitat.truck.spawn.probability",
            "Truck spawn"
        );
        TRUCK_LIFETIME = validatePositiveInt(
            configuration,
            "habitat.truck.lifetime",
            "Truck lifetime"
        );
        TRUCK_LIFETIME_TIME_UNIT = validateTimeUnitString(
            configuration,
            "habitat.truck.lifetime.time.unit"
        );
        TRUCK_MOVEMENT_SPEED = validatePositiveInt(
            configuration,
            "habitat.truck.movement.speed",
            "Truck movement speed"
        );

        CAR_SPAWN_PERIOD = validatePositiveInt(
            configuration,
            "habitat.car.spawn.period",
            "Car spawn period"
        );
        CAR_SPAWN_TIME_UNIT = validateTimeUnitString(
            configuration,
            "habitat.car.spawn.time.unit"
        );
        CAR_SPAWN_PROBABILITY = validateProbability(
            configuration,
            "habitat.car.spawn.probability",
            "Car spawn"
        );
        CAR_LIFETIME = validatePositiveInt(
            configuration,
            "habitat.car.lifetime",
            "Car lifetime"
        );
        CAR_LIFETIME_TIME_UNIT = validateTimeUnitString(
            configuration,
            "habitat.car.lifetime.time.unit"
        );
        CAR_MOVEMENT_SPEED = validatePositiveInt(
            configuration,
            "habitat.car.movement.speed",
            "Car movement speed"
        );

        VEHICLE_RELATIVE_SIZE = validateRange(
            configuration,
            "vehicle.relative.size",
            "Vehicle relative size",
            0.0,
            1.0
        );
        VEHICLE_IMAGE_SIZE = validatePositiveInt(
            configuration,
            "vehicle.image.size",
            "Vehicle image size"
        );
        VEHICLE_IMAGES_BASE_PATH = configuration.getString(
            "vehicle.images.base.path"
        );
        VEHICLE_IMAGES_CARS_PATH = configuration.getString(
            "vehicle.images.cars.path"
        );
        VEHICLE_IMAGES_TRUCKS_PATH = configuration.getString(
            "vehicle.images.trucks.path"
        );
        VEHICLE_IMAGE_EXTENSIONS = configuration
            .getString("vehicle.image.extensions")
            .split(",");
    }

    private Configuration() {
        throw new UnsupportedOperationException("Utility class");
    }

    private static int validatePositiveInt(
        final ResourceBundle config,
        final String key,
        final String parameterName
    ) {
        final var value = Integer.parseInt(config.getString(key));
        if (value < 1) {
            throw new IllegalArgumentException(
                "%s must be positive: %d".formatted(
                    parameterName, value
                )
            );
        }
        return value;
    }

    private static double validateProbability(
        final ResourceBundle config,
        final String key,
        final String parameterName
    ) {
        final var value = Double.parseDouble(config.getString(key));
        if (value <= 0.0 || value > 1.0) {
            throw new IllegalArgumentException(
                "%s probability must be in [0, 1]: %f".formatted(
                    parameterName, value
                )
            );
        }
        return value;
    }

    private static double validateRange(
        final ResourceBundle config,
        final String key,
        final String parameterName,
        final double min,
        final double max
    ) {
        final var value = Double.parseDouble(config.getString(key));
        if (value <= min || value > max) {
            throw new IllegalArgumentException(
                "%s must be in (%f, %f]: %f"
                    .formatted(parameterName, min, max, value)
            );
        }
        return value;
    }

    private static TimeUnit validateTimeUnitString(
        final ResourceBundle config,
        final String key
    ) {
        final var configTimeUnit = config.getString(key);
        final var optionalTimeUnit = Arrays.stream(TimeUnit.values())
            .filter(timeUnit -> timeUnit.literal.equals(configTimeUnit))
            .findFirst();

        if (optionalTimeUnit.isPresent()) {
            return optionalTimeUnit.get();
        }

        throw new IllegalArgumentException(
            String.format(
                "Time unit must be from [%s] but got %s",
                Arrays
                    .stream(TimeUnit.values())
                    .map(unit -> unit.literal)
                    .collect(Collectors.joining(", ")),
                configTimeUnit
            )
        );
    }
}