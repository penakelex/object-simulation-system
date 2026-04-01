package org.penakelex.objectsimulationsystem.view;

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
import org.penakelex.objectsimulationsystem.model.habitat.Configuration;
import org.penakelex.objectsimulationsystem.model.habitat.TimeUnit;
import org.penakelex.objectsimulationsystem.ui.*;
import org.penakelex.objectsimulationsystem.ui.components.LabeledInputRow;
import org.penakelex.objectsimulationsystem.ui.components.LabeledProbabilityBox;
import org.penakelex.objectsimulationsystem.ui.components.LabeledValueRow;
import org.penakelex.objectsimulationsystem.ui.components.ToolbarButton;
import org.penakelex.objectsimulationsystem.ui.helpers.SimulationViewHelper;
import org.penakelex.objectsimulationsystem.model.vehicle.images.CarImages;
import org.penakelex.objectsimulationsystem.model.vehicle.images.TruckImages;
import org.penakelex.objectsimulationsystem.viewmodel.SimulationState;
import org.penakelex.objectsimulationsystem.viewmodel.SimulationViewModel;

import java.net.URL;
import java.util.Arrays;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.IntStream;

public final class SimulationViewController implements Initializable {
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

    private SimulationViewModel viewModel;
    private AnimationTimer gameTimer;
    private Stage stage;
    private ResourceBundle resources;

    @Override
    public void initialize(
        final URL _location,
        final ResourceBundle resources
    ) {
        this.resources = resources;
        this.viewModel = new SimulationViewModel();

        bindUI();

        gameTimer = new AnimationTimer() {
            @Override
            public void handle(final long now) {
                if (viewModel.getState() == SimulationState.Running) {
                    viewModel.updateTime();
                    viewModel.updateHabitat();
                    SimulationViewHelper.draw(
                        simulationCanvas,
                        viewModel.getHabitat()
                    );
                    SimulationViewHelper.updateStatusTime(
                        viewModel.getElapsedTime(),
                        viewModel.isShowTime(),
                        resources,
                        statusTimeRow
                    );
                }
            }
        };

        initializeMenuAccelerators();
        setupTimeToggleGroup();
        initializeInputFields();
        setupLayoutListener();

        updateUIState(viewModel.getState());
    }

    private void bindUI() {
        viewModel.truckCountProperty().addListener((_, _, newValue) ->
            truckRow.setValue(newValue.intValue())
        );
        viewModel.carCountProperty().addListener((_, _, newValue) ->
            carRow.setValue(newValue.intValue())
        );
        viewModel.totalCountProperty().addListener((_, _, newValue) ->
            totalRow.setValue(newValue.intValue())
        );

        viewModel.onStateChanged(this::updateUIState);

        SimulationViewHelper.bindCanvasSize(
            simulationCanvas,
            simulationField
        );
    }

    @FXML
    private void startSimulation() {
        viewModel.startSimulation();
        gameTimer.start();
        simulationCanvas.requestFocus();
        SimulationViewHelper.setNodeVisible(infoContainer, true);
        SimulationViewHelper.setStatusTimeVisible(
            viewModel.isShowTime(),
            statusTimeContainer,
            statusContainer
        );
    }

    @FXML
    private void stopSimulation() {
        viewModel.stopSimulation();
        gameTimer.stop();
        SimulationViewHelper.setNodeVisible(infoContainer, false);

        if (showStatisticsCheckBox.isSelected()) {
            SimulationViewHelper.setStatusTimeVisible(false,
                statusTimeContainer,
                statusContainer
            );

            final var shouldStop = StatisticsDialog
                .showStatisticsDialog(
                    stage,
                    viewModel.getHabitat().getStatistics(),
                    viewModel.getElapsedTime(),
                    resources
                );

            SimulationViewHelper.setStatusTimeVisible(
                viewModel.isShowTime(),
                statusTimeContainer,
                statusContainer
            );

            if (!shouldStop) {
                viewModel.startSimulation();
                gameTimer.start();
                SimulationViewHelper
                    .setNodeVisible(infoContainer, true);
                return;
            }
        }

        viewModel.resetStatistics();
        updatePanelStatisticsWithTime();
        simulationCanvas.requestFocus();
    }

