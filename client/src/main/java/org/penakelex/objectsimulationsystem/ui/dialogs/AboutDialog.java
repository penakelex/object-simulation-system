package org.penakelex.objectsimulationsystem.ui.dialogs;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.ResourceBundle;

public final class AboutDialog {
    private AboutDialog() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void showAboutDialog(
        final Stage ownerStage,
        final ResourceBundle resources
    ) {
        final var dialogStage = new Stage();
        dialogStage.initOwner(ownerStage);
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initStyle(StageStyle.TRANSPARENT);

        final var root =
            DialogUtils.createRootContainer("about-dialog-pane");
        root.getChildren().addAll(
            DialogUtils.createHeaderContainer(
                resources,
                "application.title",
                dialogStage,
                "fas-car-side"
            ),
            createAboutContentContainer(resources),
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
}