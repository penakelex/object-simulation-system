package org.penakelex.objectsimulationsystem.model.habitat;

public record VehicleStatistics(
    int trucks,
    int cars,
    int total,
    int currentTrucks,
    int currentCars,
    int currentTotal
)
{
    public VehicleStatistics {
        if (trucks < 0 || cars < 0 || total < 0 ||
            currentTrucks < 0 || currentCars < 0 ||
            currentTotal < 0) {
            throw new IllegalArgumentException(
                "Counts must be non-negative"
            );
        }
    }
}
