package org.penakelex.objectsimulationsystem.viewmodel.exceptions;

import org.penakelex.objectsimulationsystem.model.validation.InvalidParameter;

public abstract sealed class SimulationLoadInvalidParameter
    implements InvalidParameter
{
    public static final class ElapsedTime
        extends SimulationLoadInvalidParameter
    {
        private final long elapsedTime;

        public ElapsedTime(final long elapsedTime) {
            this.elapsedTime = elapsedTime;
        }

        public long getElapsedTime() {
            return elapsedTime;
        }

        @Override
        public Object getParameterValue() {
            return elapsedTime;
        }
    }

    public static final class VehiclesList
        extends SimulationLoadInvalidParameter
    {
        @Override
        public Object getParameterValue() {
            return null;
        }
    }

    public static final class TotalTrucks
        extends SimulationLoadInvalidParameter
    {
        private final int totalTrucks;

        public TotalTrucks(final int totalTrucks) {
            this.totalTrucks = totalTrucks;
        }

        public int getTotalTrucks() {
            return totalTrucks;
        }

        @Override
        public Object getParameterValue() {
            return totalTrucks;
        }
    }

    public static final class TotalCars
        extends SimulationLoadInvalidParameter
    {
        private final int totalCars;

        public TotalCars(final int totalCars) {
            this.totalCars = totalCars;
        }

        public int getTotalCars() {
            return totalCars;
        }

        @Override
        public Object getParameterValue() {
            return totalCars;
        }
    }

    public static final class CurrentTrucks
        extends SimulationLoadInvalidParameter
    {
        private final int currentTrucks;

        public CurrentTrucks(final int currentTrucks) {
            this.currentTrucks = currentTrucks;
        }

        public int getCurrentTrucks() {
            return currentTrucks;
        }

        @Override
        public Object getParameterValue() {
            return currentTrucks;
        }
    }

    public static final class CurrentCars
        extends SimulationLoadInvalidParameter
    {
        private final int currentCars;

        public CurrentCars(final int currentCars) {
            this.currentCars = currentCars;
        }

        public int getCurrentCars() {
            return currentCars;
        }

        @Override
        public Object getParameterValue() {
            return currentCars;
        }
    }

    public static final class CurrentTrucksExceedsTotal
        extends SimulationLoadInvalidParameter
    {
        private final int current, total;

        public CurrentTrucksExceedsTotal(
            final int current,
            final int total
        ) {
            this.current = current;
            this.total = total;
        }

        public int getCurrent() {
            return current;
        }

        public int getTotal() {
            return total;
        }

        @Override
        public Object getParameterValue() {
            return String.format("%d > %d", current, total);
        }
    }

    public static final class CurrentCarsExceedsTotal
        extends SimulationLoadInvalidParameter
    {
        private final int current, total;

        public CurrentCarsExceedsTotal(
            final int current,
            final int total
        ) {
            this.current = current;
            this.total = total;
        }

        public int getCurrent() {
            return current;
        }

        public int getTotal() {
            return total;
        }

        @Override
        public Object getParameterValue() {
            return String.format("%d > %d", current, total);
        }
    }

    public static final class VehicleCountMismatch
        extends SimulationLoadInvalidParameter
    {
        private final int actual, expected;

        public VehicleCountMismatch(
            final int actual,
            final int expected
        ) {
            this.actual = actual;
            this.expected = expected;
        }

        public int getActual() {
            return actual;
        }

        public int getExpected() {
            return expected;
        }

        @Override
        public Object getParameterValue() {
            return String.format("actual=%d, expected=%d",
                actual,
                expected
            );
        }
    }
}