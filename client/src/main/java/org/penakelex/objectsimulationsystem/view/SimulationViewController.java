package org.penakelex.objectsimulationsystem.view;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;
import org.penakelex.objectsimulationsystem.model.config.ConfigurationManager;
import org.penakelex.objectsimulationsystem.model.habitat.TimeUnit;
import org.penakelex.objectsimulationsystem.model.vehicle.Car;
import org.penakelex.objectsimulationsystem.model.vehicle.Truck;
import org.penakelex.objectsimulationsystem.model.vehicle.Vehicle;
import org.penakelex.objectsimulationsystem.model.vehicle.VehicleNetworkConverter;
import org.penakelex.objectsimulationsystem.network.NetworkClient;
import org.penakelex.objectsimulationsystem.ui.components.*;
import org.penakelex.objectsimulationsystem.ui.dialogs.*;
import org.penakelex.objectsimulationsystem.ui.helpers.ParameterPanelInitializer;
import org.penakelex.objectsimulationsystem.ui.helpers.SimulationViewHelper;
import org.penakelex.objectsimulationsystem.model.vehicle.images.CarImages;
import org.penakelex.objectsimulationsystem.model.vehicle.images.TruckImages;
import org.penakelex.objectsimulationsystem.viewmodel.SimulationLoadResult;
import org.penakelex.objectsimulationsystem.viewmodel.SimulationState;
import org.penakelex.objectsimulationsystem.viewmodel.SimulationViewModel;
import org.penakelex.shared.network.ClientToServerMessage;
import org.penakelex.shared.network.ServerToClientMessage;
import org.penakelex.shared.network.VehicleType;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.IntStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public final class SimulationViewController implements Initializable {
    @FXML private MenuItem menuStart, menuRestart, menuStop,
        menuPause, menuCurrentObjects;
    @FXML private CheckMenuItem menuToggleTime;
    @FXML private FontIcon menuTimeIcon;
    @FXML private Canvas simulationCanvas;
    @FXML private StackPane loadingOverlay;
    @FXML private StackPane simulationField;
    @FXML private ToolbarButton startButton, stopButton, pauseButton,
        restartButton;
    @FXML private StackPane statusTimeContainer;
    @FXML private LabeledValueRow statusTimeRow;
    @FXML private LabeledValueRow truckRow, carRow, totalRow,
        currentTruckRow, currentCarRow, currentTotalRow;
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
    @FXML private LabeledTextRow serverHostInput;
    @FXML private LabeledTextRow serverPortInput;
    @FXML private Button connectToServerButton,
        disconnectFromServerButton;
    @FXML private Label connectionStatusText, clientIdentifierText;
    @FXML private ComboBox<String> connectedClientsComboBox,
        exchangeVehicleTypeComboBox;
    @FXML private Button exchangeVehiclesButton;

    private SimulationViewModel viewModel;
    private AnimationTimer gameTimer;
    private Stage stage;
    private ResourceBundle resources;
    private Task<SimulationLoadResult> currentLoadTask;
    private NetworkClient networkClient;
    private String connectedClientIdentifier;

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

        exchangeVehicleTypeComboBox.getItems().addAll(
            resources.getString("label.network.type.truck"),
            resources.getString("label.network.type.car")
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
        viewModel.currentTotalCountProperty()
            .addListener((_, _, newValue) ->
                currentTotalRow.setValue(newValue.intValue())
            );

        viewModel.onStateChanged(this::updateUIState);
        SimulationViewHelper.bindCanvasSize(
            simulationCanvas,
            simulationField
        );
    }

    private void initPriorityComboBoxes() {
        final var priorities = IntStream
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
            .addListener((_, _, newValue) -> {
                if (newValue != null) {
                    viewModel.setTruckAIPriority(newValue);
                }
            });
        carAIControl.getPriorityComboBox()
            .valueProperty()
            .addListener((_, _, newValue) -> {
                if (newValue != null) {
                    viewModel.setCarAIPriority(newValue);
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
        SimulationViewHelper.updateStatusTime(
            viewModel.getElapsedTime(),
            resources,
            statusTimeRow
        );
        gameTimer.stop();
        SimulationViewHelper.setNodeVisible(infoContainer, false);

        if (showStatisticsCheckBox.isSelected()) {
            SimulationViewHelper.setStatusTimeVisible(false,
                statusTimeContainer,
                statusContainer
            );

            final var shouldStop =
                StatisticsDialog.showStatisticsDialog(
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
        SimulationViewHelper.updateStatusTime(
            viewModel.getElapsedTime(),
            resources,
            statusTimeRow
        );
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
        synchronizeTimeDisplayUI();
    }

    @FXML
    public void exitApplication() {
        saveConfiguration();
        ConfigurationManager.save();
        viewModel.stopSimulation();
        gameTimer.stop();
        handleDisconnectFromServer();
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

    @FXML
    private void showConsoleDialog() {
        ConsoleDialog.showDialog(
            stage,
            viewModel::pauseUserAI,
            viewModel::resumeUserAI,
            resources,
            paused -> {
                truckAIControl.setPausedSilent(paused);
                carAIControl.setPausedSilent(paused);
            }
        );
    }

    @FXML
    private void saveSimulation() {
        final var fileChooser = new FileChooser();
        fileChooser.setTitle(resources.getString(
            "dialog.save.title"
        ));
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter(
                resources.getString("dialog.file.filter.name"),
                "*.sim"
            )
        );

        final var selectedFile = fileChooser.showSaveDialog(stage);

        if (selectedFile == null) {
            return;
        }

        try (final var fos = new FileOutputStream(selectedFile);
             final var gzos = new GZIPOutputStream(fos);
             final var oos = new ObjectOutputStream(gzos)
        ) {
            oos.writeLong(viewModel.getElapsedTime());

            final var statistics =
                viewModel.getHabitat().getStatistics();
            oos.writeInt(statistics.trucks());
            oos.writeInt(statistics.cars());
            oos.writeInt(statistics.currentTrucks());
            oos.writeInt(statistics.currentCars());

            oos.writeObject(viewModel.getHabitat()
                .getVehicleCollection()
                .getAll()
            );
        } catch (final IOException _) {
            ErrorDialog.showError(
                stage,
                resources,
                resources.getString("dialog.save.error.message"),
                resources.getString("dialog.save.error.header")
            );
        }
    }

    @FXML
    private void loadSimulation() {
        if (currentLoadTask != null && !currentLoadTask.isDone()) {
            currentLoadTask.cancel(true);
            return;
        }

        final var fileChooser = new FileChooser();
        fileChooser.setTitle(resources.getString("dialog.load.title"));
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter(
                resources.getString("dialog.file.filter.name"),
                "*.sim"
            )
        );

        final var selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile == null) {
            return;
        }

        Platform.runLater(() -> {
            showLoadingOverlay();
            setSimulationControlsBlocked(true);
        });

        currentLoadTask = new Task<>() {
            @Override
            protected SimulationLoadResult call() throws Exception {
                try (
                    final var fis = new FileInputStream(selectedFile);
                    final var gzis = new GZIPInputStream(fis);
                    final var ois = new ObjectInputStream(gzis)
                ) {
                    final long loadedTime = ois.readLong();
                    updateProgress(0.15, 1.0);

                    Platform.runLater(() ->
                        SimulationViewHelper.updateStatusTime(
                            loadedTime,
                            resources,
                            statusTimeRow
                        )
                    );

                    if (isCancelled()) {
                        return null;
                    }

                    final int totalTrucks = ois.readInt();
                    final int totalCars = ois.readInt();
                    final int currentTrucks = ois.readInt();
                    final int currentCars = ois.readInt();

                    if (isCancelled()) {
                        return null;
                    }

                    final var rawList = (List<?>) ois.readObject();

                    updateProgress(0.4, 1.0);

                    if (isCancelled()) {
                        return null;
                    }

                    final var vehicles =
                        new ArrayList<Vehicle>(rawList.size());
                    final double canvasWidth =
                        simulationCanvas.getWidth();
                    final double canvasHeight =
                        simulationCanvas.getHeight();

                    for (int i = 0; i < rawList.size(); i++) {
                        final var item = rawList.get(i);

                        if (!(item instanceof Vehicle vehicle)) {
                            throw new IOException(resources.getString(
                                "error.load.invalid_type"
                            ));
                        }

                        final var images = switch (vehicle) {
                            case Truck _ ->
                                viewModel.getTruckImages();
                            case Car _ -> viewModel.getCarImages();
                        };

                        images.getImageForResourceIndex(
                            vehicle.getImageResourceIndex()
                        ).ifPresentOrElse(
                            vehicle::setImage,
                            () -> {
                                final var imageWithIndex =
                                    images.getRandomImageWithIndex();
                                vehicle.setImageWithResourceIndex(
                                    imageWithIndex.getLeft(),
                                    imageWithIndex.getRight()
                                );
                            }
                        );

                        vehicle.onCanvasSizeUpdated(
                            canvasWidth,
                            canvasHeight
                        );
                        vehicles.add(vehicle);

                        if (i % 50 == 0) {
                            updateProgress(
                                0.4 + (0.6 * i / rawList.size()),
                                1.0
                            );

                            if (isCancelled()) {
                                return null;
                            }
                        }
                    }

                    return new SimulationLoadResult(
                        loadedTime,
                        vehicles,
                        totalTrucks,
                        totalCars,
                        currentTrucks,
                        currentCars
                    );
                }
            }
        };

        currentLoadTask.setOnSucceeded(_ -> {
            final var result = currentLoadTask.getValue();

            if (result != null) {
                applyLoadedData(result);
            }

            hideLoadingOverlay();
            setSimulationControlsBlocked(false);
            currentLoadTask = null;
        });

        currentLoadTask.setOnFailed(_ -> {
            final var exception = currentLoadTask.getException();

            if (!(exception instanceof InterruptedException) &&
                !currentLoadTask.isCancelled()) {
                ErrorDialog.showError(
                    stage,
                    resources,
                    resources
                        .getString("dialog.load.error.message"),
                    resources
                        .getString("dialog.load.error.header")
                );
            }

            hideLoadingOverlay();
            resetStatusTime();
            setSimulationControlsBlocked(false);
            currentLoadTask = null;
        });

        currentLoadTask.setOnCancelled(_ -> {
            hideLoadingOverlay();
            resetStatusTime();
            setSimulationControlsBlocked(false);
            currentLoadTask = null;
        });

        new Thread(currentLoadTask, "SimulationLoader").start();
    }

    @FXML
    private void handleConnectToServer() {
        final var serverHost = serverHostInput.getFieldText().trim();
        final var serverPortText =
            serverPortInput.getFieldText().trim();

        if (serverHost.isEmpty() || serverPortText.isEmpty()) {
            connectionStatusText.setText(resources.getString(
                "label.network.error.connection"
            ));
            return;
        }

        final int serverPort;

        try {
            serverPort = Integer.parseInt(serverPortText);
        } catch (final NumberFormatException _) {
            connectionStatusText.setText(resources.getString(
                "label.network.error.connection"
            ));
            return;
        }

        try {
            networkClient = new NetworkClient(
                serverHost,
                serverPort,
                this::processServerMessage
            );

            serverHostInput.setDisable(true);
            serverPortInput.setDisable(true);

            connectionStatusText.setText(resources.getString(
                "label.network.status.connecting"
            ));
            connectToServerButton.setDisable(true);
            disconnectFromServerButton.setDisable(false);
        } catch (final IOException networkException) {
            connectionStatusText.setText(String.format("%s: %s",
                resources.getString("label.network.error.connection"),
                networkException.getMessage()
            ));
            networkClient = null;
        }
    }

    @FXML
    private void handleDisconnectFromServer() {
        serverHostInput.setDisable(false);
        serverPortInput.setDisable(false);

        if (networkClient != null) {
            try {
                networkClient.close();
            } catch (final IOException _) {
            }

            networkClient = null;
        }

        connectedClientIdentifier = null;
        clientIdentifierText.setVisible(false);
        clientIdentifierText.setManaged(false);

        connectedClientsComboBox.getItems().clear();
        connectedClientsComboBox.setDisable(true);
        exchangeVehicleTypeComboBox.setDisable(true);
        exchangeVehiclesButton.setDisable(true);

        connectionStatusText.setText(resources
            .getString("label.network.status.disconnected")
        );
        connectToServerButton.setDisable(false);
        disconnectFromServerButton.setDisable(true);
    }

    @FXML
    private void handleExchangeVehicles() {
        if (networkClient == null) {
            return;
        }

        final var targetClientIdentifier =
            connectedClientsComboBox.getValue();
        final var vehicleTypeLabel =
            exchangeVehicleTypeComboBox.getValue();

        if (targetClientIdentifier == null ||
            vehicleTypeLabel == null) {
            return;
        }

        final var truckLabel =
            resources.getString("label.network.type.truck");
        final var vehicleType = vehicleTypeLabel.equals(truckLabel)
            ? VehicleType.Truck
            : VehicleType.Car;

        try {
            networkClient.send(
                new ClientToServerMessage.VehicleExchangeRequest(
                    targetClientIdentifier,
                    vehicleType
                )
            );
        } catch (final IOException networkException) {
            connectionStatusText.setText(String.format("%s: %s",
                resources.getString("label.network.error.exchange"),
                networkException.getMessage()
            ));
        }
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
                SimulationViewHelper
                    .setNodeVisible(startButton, true);
                SimulationViewHelper.setNodesVisible(false,
                    pauseButton,
                    stopButton,
                    restartButton
                );
            }
            case Running -> {
                SimulationViewHelper
                    .setNodeVisible(startButton, false);
                SimulationViewHelper.setNodesVisible(true,
                    pauseButton,
                    stopButton,
                    restartButton
                );
            }
            case Paused -> {
                SimulationViewHelper
                    .setNodeVisible(pauseButton, false);
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
            currentCarRow,
            currentTotalRow
        );
        SimulationViewHelper.updateStatusTime(
            viewModel.getElapsedTime(),
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
                    synchronizeTimeDisplayUI();
                }
            });
    }

    private void synchronizeTimeDisplayUI() {
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
        menuCurrentObjects.setAccelerator(KeyCodeCombination.valueOf(
            resources.getString(
                "label.controls.keybind.current.objects"
            )
        ));
    }

    private void setupLayoutListener() {
        simulationField.layoutBoundsProperty()
            .addListener((_, _, newValue) -> {
                if (viewModel.getHabitat() != null) {
                    viewModel.getHabitat().setSize(
                        newValue.getWidth(),
                        newValue.getHeight()
                    );
                    Platform.runLater(() ->
                        SimulationViewHelper.draw(
                            simulationCanvas,
                            viewModel.getHabitat()
                        )
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

        scene.addEventFilter(
            MouseEvent.MOUSE_CLICKED,
            event -> {
                if (!isInsideInputControl((Node) event.getTarget())) {
                    Platform.runLater(() -> simulationCanvas.requestFocus());
                }
            }
        );

        simulationCanvas.setFocusTraversable(true);
        simulationCanvas.requestFocus();
    }

    private boolean isInsideInputControl(Node node) {
        while (node != null) {
            if (node instanceof TextInputControl ||
                node instanceof ComboBoxBase) {
                return true;
            }

            node = node.getParent();
        }

        return false;
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

        Platform.runLater(() -> SimulationViewHelper.draw(
            simulationCanvas,
            viewModel.getHabitat()
        ));

        ConfigurationManager.load();
        restoreConfiguration();
    }

    private void applyLoadedData(final SimulationLoadResult result) {
        viewModel.stopSimulation();

        final var habitat = viewModel.getHabitat();
        habitat.reset();
        habitat.getVehicleCollection()
            .loadVehicles(result.vehicles());
        habitat.synchronizeSpawnerTimes(result.elapsedTime());
        habitat.restoreStatistics(
            result.totalTrucks(),
            result.totalCars(),
            result.currentTrucks(),
            result.currentCars()
        );

        viewModel.setElapsedTimeForLoad(result.elapsedTime());
        viewModel.updateStatistics();
        updatePanelStatisticsWithTime();

        SimulationViewHelper.draw(simulationCanvas, habitat);
    }

    private void showLoadingOverlay() {
        loadingOverlay.setVisible(true);
        loadingOverlay.setManaged(true);
    }

    private void hideLoadingOverlay() {
        loadingOverlay.setVisible(false);
        loadingOverlay.setManaged(false);
    }

    private void resetStatusTime() {
        SimulationViewHelper.updateStatusTime(
            0,
            resources,
            statusTimeRow
        );
    }

    private void setSimulationControlsBlocked(final boolean blocked) {
        startButton.setDisable(blocked);
        menuStart.setDisable(blocked);
    }

    private void saveConfiguration() {
        ConfigurationManager.setShowTime(viewModel.isShowTime());

        try {
            ConfigurationManager.setTruckPeriod(Integer.parseInt(
                truckPeriodInput.getFieldText()));
        } catch (final NumberFormatException _) {
        }

        try {
            ConfigurationManager.setCarPeriod(Integer.parseInt(
                carPeriodInput.getFieldText()));
        } catch (final NumberFormatException _) {
        }

        Arrays.stream(TimeUnit.values())
            .filter(timeUnit -> resources
                .getString(timeUnit.messageKey)
                .equals(truckPeriodInput.getComboBoxValue())
            )
            .findFirst()
            .ifPresent(ConfigurationManager::setTruckPeriodUnit);
        Arrays.stream(TimeUnit.values())
            .filter(timeUnit -> resources
                .getString(timeUnit.messageKey)
                .equals(carPeriodInput.getComboBoxValue())
            )
            .findFirst()
            .ifPresent(ConfigurationManager::setCarPeriodUnit);

        ConfigurationManager.setTruckProbability(
            truckProbabilityBox.selectedIndexProperty().get() / 10.0
        );
        ConfigurationManager.setCarProbability(
            carProbabilityBox.selectedIndexProperty().get() / 10.0
        );

        try {
            ConfigurationManager.setTruckLifetime(Integer.parseInt(
                truckLifetimeInput.getFieldText()
            ));
        } catch (final NumberFormatException _) {
        }

        try {
            ConfigurationManager.setCarLifetime(Integer.parseInt(
                carLifetimeInput.getFieldText()
            ));
        } catch (final NumberFormatException _) {
        }

        Arrays.stream(TimeUnit.values())
            .filter(timeUnit -> resources
                .getString(timeUnit.messageKey)
                .equals(truckLifetimeInput.getComboBoxValue())
            )
            .findFirst()
            .ifPresent(ConfigurationManager::setTruckLifetimeUnit);
        Arrays.stream(TimeUnit.values())
            .filter(timeUnit -> resources
                .getString(timeUnit.messageKey)
                .equals(carLifetimeInput.getComboBoxValue())
            )
            .findFirst()
            .ifPresent(ConfigurationManager::setCarLifetimeUnit);

        try {
            ConfigurationManager.setTruckSpeed(Integer.parseInt(
                truckSpeedInput.getFieldText()
            ));
        } catch (final NumberFormatException _) {
        }

        try {
            ConfigurationManager.setCarSpeed(Integer.parseInt(
                carSpeedInput.getFieldText()
            ));
        } catch (final NumberFormatException _) {
        }

        ConfigurationManager
            .setShowStatistics(showStatisticsCheckBox.isSelected());

        ConfigurationManager.setTruckAIPaused(truckAIControl.isPaused());
        ConfigurationManager.setCarAIPaused(carAIControl.isPaused());
        final var truckPriority =
            truckAIControl.getPriorityComboBox().getValue();
        final var carPriority =
            carAIControl.getPriorityComboBox().getValue();

        if (truckPriority != null) {
            ConfigurationManager.setTruckAIPriority(truckPriority);
        }

        if (carPriority != null) {
            ConfigurationManager.setCarAIPriority(carPriority);
        }


        ConfigurationManager
            .setServerHost(serverHostInput.getFieldText());

        try {
            ConfigurationManager.setServerPort(
                Integer.parseUnsignedInt(
                    serverPortInput.getFieldText()
                )
            );
        } catch (final NumberFormatException _) {
        }
    }

    private void restoreConfiguration() {
        if (ConfigurationManager.isShowTime() !=
            viewModel.isShowTime()) {
            viewModel.toggleShowTime();
        }

        updateTimeRadio();
        synchronizeTimeDisplayUI();

        truckPeriodInput
            .setTextFieldValue(ConfigurationManager.getTruckPeriod());
        carPeriodInput
            .setTextFieldValue(ConfigurationManager.getCarPeriod());

        truckPeriodInput.setComboBoxValue(resources.getString(
            ConfigurationManager.getTruckPeriodUnit().messageKey
        ));
        carPeriodInput.setComboBoxValue(resources.getString(
            ConfigurationManager.getCarPeriodUnit().messageKey
        ));

        truckProbabilityBox.setSelectedIndex((int) Math.round(
            ConfigurationManager.getTruckProbability() * 10
        ));
        carProbabilityBox.setSelectedIndex((int) Math.round(
            ConfigurationManager.getCarProbability() * 10
        ));

        truckLifetimeInput
            .setTextFieldValue(ConfigurationManager.getTruckLifetime());
        carLifetimeInput
            .setTextFieldValue(ConfigurationManager.getCarLifetime());
        truckLifetimeInput.setComboBoxValue(resources.getString(
            ConfigurationManager.getTruckLifetimeUnit().messageKey
        ));
        carLifetimeInput.setComboBoxValue(resources.getString(
            ConfigurationManager.getCarLifetimeUnit().messageKey
        ));

        truckSpeedInput
            .setTextFieldValue(ConfigurationManager.getTruckSpeed());
        carSpeedInput
            .setTextFieldValue(ConfigurationManager.getCarSpeed());

        updateTimeRadio();
        showStatisticsCheckBox
            .setSelected(ConfigurationManager.isShowStatistics());

        final var truckPriority =
            ConfigurationManager.getTruckAIPriority();
        final var carPriority =
            ConfigurationManager.getCarAIPriority();

        truckAIControl.getPriorityComboBox().setValue(truckPriority);
        carAIControl.getPriorityComboBox().setValue(carPriority);

        truckAIControl
            .setPausedSilent(ConfigurationManager.isTruckAIPaused());
        carAIControl
            .setPausedSilent(ConfigurationManager.isCarAIPaused());

        if (ConfigurationManager.isTruckAIPaused()) {
            viewModel.pauseTruckAI();
        } else {
            viewModel.resumeTruckAI();
        }

        if (ConfigurationManager.isCarAIPaused()) {
            viewModel.pauseCarAI();
        } else {
            viewModel.resumeCarAI();
        }

        serverHostInput
            .setTextFieldValue(ConfigurationManager.getServerHost());
        serverPortInput
            .setTextFieldValue(ConfigurationManager.getServerPort());
    }

    private void processServerMessage(final ServerToClientMessage message) {
        Platform.runLater(() -> {
            switch (message) {
                case ServerToClientMessage.ClientList clientList ->
                    handleClientListUpdate(clientList);
                case ServerToClientMessage.VehicleStateRequest
                         stateRequest ->
                    handleStateRequest(stateRequest);
                case ServerToClientMessage.VehicleExchangeResponse
                         exchangeResponse ->
                    handleExchangeResponse(exchangeResponse);
                case ServerToClientMessage.ErrorMessage
                         errorMessage -> handleError(errorMessage);
            }
        });
    }

    private void handleClientListUpdate(final ServerToClientMessage.ClientList clientList) {
        if (clientList.assignedClientId() != null) {
            connectedClientIdentifier = clientList.assignedClientId();
            clientIdentifierText.setText(resources
                .getString("label.network.client.id")
                .formatted(connectedClientIdentifier)
            );
            clientIdentifierText.setVisible(true);
            clientIdentifierText.setManaged(true);
        }

        final var allClients = clientList.clientIds();
        final var otherClients = allClients.stream()
            .filter(identifier ->
                !identifier.equals(connectedClientIdentifier)
            )
            .toList();

        connectedClientsComboBox.getItems().setAll(otherClients);

        if (!otherClients.isEmpty()) {
            connectedClientsComboBox.getSelectionModel()
                .selectFirst();
        }

        final var isConnected =
            networkClient != null && networkClient.isConnected();
        connectedClientsComboBox.setDisable(!isConnected);
        exchangeVehicleTypeComboBox.setDisable(!isConnected);
        exchangeVehiclesButton.setDisable(!isConnected);
        connectionStatusText.setText(resources
            .getString("label.network.status.connected")
        );
    }

    private void handleStateRequest(
        final ServerToClientMessage.VehicleStateRequest stateRequest
    ) {
        if (networkClient == null) {
            return;
        }

        final var vehicleType = stateRequest.vehicleType();
        final var vehiclesToSend = viewModel
            .getHabitat()
            .getVehicleCollection()
            .getAll()
            .stream()
            .filter(vehicle -> matchesVehicleType(vehicle,
                vehicleType
            ))
            .map(VehicleNetworkConverter::toState)
            .toList();

        try {
            networkClient.send(
                new ClientToServerMessage.VehicleStateResponse(
                    stateRequest.exchangeId(),
                    connectedClientIdentifier,
                    vehicleType,
                    vehiclesToSend
                )
            );
        } catch (final IOException networkException) {
            connectionStatusText.setText(String.format("%s: %s",
                resources.getString("label.network.error.exchange"),
                networkException.getMessage()
            ));
        }
    }

    private void handleExchangeResponse(
        final ServerToClientMessage.VehicleExchangeResponse
            exchangeResponse
    ) {
        final var vehicleType = exchangeResponse.vehicleType();

        final var identifiersToRemove = viewModel.getHabitat()
            .getVehicleCollection()
            .getAll()
            .stream()
            .filter(vehicle -> matchesVehicleType(
                vehicle,
                vehicleType
            ))
            .map(Vehicle::getId)
            .toList();
        viewModel.getHabitat()
            .getVehicleCollection()
            .removeByIds(identifiersToRemove);

        final var canvasWidth = simulationCanvas.getWidth();
        final var canvasHeight = simulationCanvas.getHeight();

        final var receivedVehicles = exchangeResponse.vehicles()
            .stream()
            .map(vehicleState -> VehicleNetworkConverter.fromState(
                vehicleState,
                viewModel.getHabitat()
                    .getVehicleCollection()::getNextId,
                viewModel.getTruckImages(),
                viewModel.getCarImages(),
                canvasWidth,
                canvasHeight
            ))
            .toList();

        viewModel.getHabitat().getVehicleCollection()
            .addAll(receivedVehicles);

        viewModel.updateStatistics();
        SimulationViewHelper.draw(simulationCanvas,
            viewModel.getHabitat()
        );
    }

    private void handleError(final ServerToClientMessage.ErrorMessage errorMessage) {
        connectionStatusText.setText(resources
            .getString("label.network.error.template")
            .formatted(errorMessage.message())
        );
        connectionStatusText.getStyleClass()
            .removeAll("connected", "connecting", "error");
        connectionStatusText.getStyleClass().add("error");
    }

    private boolean matchesVehicleType(
        final Vehicle vehicle,
        final VehicleType vehicleType
    ) {
        return switch (vehicleType) {
            case Truck -> vehicle instanceof Truck;
            case Car -> vehicle instanceof Car;
        };
    }
}