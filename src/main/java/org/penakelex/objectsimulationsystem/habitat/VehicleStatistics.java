package org.penakelex.objectsimulationsystem.habitat;

public record VehicleStatistics(int trucks, int cars, int total) {
    public VehicleStatistics {
        if (trucks < 0 || cars < 0 || total < 0) {
            throw new IllegalArgumentException(
                "Counts must be non-negative"
            );
        }
    }
}
