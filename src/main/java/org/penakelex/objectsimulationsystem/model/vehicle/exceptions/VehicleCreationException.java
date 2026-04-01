package org.penakelex.objectsimulationsystem.model.vehicle.exceptions;

import org.penakelex.objectsimulationsystem.model.validation.ValidationException;

import java.util.List;

public final class VehicleCreationException extends
    ValidationException
{
    public final List<VehicleInvalidParameter> invalidParameters;

    public VehicleCreationException(
        final List<VehicleInvalidParameter> invalidParameters
    ) {
        super(invalidParameters);
        this.invalidParameters = List.copyOf(invalidParameters);
    }
}
