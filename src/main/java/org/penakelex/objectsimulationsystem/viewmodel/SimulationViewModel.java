package org.penakelex.objectsimulationsystem.viewmodel;

import javafx.application.Platform;
import javafx.beans.property.*;
import org.penakelex.objectsimulationsystem.model.habitat.Habitat;
import org.penakelex.objectsimulationsystem.model.vehicle.images.CarImages;
import org.penakelex.objectsimulationsystem.model.vehicle.images.TruckImages;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public final class SimulationViewModel {
    private final ObjectProperty<SimulationState> state =
        new SimpleObjectProperty<>(SimulationState.Stopped);

    private final LongProperty elapsedTime =
        new SimpleLongProperty(0);
    private final BooleanProperty showTime =
        new SimpleBooleanProperty(true);

    private final IntegerProperty truckCount =
        new SimpleIntegerProperty(0);
    private final IntegerProperty carCount =
        new SimpleIntegerProperty(0);
    private final IntegerProperty totalCount =
        new SimpleIntegerProperty(0);

    private Habitat habitat;
    private long startTime = 0;
    private Consumer<SimulationState> stateChangeListener;

    public SimulationViewModel() {
        state.addListener((_, _, newState) -> {
            if (stateChangeListener != null) {
                stateChangeListener.accept(newState);
            }
        });
    }

    private final ScheduledExecutorService simTick = Executors
        .newSingleThreadScheduledExecutor(runnable -> {
            final var thread = new Thread(runnable, "SimulationTick");
            thread.setDaemon(true);
            return thread;
        });

    private volatile boolean simulationRunning = false;

    {
        simTick.scheduleAtFixedRate(
            () -> {
                if (!simulationRunning || habitat == null) {
                    return;
                }

                final long elapsed =
                    System.currentTimeMillis() - startTime;
                habitat.update(elapsed);

                Platform.runLater(() -> {
                    elapsedTime.set(elapsed);
                    updateStatistics();
                });
            },
            0,
            100,
            TimeUnit.MILLISECONDS
        );
    }

    public IntegerProperty truckCountProperty() {
        return truckCount;
    }

    public IntegerProperty carCountProperty() {
        return carCount;
    }

    public IntegerProperty totalCountProperty() {
        return totalCount;
    }

    public SimulationState getState() {
        return state.get();
    }

    public long getElapsedTime() {
        return elapsedTime.get();
    }

    public boolean isShowTime() {
        return showTime.get();
    }

    public Habitat getHabitat() {
        return habitat;
    }

    public void startSimulation() {
        if (state.get() == SimulationState.Running) {
            return;
        }

        startTime = System.currentTimeMillis() - elapsedTime.get();
        simulationRunning = true;
        habitat.startAI();
        setState(SimulationState.Running);
    }

    public void stopSimulation() {
        if (state.get() == SimulationState.Stopped) {
            return;
        }

        simulationRunning = false;
        habitat.pauseAI();
        setState(SimulationState.Stopped);
    }

    public void pauseSimulation() {
        if (state.get() != SimulationState.Running) {
            return;
        }

        simulationRunning = false;
        habitat.pauseAI();
        setState(SimulationState.Paused);
    }

    public void restartSimulation() {
        if (state.get() == SimulationState.Stopped) {
            return;
        }

        resetStatistics();
    }

    public void toggleShowTime() {
        showTime.set(!showTime.get());
    }

    public void updateStatistics() {
        if (habitat != null && habitat.isStatisticsDirty()) {
            final var statistics = habitat.getStatistics();
            truckCount.set(statistics.trucks());
            carCount.set(statistics.cars());
            totalCount.set(statistics.total());
        }
    }

    public void initializeHabitat(
        final double width,
        final double height,
        final TruckImages truckImages,
        final CarImages carImages
    ) {
        habitat = new Habitat(width, height, truckImages, carImages);
        updateStatistics();
    }

    public void resetStatistics() {
        elapsedTime.set(0);
        truckCount.set(0);
        carCount.set(0);
        totalCount.set(0);
        startTime = System.currentTimeMillis();
        habitat.reset();
    }

    public void onStateChanged(
        final Consumer<SimulationState> listener
    ) {
        stateChangeListener = listener;
    }

    private void setState(final SimulationState newState) {
        if (state.get() != newState) {
            state.set(newState);
        }
    }

    public void pauseTruckAI() {
        habitat.pauseTruckAI();
    }

    public void resumeTruckAI() {
        habitat.resumeTruckAI();
    }

    public void pauseCarAI() {
        habitat.pauseCarAI();
    }

    public void resumeCarAI() {
        habitat.resumeCarAI();
    }

    public void setTruckAIPriority(final int priority) {
        habitat.setTruckAIPriority(priority);
    }

    public void setCarAIPriority(final int priority) {
        habitat.setCarAIPriority(priority);
    }
}