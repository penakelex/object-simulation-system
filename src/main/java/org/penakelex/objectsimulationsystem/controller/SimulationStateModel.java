package org.penakelex.objectsimulationsystem.controller;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.function.Consumer;

public final class SimulationStateModel {

    private final ObjectProperty<SimulationState> state =
        new SimpleObjectProperty<>(SimulationState.Stopped);

    private long startTime = 0;
    private long elapsedTime = 0;
    private boolean showTime = true;

    private Consumer<SimulationState> stateChangeListener;

    public SimulationStateModel() {
        state.addListener((_, _, newState) ->
            stateChangeListener.accept(newState)
        );
    }

    public SimulationState getState() {
        return state.get();
    }

    public void setState(final SimulationState newState) {
        if (state.get() != newState) {
            state.set(newState);
        }
    }

    public void onStateChanged(final Consumer<SimulationState> listener) {
        stateChangeListener = listener;
    }

    public void startTimer() {
        startTime = System.currentTimeMillis() - elapsedTime;
    }

    public void updateTime() {
        elapsedTime = System.currentTimeMillis() - startTime;
    }

    public void resetTimer() {
        startTime = System.currentTimeMillis();
        elapsedTime = 0;
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    public boolean isShowTime() {
        return showTime;
    }

    public void toggleShowTime() {
        showTime = !showTime;
    }
}