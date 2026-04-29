package org.penakelex.objectsimulationsystem.ui.helpers;

import org.penakelex.objectsimulationsystem.model.habitat.TimeUnit;

import java.util.ResourceBundle;

public final class TimeFormatter {
    private TimeFormatter() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static String formatTime(
        final long elapsedTimeMillis,
        final ResourceBundle resources
    ) {
        final var builder = new StringBuilder();

        final var minutes = elapsedTimeMillis / 60_000;
        final var seconds = (elapsedTimeMillis / 1000) % 60;
        final var millis = elapsedTimeMillis % 1000;

        if (minutes > 0) {
            builder.append(formatTimeComponent(
                minutes,
                TimeUnit.Minutes,
                resources
            ));
            builder.append(' ');
        }

        if (seconds > 0 || minutes > 0) {
            builder.append(formatTimeComponent(
                seconds,
                TimeUnit.Seconds,
                resources
            ));
            builder.append(' ');
        }

        builder.append(formatTimeComponent(
            millis,
            TimeUnit.Millis,
            resources
        ));

        return builder.toString();
    }

    private static String formatTimeComponent(
        final long value,
        final TimeUnit timeUnit,
        final ResourceBundle resources
    ) {
        return resources.getString("format.time").formatted(
            value,
            resources.getString(timeUnit.messageKey)
        );
    }
}