package org.penakelex.objectsimulationsystem;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.javafx.FontIcon;
import org.penakelex.objectsimulationsystem.habitat.Habitat;
import org.penakelex.objectsimulationsystem.ui.LabeledValueRow;
import org.penakelex.objectsimulationsystem.vehicle.Car;
import org.penakelex.objectsimulationsystem.vehicle.Truck;

import java.net.URL;
import java.util.ResourceBundle;

public class SimulationController implements Initializable {
    @FXML
    private Canvas simulationCanvas;
    @FXML
    private Label timeLabel;
    @FXML
    private LabeledValueRow truckRow;
    @FXML
    private LabeledValueRow carRow;
    @FXML
    private LabeledValueRow totalRow;
    @FXML
    private Label statusLabel;
    @FXML
    private FontIcon statusIcon;
    @FXML
    private StackPane canvasContainer;
    private AnimationTimer gameTimer;

    private Habitat habitat;
    private GraphicsContext graphicsContext;
    private boolean running = false;
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
        habitat = new Habitat(
            simulationCanvas.getWidth(),
            simulationCanvas.getHeight()
        );

        gameTimer = new AnimationTimer() {
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

        updateStatistics();
        updateStatusLabel();

        simulationCanvas.widthProperty()
            .bind(canvasContainer.widthProperty());
        simulationCanvas.heightProperty()
            .bind(canvasContainer.heightProperty());

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

    private void startSimulation() {
        if (running) {
            return;
        }

        startTime = System.currentTimeMillis() - elapsedTime;
        running = true;
        updateStatusLabel();
        simulationCanvas.requestFocus();
        gameTimer.start();
    }

    private void stopSimulation() {
        running = false;
        gameTimer.stop();
        updateStatusLabel();
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
        int trucks = 0, cars = 0;

        for (final var vehicle : habitat.getVehicles()) {
            switch (vehicle) {
                case Car _ -> cars++;
                case Truck _ -> trucks++;
            }
        }

        truckRow.setValue(trucks);
        carRow.setValue(cars);
        totalRow.setValue(trucks + cars);

        if (showTime) {
            timeLabel.setText(resources
                .getString("format.time.milliseconds")
                .formatted(elapsedTime)
            );
        }
    }

    private void updateStatusLabel() {

        if (running) {
            statusLabel.setText(resources.getString(
                "label.status.running"
            ));
            statusLabel.getStyleClass()
                .setAll("status-value", "status-running");

            statusIcon.setIconLiteral("fas-play-circle");
            statusIcon.setIconColor(Color.web("#4CAF50"));
        } else {
            statusLabel.setText(resources.getString(
                "label.status.stopped"
            ));
            statusLabel.getStyleClass()
                .setAll("status-value", "status-stopped");

            statusIcon.setIconLiteral("fas-pause-circle");
            statusIcon.setIconColor(Color.web("#F44336"));
        }
    }

    public void requestFocus() {
        simulationCanvas.requestFocus();
    }
}