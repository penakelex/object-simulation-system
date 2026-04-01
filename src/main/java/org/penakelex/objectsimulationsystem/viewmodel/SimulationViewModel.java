package org.penakelex.objectsimulationsystem.viewmodel;

import javafx.beans.property.*;
import org.penakelex.objectsimulationsystem.model.habitat.Habitat;
import org.penakelex.objectsimulationsystem.model.vehicle.images.CarImages;
import org.penakelex.objectsimulationsystem.model.vehicle.images.TruckImages;

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

    public ObjectProperty<SimulationState> stateProperty() {
        return state;
    }

    public LongProperty elapsedTimeProperty() {
        return elapsedTime;
    }

    public BooleanProperty showTimeProperty() {
        return showTime;
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
        setState(SimulationState.Running);
    }

    public void stopSimulation() {
        if (state.get() == SimulationState.Stopped) {
            return;
        }

        setState(SimulationState.Stopped);
    }

    public void pauseSimulation() {
        if (state.get() != SimulationState.Running) {
            return;
        }

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

    public void updateTime() {
        if (state.get() == SimulationState.Running) {
            elapsedTime.set(System.currentTimeMillis() - startTime);
        }
    }

    public void updateHabitat() {
        if (habitat != null &&
            state.get() == SimulationState.Running
        ) {
            habitat.update(elapsedTime.get());
            updateStatistics();
        }
    }

    private void updateStatistics() {
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

        if (habitat != null) {
            habitat.reset();
        }
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
}