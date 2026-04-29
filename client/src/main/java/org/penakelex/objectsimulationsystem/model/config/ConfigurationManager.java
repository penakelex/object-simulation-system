package org.penakelex.objectsimulationsystem.model.config;

import org.penakelex.objectsimulationsystem.model.habitat.Configuration;
import org.penakelex.objectsimulationsystem.model.habitat.TimeUnit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

public final class ConfigurationManager {
    private ConfigurationManager() {
        throw new UnsupportedOperationException("Utility class");
    }

    private static final Path CONFIG_PATH =
        Path.of("simulation-config.properties");
    private static final Properties properties = new Properties();

    // ========== Ключи конфигурации ==========
    private static final String K_TRUCK_PERIOD = "truck.spawn.period";
    private static final String K_TRUCK_PERIOD_UNIT =
        "truck.spawn.period.unit";
    private static final String K_TRUCK_PROB =
        "truck.spawn.probability";
    private static final String K_TRUCK_LIFETIME = "truck.lifetime";
    private static final String K_TRUCK_LIFETIME_U =
        "truck.lifetime.unit";
    private static final String K_TRUCK_SPEED =
        "truck.movement.speed";

    private static final String K_CAR_PERIOD = "car.spawn.period";
    private static final String K_CAR_PERIOD_UNIT =
        "car.spawn.period.unit";
    private static final String K_CAR_PROB = "car.spawn.probability";
    private static final String K_CAR_LIFETIME = "car.lifetime";
    private static final String K_CAR_LIFETIME_U =
        "car.lifetime.unit";
    private static final String K_CAR_SPEED = "car.movement.speed";

    private static final String K_VIEW_SHOW_TIME = "view.show.time";
    private static final String K_VIEW_SHOW_STATS =
        "view.show.statistics";
    private static final String K_AI_TRUCK_PAUSED = "ai.truck.paused";
    private static final String K_AI_CAR_PAUSED = "ai.car.paused";
    private static final String K_AI_TRUCK_PRIORITY =
        "ai.truck.priority";
    private static final String K_AI_CAR_PRIORITY = "ai.car.priority";

    private static final String K_SERVER_HOST = "server.host";
    private static final String K_SERVER_PORT = "server.port";

