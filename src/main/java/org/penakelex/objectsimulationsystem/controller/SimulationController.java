package org.penakelex.objectsimulationsystem.controller;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;
import org.penakelex.objectsimulationsystem.habitat.Configuration;
import org.penakelex.objectsimulationsystem.habitat.Habitat;
import org.penakelex.objectsimulationsystem.ui.LabeledValueRow;
import org.penakelex.objectsimulationsystem.ui.ToolbarButton;
import org.penakelex.objectsimulationsystem.vehicle.images.CarImages;
import org.penakelex.objectsimulationsystem.vehicle.images.TruckImages;

import java.net.URL;
import java.util.ResourceBundle;

public class SimulationController implements Initializable {
    @FXML private Canvas simulationCanvas;
    @FXML private StackPane simulationField;

    @FXML private ToolbarButton startButton, stopButton, pauseButton,
        restartButton;

    @FXML private LabeledValueRow truckPeriodRow, truckProbabilityRow,
        carPeriodRow, carProbabilityRow;

    @FXML private StackPane statusTimeContainer;
    @FXML private LabeledValueRow statusTimeRow;

    @FXML private Label overlayTimeLabel;

    @FXML private LabeledValueRow truckRow, carRow, totalRow,
        overlayTruckRow, overlayCarRow, overlayTotalRow;

    @FXML private Label statusLabel;
    @FXML private FontIcon statusIcon;

    @FXML private VBox infoContainer, statisticsOverlay;

    @FXML private RadioButton showTimeRadio, hideTimeRadio;

    @FXML private final ToggleGroup timeToggleGroup =
        new ToggleGroup();

    private AnimationTimer gameTimer;
    private Habitat habitat;

    private SimulationStateModel stateModel;
    private SimulationView view;

    private ResourceBundle resources;

    @Override
    public void initialize(
        final URL _location,
        final ResourceBundle resources
    ) {
        this.resources = resources;

        stateModel = new SimulationStateModel();
        view = new SimulationView(
            simulationCanvas, simulationField,
            truckRow, carRow, totalRow,
            overlayTruckRow, overlayCarRow, overlayTotalRow,
            statusTimeContainer,
            statusTimeRow, overlayTimeLabel,
            statusLabel, statusIcon,
            infoContainer, statisticsOverlay,
            resources
        );

        stateModel.onStateChanged(state -> {
            updateToolbarButtons(state);
            view.updateStatus(state);
        });

        gameTimer = new AnimationTimer() {
            @Override
            public void handle(final long now) {
                if (stateModel.getState() ==
                    SimulationState.Running
                ) {
                    stateModel.updateTime();
                    habitat.update(stateModel.getElapsedTime());
                    view.draw(habitat);

                    if (habitat.isStatisticsDirty()) {
                        view.updatePanelStatistics(
                            habitat.getStatistics()
                        );
                    }

                    view.updateStatusTime(
                        stateModel.getElapsedTime(),
                        stateModel.isShowTime()
                    );
                }
            }
        };

        initializeGenerationParameters();

        updateToolbarButtons(stateModel.getState());
        view.updateStatus(stateModel.getState());

        setupTimeToggleGroup();

        simulationField
            .layoutBoundsProperty()
            .addListener((_, _, newValue) -> {
                habitat.setSize(newValue.getWidth(),
                    newValue.getHeight()
                );
                view.draw(habitat);
            });
    }

