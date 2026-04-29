package org.penakelex.objectsimulationsystem.ui.dialogs;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;
import org.penakelex.objectsimulationsystem.SimulationApplication;
import org.penakelex.objectsimulationsystem.ui.helpers.MutableHolder;

import java.util.List;
import java.util.ResourceBundle;

public final class DialogUtils {
    private DialogUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static VBox createRootContainer(
        final String... additionalStyleClasses
    ) {
        final var container = new VBox();
        container.getStyleClass().add("dialog-pane");

        if (additionalStyleClasses != null) {
            for (final var styleClass : additionalStyleClasses) {
                if (styleClass != null && !styleClass.isEmpty()) {
                    container.getStyleClass().add(styleClass);
                }
            }
        }

        return container;
    }

    public static HBox createHeaderContainer(
        final ResourceBundle resources,
        final String titleKey,
        final Stage dialogStage,
        final String iconLiteral
    ) {
        final var headerContainer = new HBox();
        headerContainer.getStyleClass().add("dialog-header");

        if (iconLiteral != null && !iconLiteral.isEmpty()) {
            final var icon = new FontIcon(iconLiteral);
            icon.getStyleClass().add("dialog-header-icon");
            headerContainer.getChildren().add(icon);
        }

        final var titleLabel =
            new Label(resources.getString(titleKey));
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

    public static HBox createHeaderContainer(
        final String headerText,
        final Stage dialogStage,
        final String iconLiteral
    ) {
        final var headerContainer = new HBox();
        headerContainer.getStyleClass().add("dialog-header");

        if (iconLiteral != null && !iconLiteral.isEmpty()) {
            final var icon = new FontIcon(iconLiteral);
            icon.getStyleClass().add("dialog-header-icon");
            headerContainer.getChildren().add(icon);
        }

        final var titleLabel = new Label(headerText);
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

    public static VBox createTextContentContainer(
        final String message,
        final String styleClass
    ) {
        final var contentContainer = new VBox();
        contentContainer.getStyleClass().add("dialog-content");

        final var messageLabel = new Label(message);
        messageLabel.getStyleClass().add(styleClass);
        messageLabel.setWrapText(true);
        messageLabel.setFocusTraversable(false);

        contentContainer.getChildren().add(messageLabel);
        return contentContainer;
    }

    public static HBox createSimpleButtonBar(
        final ResourceBundle resources,
        final Stage dialogStage
    ) {
        return createButtonBar(
            resources,
            List.of(new ButtonConfig(
                "dialog.button.ok",
                null,
                _ -> dialogStage.close(),
                true
            ))
        );
    }

    public static HBox createCloseButtonBar(
        final ResourceBundle resources,
        final Stage dialogStage
    ) {
        return createButtonBar(
            resources,
            List.of(new ButtonConfig(
                "dialog.button.close",
                null,
                _ -> dialogStage.close(),
                true
            ))
        );
    }

    public static HBox createConfirmCancelButtonBar(
        final ResourceBundle resources,
        final MutableHolder<Boolean> resultHolder,
        final Stage dialogStage
    ) {
        return createButtonBar(
            resources,
            List.of(
                new ButtonConfig(
                    "dialog.button.cancel",
                    "cancel",
                    _ -> dialogStage.close(),
                    false
                ),
                new ButtonConfig(
                    "dialog.button.ok",
                    null,
                    _ -> {
                        if (resultHolder != null) {
                            resultHolder.setContainedValue(true);
                        }

                        dialogStage.close();
                    },
                    true
                )
            )
        );
    }

    public record ButtonConfig(
        String textKey,
        String styleClass,
        EventHandler<ActionEvent> action,
        boolean defaultButton
    )
    {
    }

    public static HBox createButtonBar(
        final ResourceBundle resources,
        final List<ButtonConfig> buttons
    ) {
        final var buttonBarContainer = new HBox();
        buttonBarContainer.getStyleClass().add("dialog-button-bar");

        for (final var config : buttons) {
            final var button =
                new Button(resources.getString(config.textKey()));

            if (config.styleClass() != null) {
                button.getStyleClass().add(config.styleClass());
            }

            button.setOnAction(config.action());
            button.setDefaultButton(config.defaultButton());
            buttonBarContainer.getChildren().add(button);
        }

        return buttonBarContainer;
    }

    public static void setupWindowDragging(
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

    public static void applyStylesheets(
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

    public static void registerEscapeHandler(
        final Scene dialogScene,
        final Stage dialogStage
    ) {
        dialogScene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                dialogStage.close();
            }
        });
    }
}