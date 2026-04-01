package org.penakelex.objectsimulationsystem.ui;

import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.penakelex.objectsimulationsystem.model.habitat.VehicleStatistics;

import java.util.ResourceBundle;

public final class StatisticsDialog {
    private StatisticsDialog() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static boolean showStatisticsDialog(
        final Stage owner,
        final VehicleStatistics statistics,
        final long elapsedTime,
        final ResourceBundle resources
    ) {
        final var alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(owner);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.setTitle(resources.getString(
            "dialog.statistics.title"
        ));
        alert.setHeaderText(resources.getString(
            "dialog.statistics.header"
        ));

        final var content = new VBox();
        content.getStyleClass().add("dialog-content");

        final var textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.getStyleClass().add("dialog-text-area");

        final var text = resources
            .getString("dialog.statistics.content")
            .formatted(
                statistics.trucks(),
                statistics.cars(),
                statistics.total(),
                TimeFormatter.formatTime(elapsedTime, resources)
            );

        final var lines = text.lines().count();
        final var maxLineLength = text.lines()
            .map(String::length)
            .max(Integer::compareTo)
            .orElse(0);

        textArea.setPrefRowCount((int) lines);
        textArea.setPrefColumnCount(maxLineLength);
        textArea.setText(text);

        content.getChildren().add(textArea);
        final var alertDialogPane = alert.getDialogPane();
        alertDialogPane.setContent(content);
        alertDialogPane.getStyleClass().add("dialog-pane");
        alertDialogPane.getStyleClass()
            .add("statistics-dialog-pane");

        final var okButtonType = new ButtonType(
            resources.getString("dialog.button.ok"),
            ButtonBar.ButtonData.OK_DONE
        );
        final var cancelButtonType = new ButtonType(
            resources.getString("dialog.button.cancel"),
            ButtonBar.ButtonData.CANCEL_CLOSE
        );
        alert.getButtonTypes().setAll(okButtonType, cancelButtonType);

        final var result = alert.showAndWait();
        return result
            .filter(type -> type == okButtonType)
            .isPresent();
    }
}