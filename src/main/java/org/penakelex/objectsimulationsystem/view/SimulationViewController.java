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
import org.penakelex.objectsimulationsystem.ui.*;
import org.penakelex.objectsimulationsystem.ui.components.*;
import org.penakelex.objectsimulationsystem.ui.helpers.ParameterPanelInitializer;
import org.penakelex.objectsimulationsystem.ui.helpers.SimulationViewHelper;
import org.penakelex.objectsimulationsystem.model.vehicle.images.CarImages;
import org.penakelex.objectsimulationsystem.model.vehicle.images.TruckImages;
import org.penakelex.objectsimulationsystem.viewmodel.SimulationState;
import org.penakelex.objectsimulationsystem.viewmodel.SimulationViewModel;

import java.net.URL;
import java.util.ResourceBundle;

public final class SimulationViewController implements Initializable {
    @FXML private MenuItem menuStart, menuRestart, menuStop,
        menuPause, menuCurrentObjects;
    @FXML private CheckMenuItem menuToggleTime;
    @FXML private FontIcon menuTimeIcon;
    @FXML private Canvas simulationCanvas;
    @FXML private StackPane simulationField;
    @FXML private ToolbarButton startButton, stopButton, pauseButton,
        restartButton;
    @FXML private StackPane statusTimeContainer;
    @FXML private LabeledValueRow statusTimeRow;
    @FXML private LabeledValueRow truckRow, carRow, totalRow,
        currentTruckRow, currentCarRow;
    @FXML private Label statusLabel;
    @FXML private FontIcon statusIcon;
    @FXML private VBox infoContainer, statusContainer;
    @FXML private RadioButton showTimeRadio, hideTimeRadio;
    @FXML final ToggleGroup timeToggleGroup = new ToggleGroup();
    @FXML private CheckBox showStatisticsCheckBox;
    @FXML private LabeledInputRow truckPeriodInput, carPeriodInput;
    @FXML private LabeledProbabilityBox truckProbabilityBox,
        carProbabilityBox;
    @FXML private LabeledUnitInputRow truckSpeedInput, carSpeedInput;
    @FXML private LabeledInputRow truckLifetimeInput,
        carLifetimeInput;
    @FXML private AIControlRow truckAIControl, carAIControl;

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
                    SimulationViewHelper.draw(simulationCanvas,
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

        ParameterPanelInitializer.initialize(
            viewModel::getHabitat,
            resources,
            stage,
            truckPeriodInput,
            carPeriodInput,
            truckProbabilityBox,
            carProbabilityBox,
            truckLifetimeInput,
            carLifetimeInput,
            truckSpeedInput,
            carSpeedInput
        );

        setupLayoutListener();
        updateUIState(viewModel.getState());

        initPriorityComboBoxes();
        bindAIControls();
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
        viewModel.currentTruckCountProperty()
            .addListener((_, _, newValue) ->
                currentTruckRow.setValue(newValue.intValue())
            );
        viewModel.currentCarCountProperty()
            .addListener((_, _, newValue) ->
                currentCarRow.setValue(newValue.intValue())
            );

        viewModel.onStateChanged(this::updateUIState);
        SimulationViewHelper.bindCanvasSize(
            simulationCanvas,
            simulationField
        );
    }

    private void initPriorityComboBoxes() {
        final var priorities = java.util.stream.IntStream
            .rangeClosed(Thread.MIN_PRIORITY, Thread.MAX_PRIORITY)
            .boxed()
            .toList();

        truckAIControl.getPriorityComboBox()
            .getItems()
            .addAll(priorities);
        truckAIControl.getPriorityComboBox()
            .setValue(Thread.NORM_PRIORITY);

        carAIControl.getPriorityComboBox()
            .getItems()
            .addAll(priorities);
        carAIControl.getPriorityComboBox()
            .setValue(Thread.NORM_PRIORITY);
    }

    private void bindAIControls() {
        truckAIControl.getPriorityComboBox()
            .valueProperty()
            .addListener((_, _, value) -> {
                if (value != null) {
                    viewModel.setTruckAIPriority(value);
                }
            });
        carAIControl.getPriorityComboBox()
            .valueProperty()
            .addListener((_, _, value) -> {
                if (value != null) {
                    viewModel.setCarAIPriority(value);
                }
            });

        truckAIControl.setToggleAction(paused -> {
            if (paused) {
                viewModel.pauseTruckAI();
            } else {
                viewModel.resumeTruckAI();
            }
        });
        carAIControl.setToggleAction(paused -> {
            if (paused) {
                viewModel.pauseCarAI();
            } else {
                viewModel.resumeCarAI();
            }
        });
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
        gameTimer.stop();
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
        updateTimeRadio();
        syncTimeDisplayUI();
    }

    @FXML
    private void exitApplication() {
        viewModel.stopSimulation();
        gameTimer.stop();
        Platform.exit();
    }

    @FXML
    private void showAboutDialog() {
        AboutDialog.showAboutDialog(stage, resources);
    }

    @FXML
    private void showCurrentObjectsDialog() {
        CurrentObjectsDialog.showDialog(
            stage,
            viewModel.getHabitat().getVehicleCollection(),
            resources
        );
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
            totalRow,
            currentTruckRow,
            currentCarRow
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

        timeToggleGroup.selectedToggleProperty()
            .addListener((_, _, newValue) -> {
                final var shouldBeVisible = newValue == showTimeRadio;

                if (shouldBeVisible != viewModel.isShowTime()) {
                    viewModel.toggleShowTime();
                    syncTimeDisplayUI();
                }
            });
    }

    private void syncTimeDisplayUI() {
        SimulationViewHelper.setStatusTimeVisible(
            viewModel.isShowTime(),
            statusTimeContainer,
            statusContainer
        );
        SimulationViewHelper.updateMenuTimeText(
            viewModel.isShowTime(),
            resources,
            menuToggleTime,
            menuTimeIcon
        );
        simulationCanvas.requestFocus();
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
        menuCurrentObjects.setAccelerator(KeyCodeCombination.valueOf(
                resources.getString(
                    "label.controls.keybind.current.objects")
            )
        );
    }

    private void setupLayoutListener() {
        simulationField.layoutBoundsProperty()
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

        Platform.runLater(() -> {
            final var habitat = viewModel.getHabitat();

            if (habitat != null) {
                SimulationViewHelper.draw(simulationCanvas, habitat);
            }
        });
    }
}