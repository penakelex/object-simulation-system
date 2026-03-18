package org.penakelex.objectsimulationsystem.habitat;

public enum TimeUnit {
    Millis("label.time.unit.millis", "ms", 1),
    Seconds("label.time.unit.seconds", "s", 1000),
    Minutes(
        "label.time.unit.minutes",
        "m",
        60 * Seconds.millisModifier
    );

    public final String messageKey;
    public final String literal;
    public final int millisModifier;

    TimeUnit(
        final String messageKey,
        final String literal,
        final int millisModifier
    ) {
        this.messageKey = messageKey;
        this.literal = literal;
        this.millisModifier = millisModifier;
    }
}
