package org.penakelex.objectsimulationsystem.model.collection;

import org.penakelex.objectsimulationsystem.model.vehicle.Vehicle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class VehicleCollection {
    private static volatile VehicleCollection instance;
    private final List<Vehicle> vehicles;
    private int idCounter = 0;

    private VehicleCollection() {
        this.vehicles = Collections.synchronizedList(new ArrayList<>(500));
    }

    public static VehicleCollection getInstance() {
        VehicleCollection result = instance;

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
        vehicles.add(vehicle);
    }

    public List<Vehicle> getAll() {
        synchronized (vehicles) {
            return new ArrayList<>(vehicles);
        }
    }

    public int size() {
        synchronized (vehicles) {
            return vehicles.size();
        }
    }

    public void clear() {
        synchronized (vehicles) {
            vehicles.clear();
            idCounter = 0;
        }
    }

    public synchronized int getNextId() {
        return idCounter++;
    }

    public void remove(final int index) {
        synchronized (vehicles) {
            if (index >= 0 && index < vehicles.size()) {
                vehicles.remove(index);
            }
        }
    }

    public boolean isEmpty() {
        synchronized (vehicles) {
            return vehicles.isEmpty();
        }
    }
}