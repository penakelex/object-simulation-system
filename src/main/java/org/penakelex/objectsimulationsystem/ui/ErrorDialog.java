package org.penakelex.objectsimulationsystem.ui;

import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public final class ErrorDialog {
    private ErrorDialog() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void showErrorAndExit(
        final ResourceBundle resources,
        final String contentText,
        final String headerText
    ) {
        final var alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(getErrorString(resources, "error.title"));
        alert.setHeaderText(
            headerText != null
                ? headerText
                : getErrorString(resources, "error.header")
        );
        alert.setContentText(contentText);
        alert.showAndWait();
        Platform.exit();
    }

    public static String getErrorString(
        final ResourceBundle resources,
        final String key
    ) {
        try {
            return resources.getString(key);
        } catch (final Exception _) {
            return String.format("Error: %s", key);
        }
    }

    public static String getErrorString(
        final ResourceBundle resources,
        final String key,
        final Object... args
    ) {
        try {
            return MessageFormat.format(
                resources.getString(key),
                args
            );
        } catch (final Exception _) {
            return String.format("Error: %s [%s]",
                key,
                String.join(", ",
                    Arrays.stream(args)
                        .map(String::valueOf)
                        .collect(Collectors.joining(", "))
                )
            );
        }
    }
}
