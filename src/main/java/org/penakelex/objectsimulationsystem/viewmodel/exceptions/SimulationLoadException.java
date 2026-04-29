package org.penakelex.objectsimulationsystem.viewmodel.exceptions;

import org.penakelex.objectsimulationsystem.model.validation.ValidationException;

import java.util.List;

public final class SimulationLoadException
    extends ValidationException
{
    public final List<SimulationLoadInvalidParameter>
        invalidParameters;

    public SimulationLoadException(
        final List<SimulationLoadInvalidParameter> invalidParameters
    ) {
        super(invalidParameters);
        this.invalidParameters = List.copyOf(invalidParameters);
    }
}