    public void setupKeyboardHandler(final Scene scene) {
        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case B -> startSimulation();
                case E -> stopSimulation();
                case P -> pauseSimulation();
                case T -> toggleTimeDisplay();
                case R -> restartSimulation();
            }
        });

        simulationCanvas.setFocusTraversable(true);
        simulationCanvas.requestFocus();
    }

    private void initializeGenerationParameters() {
        truckPeriodRow.setValue(resources
            .getString("format.period.milliseconds")
            .formatted(Configuration.TRUCK_SPAWN_PERIOD_MILLIS));
        truckProbabilityRow.setValue(resources
            .getString("format.probability.percent")
            .formatted((int) (Configuration.TRUCK_SPAWN_PROBABILITY *
                100)));

        carPeriodRow.setValue(resources
            .getString("format.period.milliseconds")
            .formatted(Configuration.CAR_SPAWN_PERIOD_MILLIS));
        carProbabilityRow.setValue(resources
            .getString("format.probability.percent")
            .formatted((int) (Configuration.CAR_SPAWN_PROBABILITY *
                100)));
    }

    private void setupTimeToggleGroup() {
        showTimeRadio.setToggleGroup(timeToggleGroup);
        hideTimeRadio.setToggleGroup(timeToggleGroup);

        timeToggleGroup
            .selectedToggleProperty()
            .addListener((_, _, newValue) -> {
                final var shouldBeVisible = newValue == showTimeRadio;

                if (shouldBeVisible != stateModel.isShowTime()) {
                    stateModel.toggleShowTime();
                }

                view.setStatusTimeVisible(stateModel.isShowTime());
            });
    }

    public void initializeHabitatImages(
        final TruckImages truckImages,
        final CarImages carImages
    ) {
        habitat = new Habitat(
            simulationCanvas.getWidth(),
            simulationCanvas.getHeight(),
            truckImages,
            carImages
        );
        updatePanelStatisticsWithTime();
    }

    @FXML
    private void startSimulation() {
        if (stateModel.getState() == SimulationState.Running) {
            return;
        }

        stateModel.startTimer();
        stateModel.setState(SimulationState.Running);
        view.showInfoPanel();
        view.setStatusTimeVisible(stateModel.isShowTime());
        simulationCanvas.requestFocus();
        gameTimer.start();
    }

    @FXML
    private void stopSimulation() {
        if (stateModel.getState() == SimulationState.Stopped) {
            return;
        }

        stateModel.setState(SimulationState.Stopped);
        gameTimer.stop();

        view.updateOverlayStatistics(habitat.getStatistics());
        view.updateOverlayTime(stateModel.getElapsedTime());
        view.setStatusTimeVisible(false);
        view.showOverlay();

        resetStatistics();
        simulationCanvas.requestFocus();
    }

    @FXML
    private void pauseSimulation() {
        if (stateModel.getState() != SimulationState.Running) {
            return;
        }

        stateModel.setState(SimulationState.Paused);
        simulationCanvas.requestFocus();
    }

    @FXML
    private void restartSimulation() {
        if (stateModel.getState() == SimulationState.Stopped) {
            return;
        }

        resetStatistics();
        view.draw(habitat);
        simulationCanvas.requestFocus();
    }

    @FXML
    private void toggleTimeDisplay() {
        stateModel.toggleShowTime();
        view.setStatusTimeVisible(stateModel.isShowTime());
        updateTimeRadio();
        simulationCanvas.requestFocus();
    }

    private void resetStatistics() {
        stateModel.resetTimer();
        habitat.reset();
        updatePanelStatisticsWithTime();
    }

    private void updatePanelStatisticsWithTime() {
        view.updatePanelStatistics(habitat.getStatistics());
        view.updateStatusTime(
            stateModel.getElapsedTime(),
            stateModel.isShowTime()
        );
    }

    private void updateToolbarButtons(final SimulationState state) {
        switch (state) {
            case Stopped -> {
                view.setNodeVisible(startButton, true);
                view.setNodesVisible(false,
                    pauseButton,
                    stopButton,
                    restartButton
                );
            }
            case Running -> {
                view.setNodeVisible(startButton, false);
                view.setNodesVisible(true,
                    pauseButton,
                    stopButton,
                    restartButton
                );
            }
            case Paused -> {
                view.setNodeVisible(pauseButton, false);
                view.setNodesVisible(true,
                    startButton,
                    stopButton,
                    restartButton
                );
            }
        }
    }

    private void updateTimeRadio() {
        if (stateModel.isShowTime()) {
            showTimeRadio.setSelected(true);
        } else {
            hideTimeRadio.setSelected(true);
        }
    }
}