    @FXML
    private void pauseSimulation() {
        viewModel.pauseSimulation();
        simulationCanvas.requestFocus();
    }

    @FXML
    private void restartSimulation() {
        viewModel.restartSimulation();
        SimulationViewHelper.draw(
            simulationCanvas,
            viewModel.getHabitat()
        );
        updatePanelStatisticsWithTime();
        simulationCanvas.requestFocus();
    }

    @FXML
    private void toggleTimeDisplay() {
        viewModel.toggleShowTime();
        SimulationViewHelper.setStatusTimeVisible(
            viewModel.isShowTime(),
            statusTimeContainer,
            statusContainer
        );
        updateTimeRadio();
        SimulationViewHelper.updateMenuTimeText(
            viewModel.isShowTime(),
            resources,
            menuToggleTime,
            menuTimeIcon
        );
        simulationCanvas.requestFocus();
    }

    @FXML
    private void exitApplication() {
        viewModel.stopSimulation();
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

    private void updateUIState(final SimulationState state) {
        SimulationViewHelper.updateMenuItems(state,
            menuStart,
            menuRestart,
            menuStop,
            menuPause
        );
        SimulationViewHelper.updateStatus(state,
            resources,
            statusLabel,
            statusIcon
        );
        updateToolbarButtons(state);
    }

    private void updateToolbarButtons(final SimulationState state) {
        switch (state) {
            case Stopped -> {
                SimulationViewHelper.setNodeVisible(startButton,
                    true
                );
                SimulationViewHelper.setNodesVisible(false,
                    pauseButton,
                    stopButton,
                    restartButton
                );
            }
            case Running -> {
                SimulationViewHelper.setNodeVisible(startButton,
                    false
                );
                SimulationViewHelper.setNodesVisible(true,
                    pauseButton,
                    stopButton,
                    restartButton
                );
            }
            case Paused -> {
                SimulationViewHelper.setNodeVisible(pauseButton,
                    false
                );
                SimulationViewHelper.setNodesVisible(true,
                    startButton,
                    stopButton,
                    restartButton
                );
            }
        }
    }

    private void updateTimeRadio() {
        final var radioButton =
            viewModel.isShowTime() ? showTimeRadio : hideTimeRadio;
        radioButton.setSelected(true);
    }

    private void updatePanelStatisticsWithTime() {
        SimulationViewHelper.updatePanelStatistics(
            viewModel.getHabitat().getStatistics(),
            truckRow,
            carRow,
            totalRow
        );
        SimulationViewHelper.updateStatusTime(
            viewModel.getElapsedTime(),
            viewModel.isShowTime(),
            resources,
            statusTimeRow
        );
    }

    private void setupTimeToggleGroup() {
        showTimeRadio.setToggleGroup(timeToggleGroup);
        hideTimeRadio.setToggleGroup(timeToggleGroup);

        timeToggleGroup
            .selectedToggleProperty()
            .addListener((_, _, newValue) -> {
                final var shouldBeVisible = newValue == showTimeRadio;

                if (shouldBeVisible != viewModel.isShowTime()) {
                    viewModel.toggleShowTime();
                }

                SimulationViewHelper.setStatusTimeVisible(
                    viewModel.isShowTime(),
                    statusTimeContainer,
                    statusContainer
                );
            });
    }

    private void initializeInputFields() {
        final var periodInputsTimeUnits = Arrays
            .stream(TimeUnit.values())
            .map(timeUnit ->
                resources.getString(timeUnit.messageKey)
            )
            .toList();

        truckPeriodInput.setTextFieldValue(
            Configuration.TRUCK_SPAWN_PERIOD
        );
        truckPeriodInput.initializeComboBoxValues(
            periodInputsTimeUnits,
            resources.getString(
                Configuration.TRUCK_SPAWN_TIME_UNIT.messageKey
            )
        );

        carPeriodInput.setTextFieldValue(
            Configuration.CAR_SPAWN_PERIOD
        );
        carPeriodInput.initializeComboBoxValues(
            periodInputsTimeUnits,
            resources.getString(
                Configuration.CAR_SPAWN_TIME_UNIT.messageKey
            )
        );

        final var probabilities = IntStream.rangeClosed(0, 10)
            .mapToObj(i -> resources
                .getString("format.probability.percent")
                .formatted(i * 10)
            )
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
                    viewModel.getHabitat()
                        .updateTruckPeriod(period.get());
                    truckPeriodInput.setError(false);
                } else {
                    truckPeriodInput.setError(true);
                }
            });

        truckPeriodInput
            .comboBoxValueProperty()
            .addListener((_, _, newValue) ->
                findTimeUnitMatch(newValue).ifPresent(timeUnit ->
                    viewModel.getHabitat()
                        .updateTruckPeriodTimeUnit(timeUnit)
                )
            );

        truckPeriodInput
            .textFieldFocusedProperty()
            .addListener((_, _, focused) ->
                onInputRowFocusChange(
                    focused, truckPeriodInput,
                    Configuration.TRUCK_SPAWN_PERIOD,
                    Configuration.TRUCK_SPAWN_TIME_UNIT
                )
            );

        carPeriodInput
            .textProperty()
            .addListener((_, _, newValue) -> {
                final var period = validatePeriod(newValue);

                if (period.isPresent()) {
                    viewModel.getHabitat()
                        .updateCarPeriod(period.get());
                    carPeriodInput.setError(false);
                } else {
                    carPeriodInput.setError(true);
                }
            });

        carPeriodInput
            .comboBoxValueProperty()
            .addListener((_, _, newValue) ->
                findTimeUnitMatch(newValue).ifPresent(timeUnit ->
                    viewModel.getHabitat()
                        .updateCarPeriodTimeUnit(timeUnit)
                )
            );

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
            .addListener((_, _, newIndex) ->
                viewModel.getHabitat()
                    .updateTruckProbability(newIndex.intValue() / 10.)
            );
        carProbabilityBox
            .selectedIndexProperty()
            .addListener((_, _, newIndex) ->
                viewModel.getHabitat()
                    .updateCarProbability(newIndex.intValue() / 10.)
            );
    }

    private Optional<Integer> validatePeriod(final String newPeriod) {
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

        viewModel.pauseSimulation();

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
        return Arrays.stream(TimeUnit.values())
            .filter(timeUnit -> resources
                .getString(timeUnit.messageKey)
                .equals(stringTimeUnit))
            .findFirst();
    }

    private void initializeMenuAccelerators() {
        menuStart.setAccelerator(KeyCodeCombination.valueOf(
            resources.getString("label.controls.keybind.start"))
        );
        menuPause.setAccelerator(KeyCodeCombination.valueOf(
            resources.getString("label.controls.keybind.pause"))
        );
        menuStop.setAccelerator(KeyCodeCombination.valueOf(
            resources.getString("label.controls.keybind.stop"))
        );
        menuRestart.setAccelerator(KeyCodeCombination.valueOf(
            resources.getString("label.controls.keybind.restart"))
        );
        menuToggleTime.setAccelerator(KeyCodeCombination.valueOf(
            resources.getString("label.controls.keybind.time"))
        );
    }

    private void setupLayoutListener() {
        simulationField
            .layoutBoundsProperty()
            .addListener((_, _, newValue) -> {
                if (viewModel.getHabitat() != null) {
                    viewModel.getHabitat().setSize(
                        newValue.getWidth(),
                        newValue.getHeight()
                    );
                    SimulationViewHelper.draw(
                        simulationCanvas, viewModel.getHabitat()
                    );
                }
            });
    }

    private void showErrorDialog(final String message) {
        final var alert = new Alert(Alert.AlertType.WARNING);
        alert.initOwner(stage);
        alert.setTitle(resources.getString("error.title"));
        alert.setHeaderText(resources.getString("error.header"));
        alert.setContentText(message);
        alert.showAndWait();
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

    public void initializeHabitatImages(
        final TruckImages truckImages,
        final CarImages carImages
    ) {
        viewModel.initializeHabitat(
            simulationCanvas.getWidth(),
            simulationCanvas.getHeight(),
            truckImages,
            carImages
        );
        updatePanelStatisticsWithTime();
    }
}