package org.penakelex.objectsimulationsystem.model.collection;

import org.penakelex.objectsimulationsystem.model.vehicle.Vehicle;

import java.util.*;

public final class VehicleCollection {
    private static volatile VehicleCollection instance;
    private final List<Vehicle> vehicles;
    private final Set<Integer> vehiclesIDs;
    private final Map<Integer, Long> spawnTimes;
    private final transient Object lock = new Object();

    private int idCounter = 0;

    private VehicleCollection() {
        this.vehicles =
            Collections.synchronizedList(new ArrayList<>(500));
        this.vehiclesIDs =
            Collections.synchronizedSet(new TreeSet<>());
        this.spawnTimes =
            Collections.synchronizedMap(new HashMap<>());
    }

    public static VehicleCollection getInstance() {
        var result = instance;

        if (result == null) {
            synchronized (VehicleCollection.class) {
                result = instance;

                if (result == null) {
                    instance = result = new VehicleCollection();
                }
            }
        }

        return result;
    }

    public void add(final Vehicle vehicle) {
        synchronized (lock) {
            vehicles.add(vehicle);
            vehiclesIDs.add(vehicle.getId());
            spawnTimes.put(vehicle.getId(), vehicle.getSpawnTime());
        }
    }

    public void removeById(final int id) {
        synchronized (lock) {
            vehicles.removeIf(v -> v.getId() == id);
            vehiclesIDs.remove(id);
            spawnTimes.remove(id);
        }
    }

    public void removeByIds(final Collection<Integer> ids) {
        synchronized (lock) {
            vehicles.removeIf(vehicle ->
                ids.contains(vehicle.getId())
            );
            vehiclesIDs.removeAll(ids);
            ids.forEach(spawnTimes::remove);
        }
    }

    public List<Vehicle> getAll() {
        synchronized (lock) {
            return new ArrayList<>(vehicles);
        }
    }

    public int size() {
        synchronized (lock) {
            return vehicles.size();
        }
    }

    public void clear() {
        synchronized (lock) {
            vehicles.clear();
            vehiclesIDs.clear();
            spawnTimes.clear();
            idCounter = 0;
        }
    }

    public synchronized int getNextId() {
        return idCounter++;
    }

    public boolean hasId(final int id) {
        synchronized (lock) {
            return vehiclesIDs.contains(id);
        }
    }

    public Map<Integer, Long> getSpawnTimesSnapshot() {
        synchronized (lock) {
            return new HashMap<>(spawnTimes);
        }
    }

    public Set<Integer> getVehicleIdsSnapshot() {
        synchronized (lock) {
            return new TreeSet<>(vehiclesIDs);
        }
    }

    public boolean isEmpty() {
        synchronized (lock) {
            return vehicles.isEmpty();
        }
    }
}