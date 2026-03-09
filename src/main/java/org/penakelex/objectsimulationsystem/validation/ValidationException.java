package org.penakelex.objectsimulationsystem.validation;

import java.util.List;
import java.util.stream.Collectors;

public class ValidationException extends RuntimeException {
    private ValidationException() {
        throw new AssertionError(
            "Validation exception is only for inheritance"
        );
    }

    protected ValidationException(
        final List<? extends InvalidParameter> invalidParameters
    ) {
        super(invalidParameters
            .stream()
            .map(invalidParameter -> String.format(
                "%s: %s",
                invalidParameter.getParameterName(),
                invalidParameter.getParameterValue()
            ))
            .collect(Collectors.joining(", "))
        );
    }
}