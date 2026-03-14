package org.penakelex.objectsimulationsystem.controller;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;
import org.penakelex.objectsimulationsystem.habitat.Configuration;
import org.penakelex.objectsimulationsystem.habitat.Habitat;
import org.penakelex.objectsimulationsystem.ui.LabeledValueRow;
import org.penakelex.objectsimulationsystem.vehicle.images.CarImages;
import org.penakelex.objectsimulationsystem.vehicle.images.TruckImages;

import java.net.URL;
import java.util.ResourceBundle;

public class SimulationController implements Initializable {
    @FXML private Canvas simulationCanvas;
    @FXML private StackPane simulationField;

    @FXML private LabeledValueRow truckPeriodRow, truckProbabilityRow,
        carPeriodRow, carProbabilityRow;

    @FXML private Label timeLabel, overlayTimeLabel;

    @FXML private LabeledValueRow truckRow, carRow, totalRow,
        overlayTruckRow, overlayCarRow, overlayTotalRow;

    @FXML private Label statusLabel;
    @FXML private FontIcon statusIcon;

    @FXML private VBox infoContainer, timeContainer,
        statisticsOverlay;

    private AnimationTimer gameTimer;

    private Habitat habitat;
    private GraphicsContext graphicsContext;

    private SimulationState state = SimulationState.Stopped;

    private boolean showTime = true;

    private long startTime = 0;
    private long elapsedTime = 0;

    private ResourceBundle resources;

    @Override
    public void initialize(
        final URL _location,
        final ResourceBundle resources
    ) {
        this.resources = resources;
        graphicsContext = simulationCanvas.getGraphicsContext2D();

        gameTimer = new AnimationTimer() {
            @Override
            public void handle(final long now) {
                if (state == SimulationState.Running) {
                    elapsedTime =
                        System.currentTimeMillis() - startTime;
                    habitat.update(elapsedTime);
                    draw();

                    if (habitat.isStatisticsDirty()) {
                        updatePanelStatistics();
                    }

                    updatePanelTime();
                }
            }
        };

        initializeGenerationParameters(
            truckPeriodRow,
            truckProbabilityRow,
            Configuration.TRUCK_SPAWN_PERIOD_MILLIS,
            Configuration.TRUCK_SPAWN_PROBABILITY
        );
        initializeGenerationParameters(
            carPeriodRow,
            carProbabilityRow,
            Configuration.CAR_SPAWN_PERIOD_MILLIS,
            Configuration.CAR_SPAWN_PROBABILITY
        );

        updateStatusLabel();

        simulationCanvas.widthProperty()
            .bind(simulationField.widthProperty());
        simulationCanvas.heightProperty()
            .bind(simulationField.heightProperty());

        simulationField
            .layoutBoundsProperty()
            .addListener((_, _, newValue) -> {
                habitat.setSize(
                    newValue.getWidth(),
                    newValue.getHeight()
                );
                draw();
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

    private void initializeGenerationParameters(
        final LabeledValueRow periodRow,
        final LabeledValueRow probabilityRow,
        final int period,
        final double probability
    ) {
        periodRow.setValue(resources
            .getString("format.period.milliseconds")
            .formatted(period)
        );
        probabilityRow.setValue(resources
            .getString("format.probability.percent")
            .formatted((int) (probability * 100))
        );
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

    private void startSimulation() {
        if (state == SimulationState.Running) {
            return;
        }

        startTime = System.currentTimeMillis() - elapsedTime;
        state = SimulationState.Running;

        infoContainer.setVisible(true);
        infoContainer.setManaged(true);

        statisticsOverlay.setVisible(false);
        statisticsOverlay.setManaged(false);

        updateStatusLabel();

        simulationCanvas.requestFocus();
        gameTimer.start();
    }

    private void stopSimulation() {
        if (state == SimulationState.Stopped) {
            return;
        }

        state = SimulationState.Stopped;
        gameTimer.stop();

        updateOverlayStatisticsWithTime();

        infoContainer.setVisible(false);
        infoContainer.setManaged(false);

        statisticsOverlay.setVisible(true);
        statisticsOverlay.setManaged(true);

        resetStatistics();
        updateStatusLabel();
    }

    private void pauseSimulation() {
        if (state != SimulationState.Running) {
            return;
        }

        state = SimulationState.Paused;
        updateStatusLabel();
    }

    private void restartSimulation() {
        if (state == SimulationState.Stopped) {
            return;
        }

        resetStatistics();
        draw();
    }

    private void resetStatistics() {
        startTime = System.currentTimeMillis();
        elapsedTime = 0;
        habitat.reset();
        updatePanelStatisticsWithTime();
    }

    private void toggleTimeDisplay() {
        showTime = !showTime;
        timeContainer.setVisible(showTime);
        timeContainer.setManaged(showTime);
    }

    private void draw() {
        graphicsContext.clearRect(
            0,
            0,
            simulationCanvas.getWidth(),
            simulationCanvas.getHeight()
        );

        habitat.draw(graphicsContext);
    }

    private void updatePanelStatisticsWithTime() {
        updatePanelStatistics();
        updatePanelTime();
    }

    private void updatePanelStatistics() {
        updateStatistics(truckRow, carRow, totalRow);
    }

    private void updatePanelTime() {
        updateTime(timeLabel);
    }

    private void updateOverlayStatisticsWithTime() {
        updateStatistics(
            overlayTruckRow,
            overlayCarRow,
            overlayTotalRow
        );
        updateTime(overlayTimeLabel);
    }

    private void updateStatistics(
        final LabeledValueRow truckRow,
        final LabeledValueRow carRow,
        final LabeledValueRow totalRow
    ) {
        final var statistics = habitat.getStatistics();

        truckRow.setValue(statistics.trucks());
        carRow.setValue(statistics.cars());
        totalRow.setValue(statistics.trucks() + statistics.cars());
    }

    private void updateTime(final Label timeLabel) {
        if (showTime) {
            timeLabel.setText(resources
                .getString("format.time.milliseconds")
                .formatted(elapsedTime)
            );
        }
    }

    private void updateStatusLabel() {
        statusLabel.setText(resources.getString(state.messageKey));
        statusLabel.getStyleClass()
            .setAll("status-value", state.styleClass);

        statusIcon.setIconLiteral(state.iconLiteral);
        statusIcon.getStyleClass()
            .setAll("status-icon", state.styleClass);
    }
}