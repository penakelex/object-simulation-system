package org.penakelex.objectsimulationsystem;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import org.penakelex.objectsimulationsystem.habitat.Habitat;
import org.penakelex.objectsimulationsystem.vehicle.Car;
import org.penakelex.objectsimulationsystem.vehicle.Truck;

public class SimulationController {
    @FXML
    private Canvas simulationCanvas;
    @FXML
    private Label timeLabel;
    @FXML
    private Label truckCountLabel;
    @FXML
    private Label carCountLabel;
    @FXML
    private Label totalCountLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private StackPane canvasContainer;

    private Habitat habitat;
    private GraphicsContext graphicsContext;
    private boolean running = false;
    private boolean showTime = true;
    private long startTime = 0;
    private long elapsedTime = 0;

    @FXML
    public void initialize() {
        graphicsContext = simulationCanvas.getGraphicsContext2D();
        habitat = new Habitat(
            simulationCanvas.getWidth(),
            simulationCanvas.getHeight()
        );

        setupGameLoop();
        updateStatistics();

        simulationCanvas.widthProperty().bind(canvasContainer
            .widthProperty()
        );
        simulationCanvas.heightProperty().bind(canvasContainer
            .heightProperty()
        );
        simulationCanvas.widthProperty()
            .addListener((_, _, newValue) -> {
                habitat.setWidth(newValue.doubleValue());
                draw();
            });
        simulationCanvas.heightProperty()
            .addListener((_, _, newValue) -> {
                habitat.setHeight(newValue.doubleValue());
                draw();
            });
    }

    public void setupKeyboardHandler(final Scene scene) {
        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case B -> startSimulation();
                case E -> stopSimulation();
                case T -> toggleTimeDisplay();
                case R -> restartSimulation();
            }
        });

        simulationCanvas.setFocusTraversable(true);
        simulationCanvas.requestFocus();
    }

    private void setupGameLoop() {
        final var gameTimer = new AnimationTimer() {
            @Override
            public void handle(final long now) {
                if (running) {
                    elapsedTime =
                        System.currentTimeMillis() - startTime;
                    habitat.update(elapsedTime);
                    draw();
                    updateStatistics();
                }
            }
        };

        gameTimer.start();
    }

    private void startSimulation() {
        if (running) {
            return;
        }

        startTime = System.currentTimeMillis() - elapsedTime;
        running = true;
        statusLabel.setText("▶️ Запущено");
        statusLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #4CAF50;");
        simulationCanvas.requestFocus();
    }

    private void stopSimulation() {
        running = false;
        statusLabel.setText("⏸️ Остановлено");
        statusLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #F44336;");
        updateStatistics();
    }

    private void restartSimulation() {
        startTime = System.currentTimeMillis();
        elapsedTime = 0;
        habitat.reset();
        updateStatistics();
        draw();
    }

    private void toggleTimeDisplay() {
        showTime = !showTime;
        timeLabel.setVisible(showTime);
        timeLabel.setManaged(showTime);
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

    private void updateStatistics() {
        int trucks = 0;
        int cars = 0;

        for (final var vehicle : habitat.getVehicles()) {
            switch (vehicle) {
                case Car _ -> cars++;
                case Truck _ -> trucks++;
            }
        }

        truckCountLabel.setText(String.valueOf(trucks));
        carCountLabel.setText(String.valueOf(cars));
        totalCountLabel.setText(String.valueOf(trucks + cars));

        if (showTime) {
            timeLabel.setText(String.format("%d мс", elapsedTime));
        }
    }

    public void requestFocus() {
        simulationCanvas.requestFocus();
    }
}