package org.penakelex.objectsimulationsystem.controller;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;
import org.penakelex.objectsimulationsystem.habitat.Configuration;
import org.penakelex.objectsimulationsystem.habitat.Habitat;
import org.penakelex.objectsimulationsystem.habitat.TimeUnit;
import org.penakelex.objectsimulationsystem.ui.*;
import org.penakelex.objectsimulationsystem.vehicle.images.CarImages;
import org.penakelex.objectsimulationsystem.vehicle.images.TruckImages;

import java.net.URL;
import java.util.Arrays;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.IntStream;

public class SimulationController implements Initializable {
    @FXML private MenuItem menuStart, menuRestart, menuStop,
        menuPause;
    @FXML private CheckMenuItem menuToggleTime;
    @FXML private FontIcon menuTimeIcon;

    @FXML private Canvas simulationCanvas;
    @FXML private StackPane simulationField;

    @FXML private ToolbarButton startButton, stopButton, pauseButton,
        restartButton;

    @FXML private StackPane statusTimeContainer;
    @FXML private LabeledValueRow statusTimeRow;

    @FXML private LabeledValueRow truckRow, carRow, totalRow;

    @FXML private Label statusLabel;
    @FXML private FontIcon statusIcon;

    @FXML private VBox infoContainer;
    @FXML private VBox statusContainer;

    @FXML private RadioButton showTimeRadio, hideTimeRadio;

    @FXML final ToggleGroup timeToggleGroup = new ToggleGroup();

    @FXML private CheckBox showStatisticsCheckBox;

    @FXML private LabeledInputRow truckPeriodInput, carPeriodInput;
    @FXML private LabeledProbabilityBox truckProbabilityBox,
        carProbabilityBox;

    private Stage stage;

    private AnimationTimer gameTimer;
    private Habitat habitat;

    private SimulationStateModel stateModel;

    private ResourceBundle resources;

    @Override
    public void initialize(
        final URL _location,
        final ResourceBundle resources
    ) {
        this.resources = resources;
        stateModel = new SimulationStateModel();

        SimulationView
            .bindCanvasSize(simulationCanvas, simulationField);

        stateModel.onStateChanged(state -> {
            updateToolbarButtons(state);
            SimulationView.updateMenuItems(state,
                menuStart, menuRestart, menuStop, menuPause
            );
            SimulationView.updateStatus(state,
                resources,
                statusLabel,
                statusIcon
            );
        });

        gameTimer = new AnimationTimer() {
            @Override
            public void handle(final long now) {
                if (stateModel.getState() ==
                    SimulationState.Running
                ) {
                    stateModel.updateTime();
                    habitat.update(stateModel.getElapsedTime());
                    SimulationView.draw(simulationCanvas, habitat);

                    if (habitat.isStatisticsDirty()) {
                        SimulationView.updatePanelStatistics(
                            habitat.getStatistics(),
                            truckRow, carRow, totalRow
                        );
                    }

                    SimulationView.updateStatusTime(
                        stateModel.getElapsedTime(),
                        stateModel.isShowTime(),
                        resources,
                        statusTimeRow
                    );
                }
            }
        };

        initializeMenuAccelerators();

        updateToolbarButtons(stateModel.getState());
        SimulationView.updateStatus(stateModel.getState(),
            resources,
            statusLabel,
            statusIcon
        );

        setupTimeToggleGroup();

        SimulationView.updateStatusContainerHeight(
            stateModel.isShowTime(),
            statusContainer
        );

        initializeInputFields();

        simulationField
            .layoutBoundsProperty()
            .addListener((_, _, newValue) -> {
                habitat.setSize(newValue.getWidth(),
                    newValue.getHeight()
                );
                SimulationView.draw(simulationCanvas, habitat);
            });

        showStatisticsCheckBox
            .selectedProperty()
            .addListener((_, _, newValue) ->
                showStatisticsCheckBox.setSelected(newValue)
            );
    }

    public void setStage(final Stage stage) {
        this.stage = stage;
    }

