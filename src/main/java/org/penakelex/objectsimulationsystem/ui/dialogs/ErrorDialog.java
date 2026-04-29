package org.penakelex.objectsimulationsystem.ui.dialogs;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public final class ErrorDialog {
    private ErrorDialog() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void showError(
        final Stage ownerStage,
        final ResourceBundle localizedResources,
        final String contentMessage,
        final String headerMessage
    ) {
        showErrorAndOptionalExit(ownerStage,
            localizedResources,
            contentMessage,
            headerMessage,
            false
        );
    }

    public static void showErrorAndExit(
        final Stage ownerStage,
        final ResourceBundle localizedResources,
        final String contentMessage,
        final String headerMessage
    ) {
        showErrorAndOptionalExit(ownerStage,
            localizedResources,
            contentMessage,
            headerMessage,
            true
        );
    }

    private static void showErrorAndOptionalExit(
        final Stage ownerStage,
        final ResourceBundle resources,
        final String content,
        final String header,
        final boolean exitOnClose
    ) {
        final var dialogStage = new Stage();
        dialogStage.initOwner(ownerStage);
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initStyle(StageStyle.TRANSPARENT);

        final var effectiveHeader = header != null
            ? header
            : getErrorString(resources, "error.header");
        final var root = DialogUtils.createRootContainer(
            "error-dialog-pane"
        );
        root.getChildren().addAll(
            DialogUtils.createHeaderContainer(
                effectiveHeader,
                dialogStage,
                "fas-exclamation-circle"
            ),
            DialogUtils.createTextContentContainer(
                content,
                "error-dialog-message"
            ),
            DialogUtils.createSimpleButtonBar(resources, dialogStage)
        );

        final var scene = new Scene(root);
        DialogUtils.applyStylesheets(scene, ownerStage);
        DialogUtils.registerEscapeHandler(scene, dialogStage);

        if (exitOnClose) {
            dialogStage.setOnHidden(_ -> Platform.exit());
        }

        dialogStage.setScene(scene);
        dialogStage.sizeToScene();
        dialogStage.centerOnScreen();
        dialogStage.showAndWait();
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