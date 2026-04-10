package org.penakelex.objectsimulationsystem.ui;

import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.kordamp.ikonli.javafx.FontIcon;
import org.penakelex.objectsimulationsystem.SimulationApplication;
import org.penakelex.objectsimulationsystem.ui.helpers.MutableHolder;

import java.util.ResourceBundle;

public final class AboutDialog {
    private AboutDialog() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void showAboutDialog(
        final Stage ownerStage,
        final ResourceBundle localizedResources
    ) {
        final var dialogStage = new Stage();

        dialogStage.initOwner(ownerStage);
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initStyle(StageStyle.TRANSPARENT);

        final var rootContainer = createRootContainer();
        rootContainer.getChildren().addAll(
            createHeaderContainer(localizedResources, dialogStage),
            createAboutContentContainer(localizedResources),
            createButtonBarContainer(localizedResources, dialogStage)
        );

        final var dialogScene = new Scene(rootContainer);
        applyStylesheets(dialogScene, ownerStage);

        dialogScene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                dialogStage.close();
            }
        });

        dialogStage.setScene(dialogScene);
        dialogStage.sizeToScene();
        dialogStage.centerOnScreen();
        dialogStage.showAndWait();
    }

    private static VBox createRootContainer() {
        final var container = new VBox();
        container.getStyleClass().addAll("dialog-pane");
        return container;
    }

    private static HBox createHeaderContainer(
        final ResourceBundle localizedResources,
        final Stage dialogStage
    ) {
        final var headerContainer = new HBox();
        headerContainer.getStyleClass().add("dialog-header");

        final var titleLabel = new Label(
            localizedResources.getString("application.title")
        );
        titleLabel.getStyleClass().add("dialog-header-label");

        final var spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        final var closeButton = new Button();
        closeButton.getStyleClass().add("dialog-close-button");
        closeButton.setGraphic(new FontIcon("fas-times"));
        closeButton.setOnAction(_ -> dialogStage.close());

        headerContainer.getChildren()
            .addAll(titleLabel, spacer, closeButton);
        setupWindowDragging(headerContainer, dialogStage);

        return headerContainer;
    }

    private static void setupWindowDragging(
        final HBox draggableHeader,
        final Stage targetStage
    ) {
        final var dragCoordinates = new MutableHolder<>(Point2D.ZERO);

        draggableHeader.setOnMousePressed(event ->
            dragCoordinates.setContainedValue(
                new Point2D(event.getSceneX(), event.getSceneY())
            )
        );

        draggableHeader.setOnMouseDragged(event -> {
            final var initialPoint =
                dragCoordinates.getContainedValue();
            targetStage.setX(
                event.getScreenX() - initialPoint.getX());
            targetStage.setY(
                event.getScreenY() - initialPoint.getY());
        });
    }

    private static VBox createAboutContentContainer(
        final ResourceBundle localizedResources
    ) {
        final var contentContainer = new VBox();
        contentContainer.getStyleClass().add("dialog-content");

        final var aboutLabel = new Label(
            localizedResources.getString("label.about")
        );
        aboutLabel.getStyleClass().add("about-dialog-info");
        aboutLabel.setWrapText(true);

        contentContainer.getChildren().add(aboutLabel);
        return contentContainer;
    }

    private static HBox createButtonBarContainer(
        final ResourceBundle localizedResources,
        final Stage dialogStage
    ) {
        final var buttonBarContainer = new HBox();
        buttonBarContainer.getStyleClass().add("dialog-button-bar");

        final var okButton = new Button(
            localizedResources.getString("dialog.button.ok")
        );
        okButton.setDefaultButton(true);
        okButton.setOnAction(_ -> dialogStage.close());

        buttonBarContainer.getChildren().add(okButton);
        return buttonBarContainer;
    }

    private static void applyStylesheets(
        final Scene dialogScene,
        final Stage ownerStage
    ) {
        if (ownerStage != null && ownerStage.getScene() != null) {
            final var ownerStylesheets =
                ownerStage.getScene().getStylesheets();
            if (!ownerStylesheets.isEmpty()) {
                dialogScene.getStylesheets().addAll(ownerStylesheets);
                return;
            }
        }

        final var cssURL = SimulationApplication.class
            .getResource("css/main.css");
        if (cssURL != null) {
            dialogScene.getStylesheets().add(cssURL.toExternalForm());
        }
    }
}