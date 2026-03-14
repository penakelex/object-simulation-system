package org.penakelex.objectsimulationsystem.habitat.exceptions;

import org.penakelex.objectsimulationsystem.validation.ValidationException;

import java.util.List;

public class HabitatCreationException extends ValidationException {
    public final List<HabitatInvalidParameter> invalidParameters;

    public HabitatCreationException(
        final List<HabitatInvalidParameter> invalidParameters
    ) {
        super(invalidParameters);
        this.invalidParameters = List.copyOf(invalidParameters);
    }
}
