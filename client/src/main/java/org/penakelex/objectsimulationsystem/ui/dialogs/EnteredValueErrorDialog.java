package org.penakelex.objectsimulationsystem.ui.dialogs;

import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.ResourceBundle;

public final class EnteredValueErrorDialog {
    private EnteredValueErrorDialog() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void showError(
        final Stage ownerStage,
        final ResourceBundle resources,
        final String errorMessage
    ) {
        final var dialogStage = new Stage();
        dialogStage.initOwner(ownerStage);
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initStyle(StageStyle.TRANSPARENT);

        final var root = DialogUtils.createRootContainer(
            "entered-value-error-dialog-pane"
        );
        root.getChildren().addAll(
            DialogUtils.createHeaderContainer(
                resources,
                "error.title",
                dialogStage,
                "fas-exclamation-circle"
            ),
            DialogUtils.createTextContentContainer(
                errorMessage,
                "error-dialog-message"
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