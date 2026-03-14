package org.penakelex.objectsimulationsystem.vehicle.exceptions;

import org.penakelex.objectsimulationsystem.validation.ValidationException;

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