    // ========== I/O ==========
    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try (final var reader =
                     Files.newBufferedReader(CONFIG_PATH)
            ) {
                properties.load(reader);
            } catch (final IOException _) {
            }
        }
    }

    public static void save() {
        try (var writer = Files.newBufferedWriter(CONFIG_PATH)) {
            properties.store(writer,
                "Simulation Configuration - Auto-generated"
            );
        } catch (final IOException exception) {
            IO.println(String.format("Failed to save config: %s",
                exception.getMessage()
            ));
        }
    }

    // ========== Хелперы чтения/записи ==========
    private static int getInt(
        final String key,
        final int defaultValue
    ) {
        return Optional.ofNullable(properties.getProperty(key))
            .map(Integer::parseInt)
            .orElse(defaultValue);
    }

    private static void setInt(final String key, final int value) {
        properties.setProperty(key, String.valueOf(value));
    }

    private static double getDouble(
        final String key,
        final double defaultValue
    ) {
        return Optional.ofNullable(properties.getProperty(key))
            .map(Double::parseDouble)
            .orElse(defaultValue);
    }

    private static void setDouble(
        final String key,
        final double value
    ) {
        properties.setProperty(key, String.valueOf(value));
    }

    private static boolean getBoolean(
        final String key,
        boolean defaultValue
    ) {
        return Optional.ofNullable(properties.getProperty(key))
            .map(Boolean::parseBoolean)
            .orElse(defaultValue);
    }

    private static void setBoolean(
        final String key,
        final boolean value
    ) {
        properties.setProperty(key, String.valueOf(value));
    }

    private static TimeUnit getTimeUnit(
        final String key,
        final TimeUnit defaultValue
    ) {
        return Optional.ofNullable(properties.getProperty(key))
            .map(literal -> Stream.of(TimeUnit.values())
                .filter(timeUnit ->
                    timeUnit.literal.equals(literal)
                )
                .findFirst()
                .orElse(defaultValue)
            )
            .orElse(defaultValue);
    }

    private static void setTimeUnit(
        final String key,
        final TimeUnit unit
    ) {
        properties.setProperty(key, unit.literal);
    }

    // ========== Truck ==========
    public static int getTruckPeriod() {
        return getInt(K_TRUCK_PERIOD,
            Configuration.TRUCK_SPAWN_PERIOD
        );
    }

    public static void setTruckPeriod(final int value) {
        setInt(K_TRUCK_PERIOD, value);
    }

    public static TimeUnit getTruckPeriodUnit() {
        return getTimeUnit(K_TRUCK_PERIOD_UNIT,
            Configuration.TRUCK_SPAWN_TIME_UNIT
        );
    }

    public static void setTruckPeriodUnit(final TimeUnit value) {
        setTimeUnit(K_TRUCK_PERIOD_UNIT, value);
    }

    public static double getTruckProbability() {
        return getDouble(K_TRUCK_PROB,
            Configuration.TRUCK_SPAWN_PROBABILITY
        );
    }

    public static void setTruckProbability(final double value) {
        setDouble(K_TRUCK_PROB, value);
    }

    public static int getTruckLifetime() {
        return getInt(K_TRUCK_LIFETIME, Configuration.TRUCK_LIFETIME);
    }

    public static void setTruckLifetime(final int value) {
        setInt(K_TRUCK_LIFETIME, value);
    }

    public static TimeUnit getTruckLifetimeUnit() {
        return getTimeUnit(K_TRUCK_LIFETIME_U,
            Configuration.TRUCK_LIFETIME_TIME_UNIT
        );
    }

    public static void setTruckLifetimeUnit(final TimeUnit value) {
        setTimeUnit(K_TRUCK_LIFETIME_U, value);
    }

    public static int getTruckSpeed() {
        return getInt(K_TRUCK_SPEED,
            Configuration.TRUCK_MOVEMENT_SPEED
        );
    }

    public static void setTruckSpeed(final int value) {
        setInt(K_TRUCK_SPEED, value);
    }

    // ========== Car ==========
    public static int getCarPeriod() {
        return getInt(K_CAR_PERIOD, Configuration.CAR_SPAWN_PERIOD);
    }

    public static void setCarPeriod(final int value) {
        setInt(K_CAR_PERIOD, value);
    }

    public static TimeUnit getCarPeriodUnit() {
        return getTimeUnit(K_CAR_PERIOD_UNIT,
            Configuration.CAR_SPAWN_TIME_UNIT
        );
    }

    public static void setCarPeriodUnit(final TimeUnit value) {
        setTimeUnit(K_CAR_PERIOD_UNIT, value);
    }

    public static double getCarProbability() {
        return getDouble(K_CAR_PROB,
            Configuration.CAR_SPAWN_PROBABILITY
        );
    }

    public static void setCarProbability(final double value) {
        setDouble(K_CAR_PROB, value);
    }

    public static int getCarLifetime() {
        return getInt(K_CAR_LIFETIME, Configuration.CAR_LIFETIME);
    }

    public static void setCarLifetime(final int value) {
        setInt(K_CAR_LIFETIME, value);
    }

    public static TimeUnit getCarLifetimeUnit() {
        return getTimeUnit(K_CAR_LIFETIME_U,
            Configuration.CAR_LIFETIME_TIME_UNIT
        );
    }

    public static void setCarLifetimeUnit(final TimeUnit value) {
        setTimeUnit(K_CAR_LIFETIME_U, value);
    }

    public static int getCarSpeed() {
        return getInt(K_CAR_SPEED, Configuration.CAR_MOVEMENT_SPEED);
    }

    public static void setCarSpeed(final int value) {
        setInt(K_CAR_SPEED, value);
    }

    // ========== View & AI ==========
    public static boolean isShowTime() {
        return getBoolean(K_VIEW_SHOW_TIME, true);
    }

    public static void setShowTime(final boolean value) {
        setBoolean(K_VIEW_SHOW_TIME, value);
    }

    public static boolean isShowStatistics() {
        return getBoolean(K_VIEW_SHOW_STATS, true);
    }

    public static void setShowStatistics(final boolean value) {
        setBoolean(K_VIEW_SHOW_STATS, value);
    }

    public static boolean isTruckAIPaused() {
        return getBoolean(K_AI_TRUCK_PAUSED, false);
    }

    public static void setTruckAIPaused(final boolean value) {
        setBoolean(K_AI_TRUCK_PAUSED, value);
    }

    public static boolean isCarAIPaused() {
        return getBoolean(K_AI_CAR_PAUSED, false);
    }

    public static void setCarAIPaused(final boolean value) {
        setBoolean(K_AI_CAR_PAUSED, value);
    }

    public static int getTruckAIPriority() {
        return getInt(K_AI_TRUCK_PRIORITY, Thread.NORM_PRIORITY);
    }

    public static void setTruckAIPriority(final int value) {
        setInt(K_AI_TRUCK_PRIORITY, value);
    }

    public static int getCarAIPriority() {
        return getInt(K_AI_CAR_PRIORITY, Thread.NORM_PRIORITY);
    }

    public static void setCarAIPriority(final int value) {
        setInt(K_AI_CAR_PRIORITY, value);
    }

    // ========== Server ==========
    public static String getServerHost() {
        return properties.getProperty(K_SERVER_HOST,
            Configuration.SERVER_HOST
        );
    }

    public static void setServerHost(final String value) {
        properties.setProperty(K_SERVER_HOST, value);
    }

    public static int getServerPort() {
        return getInt(K_SERVER_PORT, Configuration.SERVER_PORT);
    }

    public static void setServerPort(final int value) {
        setInt(K_SERVER_PORT, value);
    }
}