    public void setKeyboardHandler(final Scene scene) {
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                simulationCanvas.requestFocus();
            }
        });

        simulationCanvas.setFocusTraversable(true);
        simulationCanvas.requestFocus();
    }

    private void initializeInputFields() {
        final var periodInputsTimeUnits = Arrays
            .stream(TimeUnit.values())
            .map(timeUnit -> resources
                .getString(timeUnit.messageKey)
            )
            .toList();

        truckPeriodInput
            .setTextFieldValue(Configuration.TRUCK_SPAWN_PERIOD);
        truckPeriodInput.initializeComboBoxValues(
            periodInputsTimeUnits,
            resources.getString(
                Configuration.TRUCK_SPAWN_TIME_UNIT.messageKey
            )
        );
        carPeriodInput
            .setTextFieldValue(Configuration.CAR_SPAWN_PERIOD);
        carPeriodInput.initializeComboBoxValues(
            periodInputsTimeUnits,
            resources.getString(
                Configuration.CAR_SPAWN_TIME_UNIT.messageKey
            )
        );

        final var probabilities = IntStream.rangeClosed(0, 10)
            .mapToObj(i -> resources
                .getString("format.probability.percent")
                .formatted(i * 10))
            .toList();

        truckProbabilityBox.initializeProbabilities(
            probabilities,
            (int) (Configuration.TRUCK_SPAWN_PROBABILITY * 10.)
        );
        carProbabilityBox.initializeProbabilities(
            probabilities,
            (int) (Configuration.CAR_SPAWN_PROBABILITY * 10.)
        );

        truckPeriodInput
            .textProperty()
            .addListener((_, _, newValue) -> {
                final var period = validatePeriod(newValue);

                if (period.isPresent()) {
                    habitat.updateTruckPeriod(period.get());
                    truckPeriodInput.setError(false);
                } else {
                    truckPeriodInput.setError(true);
                }
            });
        truckPeriodInput
            .comboBoxValueProperty()
            .addListener((_, _, newValue) -> findTimeUnitMatch(
                newValue).ifPresent(timeUnit ->
                habitat.updateTruckPeriodTimeUnit(timeUnit)
            ));
        truckPeriodInput
            .textFieldFocusedProperty()
            .addListener((_, _, focused) ->
                onInputRowFocusChange(
                    focused,
                    truckPeriodInput,
                    Configuration.TRUCK_SPAWN_PERIOD,
                    Configuration.TRUCK_SPAWN_TIME_UNIT
                )
            );

        carPeriodInput
            .textProperty()
            .addListener((_, _, newValue) -> {
                final var period = validatePeriod(newValue);

                if (period.isPresent()) {
                    habitat.updateCarPeriod(period.get());
                    carPeriodInput.setError(false);
                } else {
                    carPeriodInput.setError(true);
                }
            });
        carPeriodInput
            .comboBoxValueProperty()
            .addListener((_, _, newValue) -> findTimeUnitMatch(
                newValue).ifPresent(timeUnit ->
                habitat.updateCarPeriodTimeUnit(timeUnit)
            ));
        carPeriodInput
            .textFieldFocusedProperty()
            .addListener((_, _, focused) ->
                onInputRowFocusChange(
                    focused,
                    carPeriodInput,
                    Configuration.CAR_SPAWN_PERIOD,
                    Configuration.CAR_SPAWN_TIME_UNIT
                )
            );

        truckProbabilityBox
            .selectedIndexProperty()
            .addListener((_, _, newIndex) -> habitat
                .updateTruckProbability(newIndex.intValue() / 10.)
            );
        carProbabilityBox
            .selectedIndexProperty()
            .addListener((_, _, newIndex) -> habitat
                .updateCarProbability(newIndex.intValue() / 10.)
            );
    }

    private Optional<Integer> validatePeriod(
        final String newPeriod
    ) {
        try {
            final var period = Integer.parseUnsignedInt(newPeriod);

            if (period > 0) {
                return Optional.of(period);
            }
        } catch (final NumberFormatException _) {
        }

        return Optional.empty();
    }

    private void onInputRowFocusChange(
        final boolean focused,
        final LabeledInputRow input,
        final int defaultPeriod,
        final TimeUnit defaultPeriodTimeUnit
    ) {
        if (focused) {
            return;
        }

        final var fieldText = input.getFieldText();

        if (validatePeriod(fieldText).isPresent()) {
            return;
        }

        pauseSimulation();

        final var wrongTimeUnit = input.getComboBoxValue();
        final var defaultPeriodTimeUnitLabel =
            resources.getString(defaultPeriodTimeUnit.messageKey);

        input.setTextFieldValue(defaultPeriod);
        input.setComboBoxValue(defaultPeriodTimeUnitLabel);
        input.setError(false);

        showErrorDialog(resources
            .getString("error.invalid.period")
            .formatted(
                fieldText,
                wrongTimeUnit,
                defaultPeriod,
                defaultPeriodTimeUnitLabel
            )
        );
    }

    private Optional<TimeUnit> findTimeUnitMatch(
        final String stringTimeUnit
    ) {
        return Arrays
            .stream(TimeUnit.values())
            .filter(timeUnit -> resources
                .getString(timeUnit.messageKey)
                .equals(stringTimeUnit)
            )
            .findFirst();
    }

    private void initializeMenuAccelerators() {
        menuStart.setAccelerator(KeyCodeCombination.valueOf(
            resources.getString("label.controls.keybind.start")
        ));
        menuPause.setAccelerator(KeyCodeCombination.valueOf(
            resources.getString("label.controls.keybind.pause")
        ));
        menuStop.setAccelerator(KeyCodeCombination.valueOf(
            resources.getString("label.controls.keybind.stop")
        ));
        menuRestart.setAccelerator(KeyCodeCombination.valueOf(
            resources.getString("label.controls.keybind.restart")
        ));
        menuToggleTime.setAccelerator(KeyCodeCombination.valueOf(
            resources.getString("label.controls.keybind.time")
        ));
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

                SimulationView.setStatusTimeVisible(
                    stateModel.isShowTime(),
                    statusTimeContainer,
                    statusContainer
                );
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
        SimulationView.setNodeVisible(infoContainer, true);
        SimulationView.setStatusTimeVisible(stateModel.isShowTime(),
            statusTimeContainer,
            statusContainer
        );
        simulationCanvas.requestFocus();
        gameTimer.start();
    }

    @FXML
    private void stopSimulation() {
        if (stateModel.getState() == SimulationState.Stopped) {
            return;
        }

        final var statistics = habitat.getStatistics();
        final var elapsedTime = stateModel.getElapsedTime();

        stateModel.setState(SimulationState.Stopped);
        gameTimer.stop();

        SimulationView.setNodeVisible(infoContainer, false);

        if (showStatisticsCheckBox.isSelected()) {
            SimulationView.setStatusTimeVisible(false,
                statusTimeContainer,
                statusContainer
            );

            final var shouldStop =
                StatisticsDialog.showStatisticsDialog(
                    stage,
                    statistics,
                    elapsedTime,
                    resources
                );

            SimulationView.setStatusTimeVisible(
                stateModel.isShowTime(),
                statusTimeContainer,
                statusContainer
            );

            if (!shouldStop) {
                stateModel.setState(SimulationState.Running);
                stateModel.startTimer();
                gameTimer.start();
                SimulationView.setNodeVisible(infoContainer, true);
                return;
            }
        }

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
        SimulationView.draw(simulationCanvas, habitat);
        simulationCanvas.requestFocus();
    }

    @FXML
    private void toggleTimeDisplay() {
        stateModel.toggleShowTime();
        SimulationView.setStatusTimeVisible(stateModel.isShowTime(),
            statusTimeContainer,
            statusContainer
        );
        updateTimeRadio();
        SimulationView.updateMenuTimeText(stateModel.isShowTime(),
            resources,
            menuToggleTime,
            menuTimeIcon
        );
        simulationCanvas.requestFocus();
    }

    @FXML
    private void exitApplication() {
        stateModel.setState(SimulationState.Stopped);
        gameTimer.stop();
        Platform.exit();
    }

    @FXML
    private void showAboutDialog() {
        final var alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(resources.getString("menu.help.about"));
        alert.setHeaderText(resources.getString("application.title"));

        final var descriptionLabel =
            new Label(resources.getString("label.about"));
        descriptionLabel.getStyleClass().add("about-dialog-info");

        alert.getDialogPane().setContent(descriptionLabel);
        alert.getDialogPane().getStyleClass().add("dialog-pane");

        alert.showAndWait();
    }

    private void resetStatistics() {
        stateModel.resetTimer();
        habitat.reset();
        updatePanelStatisticsWithTime();
    }

    private void updatePanelStatisticsWithTime() {
        SimulationView.updatePanelStatistics(habitat.getStatistics(),
            truckRow, carRow, totalRow
        );
        SimulationView.updateStatusTime(
            stateModel.getElapsedTime(),
            stateModel.isShowTime(),
            resources,
            statusTimeRow
        );
    }

    private void updateToolbarButtons(final SimulationState state) {
        switch (state) {
            case Stopped -> {
                SimulationView.setNodeVisible(startButton, true);
                SimulationView.setNodesVisible(false,
                    pauseButton,
                    stopButton,
                    restartButton
                );
            }
            case Running -> {
                SimulationView.setNodeVisible(startButton, false);
                SimulationView.setNodesVisible(true,
                    pauseButton,
                    stopButton,
                    restartButton
                );
            }
            case Paused -> {
                SimulationView.setNodeVisible(pauseButton, false);
                SimulationView.setNodesVisible(true,
                    startButton,
                    stopButton,
                    restartButton
                );
            }
        }
    }

    private void showErrorDialog(final String message) {
        final var alert = new Alert(Alert.AlertType.WARNING);
        alert.initOwner(stage);
        alert.setTitle(resources.getString("error.title"));
        alert.setHeaderText(resources.getString("error.header"));
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void updateTimeRadio() {
        final var radioButton =
            stateModel.isShowTime() ? showTimeRadio : hideTimeRadio;
        radioButton.setSelected(true);
    }
}