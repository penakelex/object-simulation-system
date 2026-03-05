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
                habitat.draw(graphicsContext);
            });
        simulationCanvas.heightProperty()
            .addListener((_, _, newValue) -> {
                habitat.setHeight(newValue.doubleValue());
                habitat.draw(graphicsContext);
            });

        setupGameLoop();
        updateStatistics();
    }

    public void setupKeyboardHandler(final Scene scene) {
        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case B -> startSimulation();
                case E -> stopSimulation();
                case T -> toggleTimeDisplay();
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

        running = true;
        startTime = System.currentTimeMillis();
        elapsedTime = 0;
        habitat.reset();
        statusLabel.setText("▶️ Запущено");
        statusLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #4CAF50;");
        simulationCanvas.requestFocus();
    }

    private void stopSimulation() {
        running = false;
        elapsedTime = System.currentTimeMillis() - startTime;
        statusLabel.setText("⏸️ Остановлено");
        statusLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #F44336;");
        showStatistics();
        updateStatistics();
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

        if (habitat != null) {
            for (final var vehicle : habitat.getVehicles()) {
                switch (vehicle) {
                    case Car _ -> cars++;
                    case Truck _ -> trucks++;
                }
            }
        }

        truckCountLabel.setText(String.valueOf(trucks));
        carCountLabel.setText(String.valueOf(cars));
        totalCountLabel.setText(String.valueOf(trucks + cars));

        if (showTime) {
            timeLabel.setText(String.format("%d мс", elapsedTime));
        }
    }

    private void showStatistics() {
//        int trucks = 0;
//        int cars = 0;
//
//        for (var vehicle : habitat.getVehicles()) {
//            if (vehicle instanceof Truck) {
//                trucks++;
//            } else if (vehicle instanceof Car) {
//                cars++;
//            }
//        }

        // Показываем финальную статистику в виджетах
        truckCountLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #4CAF50;");
        carCountLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #2196F3;");
        totalCountLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #FF5722;");
    }

    public void requestFocus() {
        simulationCanvas.requestFocus();
    }
}