package org.penakelex.objectsimulationsystem.viewmodel;

import org.penakelex.objectsimulationsystem.model.vehicle.Vehicle;
import org.penakelex.objectsimulationsystem.viewmodel.exceptions.SimulationLoadException;
import org.penakelex.objectsimulationsystem.viewmodel.exceptions.SimulationLoadInvalidParameter;

import java.util.ArrayList;
import java.util.List;

public record SimulationLoadResult(
    long elapsedTime,
    List<Vehicle> vehicles,
    int totalTrucks,
    int totalCars,
    int currentTrucks,
    int currentCars
)
{
    public SimulationLoadResult {
        final var invalidParameters = validateParameters(
            elapsedTime,
            vehicles,
            totalTrucks,
            totalCars,
            currentTrucks,
            currentCars
        );

        if (invalidParameters != null) {
            throw new SimulationLoadException(invalidParameters);
        }
    }

    private static List<SimulationLoadInvalidParameter>
    validateParameters(
        final long elapsedTime,
        final List<Vehicle> vehicles,
        final int totalTrucks,
        final int totalCars,
        final int currentTrucks,
        final int currentCars
    ) {
        List<SimulationLoadInvalidParameter> invalidParameters = null;

        if (elapsedTime < 0) {
            invalidParameters = new ArrayList<>(8);
            invalidParameters.add(
                new SimulationLoadInvalidParameter.ElapsedTime(
                    elapsedTime
                )
            );
        }

        if (vehicles == null) {
            if (invalidParameters == null) {
                invalidParameters = new ArrayList<>(7);
            }

            invalidParameters.add(
                new SimulationLoadInvalidParameter.VehiclesList()
            );
        }

        if (totalTrucks < 0) {
            if (invalidParameters == null) {
                invalidParameters = new ArrayList<>(6);
            }

            invalidParameters.add(
                new SimulationLoadInvalidParameter.TotalTrucks(
                    totalTrucks
                )
            );
        }

        if (totalCars < 0) {
            if (invalidParameters == null) {
                invalidParameters = new ArrayList<>(5);
            }

            invalidParameters.add(
                new SimulationLoadInvalidParameter.TotalCars(
                    totalCars
                )
            );
        }

        if (currentTrucks < 0) {
            if (invalidParameters == null) {
                invalidParameters = new ArrayList<>(4);
            }

            invalidParameters.add
                (new SimulationLoadInvalidParameter.CurrentTrucks(
                        currentTrucks
                    )
                );
        }

        if (currentCars < 0) {
            if (invalidParameters == null) {
                invalidParameters = new ArrayList<>(3);
            }

            invalidParameters.add(
                new SimulationLoadInvalidParameter.CurrentCars(
                    currentCars
                )
            );
        }

        if (currentTrucks > totalTrucks) {
            if (invalidParameters == null) {
                invalidParameters = new ArrayList<>(2);
            }

            invalidParameters.add(new SimulationLoadInvalidParameter
                    .CurrentTrucksExceedsTotal(
                    currentTrucks,
                    totalTrucks
                )
            );
        }

        if (currentCars > totalCars) {
            if (invalidParameters == null) {
                invalidParameters = new ArrayList<>(1);
            }

            invalidParameters.add(new SimulationLoadInvalidParameter
                    .CurrentCarsExceedsTotal(
                    currentCars,
                    totalCars
                )
            );
        }

        final var expectedSize = currentTrucks + currentCars;

        if (vehicles != null && vehicles.size() != expectedSize) {
            if (invalidParameters == null) {
                invalidParameters = new ArrayList<>(1);
            }

            invalidParameters.add(new SimulationLoadInvalidParameter
                    .VehicleCountMismatch(
                    vehicles.size(),
                    expectedSize
                )
            );
        }

        return invalidParameters;
    }
}