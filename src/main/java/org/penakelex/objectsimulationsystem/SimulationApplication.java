package org.penakelex.objectsimulationsystem;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;

public class SimulationApplication extends Application {
    @Override
    public void start(final Stage stage) throws IOException {
        final var loader = new FXMLLoader(SimulationApplication.class
            .getResource("simulation-view.fxml")
        );

        final HBox root = loader.load();
        final var scene = new Scene(
            root, root.getPrefWidth(), root.getPrefHeight()
        );
        final SimulationController controller =
            loader.getController();

        stage.setTitle("🚗 Симуляция автомобилей");
        stage.setScene(scene);
        stage.setMinWidth(root.getMinWidth());
        stage.setMinHeight(root.getMinHeight());
        stage.show();

        controller.setupKeyboardHandler(scene);
        controller.requestFocus();
    }
}
