package org.penakelex.objectsimulationsystem.ui.dialogs;

import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.ResourceBundle;

public final class WarningDialog {
    private WarningDialog() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void showWarning(
        final Stage ownerStage,
        final ResourceBundle resources,
        final String warningMessage
    ) {
        final var dialogStage = new Stage();
        dialogStage.initOwner(ownerStage);
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initStyle(StageStyle.TRANSPARENT);

        final var root =
            DialogUtils.createRootContainer("warning-dialog-pane");
        root.getChildren().addAll(
            DialogUtils.createHeaderContainer(
                resources,
                "warning.title",
                dialogStage,
                "fas-exclamation-triangle"
            ),
            DialogUtils.createTextContentContainer(
                warningMessage,
                "warning-dialog-message"
            ),
            DialogUtils.createSimpleButtonBar(resources, dialogStage)
        );

        final var scene = new Scene(root);
        DialogUtils.applyStylesheets(scene, ownerStage);
        DialogUtils.registerEscapeHandler(scene, dialogStage);

        dialogStage.setScene(scene);
        dialogStage.sizeToScene();
        dialogStage.centerOnScreen();
        dialogStage.showAndWait();
    }
}