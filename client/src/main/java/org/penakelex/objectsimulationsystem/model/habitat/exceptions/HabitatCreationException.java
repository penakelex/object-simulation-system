package org.penakelex.objectsimulationsystem.model.habitat.exceptions;

import org.penakelex.objectsimulationsystem.model.validation.ValidationException;

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
