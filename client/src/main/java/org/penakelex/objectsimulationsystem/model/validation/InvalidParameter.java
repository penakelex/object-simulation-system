package org.penakelex.objectsimulationsystem.model.validation;

public interface InvalidParameter {
    default String getParameterName() {
        return getClass().getSimpleName();
    }

    Object getParameterValue();
}
