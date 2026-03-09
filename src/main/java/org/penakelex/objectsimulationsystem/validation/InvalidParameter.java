package org.penakelex.objectsimulationsystem.validation;

public interface InvalidParameter {
    default String getParameterName() {
        return getClass().getSimpleName();
    }

    Object getParameterValue();
}
