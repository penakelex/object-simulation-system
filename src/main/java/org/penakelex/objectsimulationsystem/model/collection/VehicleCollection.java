package org.penakelex.objectsimulationsystem.model.collection;

import org.penakelex.objectsimulationsystem.model.vehicle.Vehicle;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public final class VehicleCollection {
    private static final VehicleCollection instance =
        new VehicleCollection();

    private final List<Vehicle> vehicles;
    private final Set<Integer> vehiclesIDs;
    private final Map<Integer, Long> spawnTimes;

    private final AtomicInteger idCounter;

    private VehicleCollection() {
        this.vehicles = new CopyOnWriteArrayList<>();
        this.vehiclesIDs = ConcurrentHashMap.newKeySet();
        this.spawnTimes = new ConcurrentHashMap<>();

        this.idCounter = new AtomicInteger(0);
    }

    public static VehicleCollection getInstance() {
        return instance;
    }

    public void add(final Vehicle vehicle) {
        vehicles.add(vehicle);
        vehiclesIDs.add(vehicle.getId());
        spawnTimes.put(vehicle.getId(), vehicle.getSpawnTime());
    }

    public void removeByIds(final Collection<Integer> ids) {
        vehicles.removeIf(vehicle ->
            ids.contains(vehicle.getId())
        );
        vehiclesIDs.removeAll(ids);
        ids.forEach(spawnTimes::remove);
    }

    public List<Vehicle> getAll() {
        return vehicles;
    }

    public int size() {
        return vehicles.size();
    }

    public void clear() {
        vehicles.clear();
        vehiclesIDs.clear();
        spawnTimes.clear();
        idCounter.set(0);
    }

    public int getNextId() {
        return idCounter.getAndIncrement();
    }

    public Map<Integer, Long> getSpawnTimesSnapshot() {
        return Map.copyOf(spawnTimes);
    }
}