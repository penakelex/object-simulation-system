package org.penakelex.shared.network;

import java.io.Serializable;

public record VehicleState(
    double relativeX,
    double relativeY,
    double targetRelativeX,
    double targetRelativeY,
    long remainingLifetime,
    short imageResourceIndex,
    VehicleType type,
    boolean hasTarget,
    boolean arrived
) implements Serializable
{
    public VehicleState {
        if (relativeX < 0.0 || relativeX > 1.0 ||
            relativeY < 0.0 || relativeY > 1.0
        ) {
            throw new IllegalArgumentException(
                "Relative coordinates out of [0.0, 1.0]: x=%.4f, y=%.4f"
                    .formatted(relativeX, relativeY)
            );
        }

        if (hasTarget) {
            if (targetRelativeX < 0.0 || targetRelativeX > 1.0 ||
                targetRelativeY < 0.0 || targetRelativeY > 1.0
            ) {
                throw new IllegalArgumentException(
                    ("Target coordinates out of [0.0, 1.0] " +
                        "when hasTarget=true: x=%.4f, y=%.4f")
                        .formatted(targetRelativeX, targetRelativeY)
                );
            }
        }

        if (remainingLifetime <= 0) {
            throw new IllegalArgumentException(
                "remainingLifetime must be positive: %d"
                    .formatted(remainingLifetime)
            );
        }

        if (imageResourceIndex < 0) {
            throw new IllegalArgumentException(
                "imageResourceIndex must be non-negative: %d"
                    .formatted(imageResourceIndex)
            );
        }
    }
}