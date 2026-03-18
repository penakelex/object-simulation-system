package org.penakelex.objectsimulationsystem.ui;

import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.penakelex.objectsimulationsystem.habitat.VehicleStatistics;

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
        alert.setTitle(
            resources.getString("dialog.statistics.title")
        );
        alert.setHeaderText(
            resources.getString("dialog.statistics.header")
        );

        final var content = new VBox(10);
        content.setStyle("-fx-padding: 10;");

        final var textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setStyle(
            "-fx-font-size: 14px; -fx-font-family: 'Consolas', monospace;");

        final var timeFormatted = resources
            .getString("format.time.milliseconds")
            .formatted(elapsedTime);

        final var text =
            resources.getString("dialog.statistics.content")
                .formatted(
                    statistics.trucks(),
                    statistics.cars(),
                    statistics.total(),
                    timeFormatted
                );

        textArea.setPrefRowCount((int) text.lines().count());
        textArea.setPrefColumnCount(text
            .lines()
            .map(String::length)
            .max(Integer::compareTo)
            .orElse(0)
        );

        content.getChildren().add(textArea);
        alert.getDialogPane().setContent(content);

        final var okButton = new ButtonType(
            resources.getString("dialog.button.ok"),
            ButtonBar.ButtonData.OK_DONE
        );
        final var cancelButton = new ButtonType(
            resources.getString("dialog.button.cancel"),
            ButtonBar.ButtonData.CANCEL_CLOSE
        );

        alert.getButtonTypes().setAll(okButton, cancelButton);

        return alert.showAndWait()
            .filter(buttonType -> buttonType == okButton)
            .isPresent();
    }
}