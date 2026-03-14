package org.penakelex.objectsimulationsystem;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.penakelex.objectsimulationsystem.controller.SimulationController;
import org.penakelex.objectsimulationsystem.habitat.Configuration;
import org.penakelex.objectsimulationsystem.vehicle.images.CarImages;
import org.penakelex.objectsimulationsystem.vehicle.images.TruckImages;

import java.io.IOException;
import java.util.Objects;
import java.util.ResourceBundle;

import static org.penakelex.objectsimulationsystem.ui.ErrorDialog.getErrorString;
import static org.penakelex.objectsimulationsystem.ui.ErrorDialog.showErrorAndExit;

public class SimulationApplication extends Application {
    @Override
    public void start(final Stage stage) {
        final ResourceBundle resources;

        try {
            resources = ResourceBundle.getBundle("messages");
        } catch (final Exception exception) {
            showErrorAndExit(
                null,
                getErrorString(
                    null,
                    "error.resources.not_found"
                ),
                exception.getLocalizedMessage()
            );
            return;
        }

        final TruckImages truckImages;
        final CarImages carImages;

        try {
            truckImages = new TruckImages();
            carImages = new CarImages();
        } catch (final Exception exception) {
            showErrorAndExit(
                resources,
                getErrorString(
                    resources,
                    "error.images.not_found",
                    Configuration.VEHICLE_IMAGES_BASE_PATH,
                    exception.getLocalizedMessage()
                ),
                null
            );
            return;
        }

        final var loader = new FXMLLoader(
            SimulationApplication.class
                .getResource("simulation-view.fxml"),
            resources
        );

        final HBox root;

        try {
            root = loader.load();
        } catch (final IOException _) {
            showErrorAndExit(
                resources,
                getErrorString(resources, "error.fxml.not_found"),
                null
            );
            return;
        }

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
        controller.initializeHabitatImages(truckImages, carImages);

        stage.setTitle(resources.getString("application.title"));
        stage.setScene(scene);
        stage.setMinWidth(root.getMinWidth());
        stage.setMinHeight(root.getMinHeight());
        stage.show();

        controller.setupKeyboardHandler(scene);
    }
}
