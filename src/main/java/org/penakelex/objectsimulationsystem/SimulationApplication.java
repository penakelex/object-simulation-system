package org.penakelex.objectsimulationsystem;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;
import java.util.ResourceBundle;

public class SimulationApplication extends Application {
    @Override
    public void start(final Stage stage) throws IOException {
        final var resources =
            ResourceBundle.getBundle("messages");

        final var loader = new FXMLLoader(
            SimulationApplication.class
                .getResource("simulation-view.fxml"),
            resources
        );

        final HBox root = loader.load();
        final var scene = new Scene(
            root,
            root.getPrefWidth(),
            root.getPrefHeight()
        );

        scene.getStylesheets().add(
            Objects.requireNonNull(SimulationApplication.class
                    .getResource("css/main.css"))
                .toExternalForm()
        );

        final SimulationController controller =
            loader.getController();

        stage.setTitle(resources.getString("application.title"));
        stage.setScene(scene);
        stage.setMinWidth(root.getMinWidth());
        stage.setMinHeight(root.getMinHeight());
        stage.show();

        controller.setupKeyboardHandler(scene);
    }
}
