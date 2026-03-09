package org.penakelex.objectsimulationsystem.habitat.exceptions;

import org.penakelex.objectsimulationsystem.validation.InvalidParameter;

public abstract sealed class HabitatInvalidParameter implements
    InvalidParameter
{
    public static final class Width extends HabitatInvalidParameter {
        private final double width;

        public Width(final double width) {
            this.width = width;
        }

        public double getWidth() {
            return width;
        }

        @Override
        public Object getParameterValue() {
            return width;
        }
    }

    public static final class Height extends HabitatInvalidParameter {
        private final double height;

        public Height(final double height) {
            this.height = height;
        }

        public double getHeight() {
            return height;
        }

        @Override
        public Object getParameterValue() {
            return height;
        }
    }
}
