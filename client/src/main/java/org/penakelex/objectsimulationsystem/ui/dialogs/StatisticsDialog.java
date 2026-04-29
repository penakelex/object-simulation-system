package org.penakelex.objectsimulationsystem.ui.dialogs;

import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.penakelex.objectsimulationsystem.model.habitat.VehicleStatistics;
import org.penakelex.objectsimulationsystem.ui.helpers.MutableHolder;
import org.penakelex.objectsimulationsystem.ui.helpers.TimeFormatter;

import java.util.ResourceBundle;

public final class StatisticsDialog {
    private StatisticsDialog() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static boolean showStatisticsDialog(
        final Stage ownerStage,
        final VehicleStatistics statistics,
        final long elapsedTimeInMillis,
        final ResourceBundle resources
    ) {
        final var result = new MutableHolder<>(false);
        final var dialogStage = new Stage();
        dialogStage.initOwner(ownerStage);
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initStyle(StageStyle.TRANSPARENT);

        final var root =
            DialogUtils.createRootContainer("statistics-dialog-pane");
        root.getChildren().addAll(
            DialogUtils.createHeaderContainer(
                resources,
                "dialog.statistics.header",
                dialogStage,
                "fas-chart-bar"
            ),
            createStatisticsContentContainer(
                statistics,
                elapsedTimeInMillis,
                resources
            ),
            DialogUtils.createConfirmCancelButtonBar(
                resources,
                result,
                dialogStage
            )
        );

        final var scene = new Scene(root);
        DialogUtils.applyStylesheets(scene, ownerStage);
        DialogUtils.registerEscapeHandler(scene, dialogStage);

        dialogStage.setScene(scene);
        dialogStage.sizeToScene();
        dialogStage.centerOnScreen();
        dialogStage.showAndWait();

        return result.getContainedValue();
    }

    private static VBox createStatisticsContentContainer(
        final VehicleStatistics statistics,
        final long elapsedTimeInMillis,
        final ResourceBundle resources
    ) {
        final var contentContainer = new VBox();
        contentContainer.getStyleClass().add("dialog-content");

        final var statisticsTextArea = new TextArea();
        statisticsTextArea.setEditable(false);
        statisticsTextArea.setWrapText(true);
        statisticsTextArea.setFocusTraversable(false);
        statisticsTextArea.getStyleClass().add("dialog-text-area");

        final var formattedContent = resources
            .getString("dialog.statistics.content")
            .formatted(
                statistics.trucks(),
                statistics.cars(),
                statistics.total(),
                TimeFormatter.formatTime(
                    elapsedTimeInMillis,
                    resources
                )
            );
        statisticsTextArea.setText(formattedContent);

        contentContainer.getChildren().add(statisticsTextArea);
        return contentContainer;
    }
}