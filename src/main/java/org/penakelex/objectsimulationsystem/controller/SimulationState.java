package org.penakelex.objectsimulationsystem.controller;

public enum SimulationState {
    Stopped(
        "stopped",
        "fas-stop-circle",
        "label.status.stopped"
    ),
    Running(
        "running",
        "fas-play-circle",
        "label.status.running"
    ),
    Paused(
        "paused",
        "fas-pause-circle",
        "label.status.paused"
    );

    public final String styleClass;
    public final String iconLiteral;
    public final String messageKey;

    SimulationState(
        final String styleClass,
        final String iconLiteral,
        final String messageKey
    ) {
        this.styleClass = styleClass;
        this.iconLiteral = iconLiteral;
        this.messageKey = messageKey;
    }
}
