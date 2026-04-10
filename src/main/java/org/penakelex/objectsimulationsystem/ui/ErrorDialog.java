package org.penakelex.objectsimulationsystem.ui;

import javafx.application.Platform;
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

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public final class ErrorDialog {
    private ErrorDialog() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void showErrorAndExit(
        final Stage ownerStage,
        final ResourceBundle localizedResources,
        final String contentMessage,
        final String headerMessage
    ) {
        final var dialogStage = new Stage();

        dialogStage.initOwner(ownerStage);
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initStyle(StageStyle.TRANSPARENT);

        final var rootContainer = createRootContainer();
        rootContainer.getChildren().addAll(
            createHeaderContainer(
                localizedResources,
                headerMessage,
                dialogStage
            ),
            createErrorContentContainer(contentMessage),
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

        dialogStage.setOnHidden(_ -> Platform.exit());
        dialogStage.showAndWait();
    }

    private static VBox createRootContainer() {
        final var container = new VBox();
        container.getStyleClass().addAll("dialog-pane");
        return container;
    }

    private static HBox createHeaderContainer(
        final ResourceBundle localizedResources,
        final String headerMessage,
        final Stage dialogStage
    ) {
        final var headerContainer = new HBox();
        headerContainer.getStyleClass().add("dialog-header");

        final var titleLabel = new Label(
            headerMessage != null
                ? headerMessage
                : getErrorString(localizedResources, "error.header")
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
                event.getScreenX() - initialPoint.getX()
            );
            targetStage.setY(
                event.getScreenY() - initialPoint.getY()
            );
        });
    }

    private static VBox createErrorContentContainer(final String errorMessage) {
        final var contentContainer = new VBox();
        contentContainer.getStyleClass().add("dialog-content");

        final var errorLabel = new Label(errorMessage);
        errorLabel.getStyleClass().add("error-dialog-message");
        errorLabel.setWrapText(true);

        contentContainer.getChildren().add(errorLabel);
        return contentContainer;
    }

    private static HBox createButtonBarContainer(
        final ResourceBundle localizedResources,
        final Stage dialogStage
    ) {
        final var buttonBarContainer = new HBox();
        buttonBarContainer.getStyleClass().add("dialog-button-bar");

        final var okButton = new Button(getErrorString(
            localizedResources,
            "dialog.button.ok"
        ));
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

    public static String getErrorString(
        final ResourceBundle localizedResources,
        final String resourceKey
    ) {
        if (localizedResources != null) {
            try {
                return localizedResources.getString(resourceKey);
            } catch (final Exception _) {
            }
        }

        return String.format("Error: %s", resourceKey);
    }

    public static String getErrorString(
        final ResourceBundle localizedResources,
        final String resourceKey,
        final Object... formatArguments
    ) {
        if (localizedResources != null) {
            try {
                return MessageFormat.format(
                    localizedResources.getString(resourceKey),
                    formatArguments
                );
            } catch (final Exception _) {
            }
        }

        return String.format("Error: %s [%s]",
            resourceKey,
            Arrays.stream(formatArguments)
                .map(String::valueOf)
                .collect(Collectors.joining(", "))
        );
    }
}