package org.penakelex.objectsimulationsystem.model.vehicle.exceptions;

import org.penakelex.objectsimulationsystem.model.validation.InvalidParameter;

public abstract sealed class VehicleInvalidParameter implements
    InvalidParameter
{
    public static final class Id extends VehicleInvalidParameter {
        private final int id;

        public Id(final int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        @Override
        public Object getParameterValue() {
            return id;
        }
    }

    public static final class RelativeX
        extends VehicleInvalidParameter
    {
        private final double relativeX;

        public RelativeX(final double relativeX) {
            this.relativeX = relativeX;
        }

        public double getRelativeX() {
            return relativeX;
        }

        @Override
        public Object getParameterValue() {
            return relativeX;
        }
    }

    public static final class RelativeY
        extends VehicleInvalidParameter
    {
        private final double relativeY;

        public RelativeY(final double relativeY) {
            this.relativeY = relativeY;
        }

        public double getRelativeY() {
            return relativeY;
        }

        @Override
        public Object getParameterValue() {
            return relativeY;
        }
    }

    public static final class SpawnTime
        extends VehicleInvalidParameter
    {
        private final long spawnTime;

        public SpawnTime(final long spawnTime) {
            this.spawnTime = spawnTime;
        }

        public long getSpawnTime() {
            return spawnTime;
        }

        @Override
        public Object getParameterValue() {
            return spawnTime;
        }
    }

    public static final class LifeTime
        extends VehicleInvalidParameter
    {
        private final long lifeTime;

        public LifeTime(final long lifeTime) {
            this.lifeTime = lifeTime;
        }

        public long getLifeTime() {
            return lifeTime;
        }

        @Override
        public Object getParameterValue() {
            return lifeTime;
        }
    }

    public static final class Image extends VehicleInvalidParameter {
        @Override
        public Object getParameterValue() {
            return null;
        }
    }
}
