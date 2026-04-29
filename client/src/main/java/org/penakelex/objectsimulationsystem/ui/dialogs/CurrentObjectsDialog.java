package org.penakelex.objectsimulationsystem.ui.dialogs;

import javafx.animation.AnimationTimer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.penakelex.objectsimulationsystem.model.collection.VehicleCollection;
import org.penakelex.objectsimulationsystem.ui.helpers.TimeFormatter;

import java.util.Map;
import java.util.ResourceBundle;

public final class CurrentObjectsDialog {
    private CurrentObjectsDialog() {
        throw new UnsupportedOperationException("Utility class");
    }

    private record VehicleInfo(int id, long spawnTime) {
    }

    public static void showDialog(
        final Stage ownerStage,
        final VehicleCollection vehicleCollection,
        final ResourceBundle resources
    ) {
        final var dialogStage = new Stage();
        dialogStage.initOwner(ownerStage);
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initStyle(StageStyle.TRANSPARENT);

        final var vehicleInfoList =
            FXCollections.<VehicleInfo>observableArrayList();
        final var listView = new ListView<>(vehicleInfoList);
        listView.getStyleClass().add("dialog-list-view");
        listView.setCellFactory(_ ->
            new VehicleInfoCell(resources)
        );

        final var emptyLabel = new Label(resources.getString(
            "dialog.current.objects.empty"
        ));
        emptyLabel.getStyleClass().add("dialog-list-empty-label");
        listView.setPlaceholder(emptyLabel);

        final var root = DialogUtils.createRootContainer(
            "current-objects-dialog-pane"
        );
        root.getChildren().addAll(
            DialogUtils.createHeaderContainer(
                resources,
                "dialog.current.objects.header",
                dialogStage,
                "fas-list"
            ),
            createListContentContainer(listView),
            DialogUtils.createCloseButtonBar(resources, dialogStage)
        );

        final var scene = new Scene(root);
        DialogUtils.applyStylesheets(scene, ownerStage);
        DialogUtils.registerEscapeHandler(scene, dialogStage);

        dialogStage.setScene(scene);
        dialogStage.sizeToScene();
        dialogStage.centerOnScreen();

        setupLiveUpdateTimer(
            dialogStage,
            vehicleInfoList,
            vehicleCollection
        );
        dialogStage.showAndWait();
    }

    private static VBox createListContentContainer(
        final ListView<VehicleInfo> listView
    ) {
        final var contentContainer = new VBox();
        contentContainer.getStyleClass().add("dialog-content");
        VBox.setVgrow(listView, Priority.ALWAYS);
        contentContainer.getChildren().add(listView);
        return contentContainer;
    }

    private static void setupLiveUpdateTimer(
        final Stage dialogStage,
        final ObservableList<VehicleInfo> targetList,
        final VehicleCollection vehicleCollection
    ) {
        final var updateTimer = new AnimationTimer() {
            private static final long UPDATE_INTERVAL_NANOS =
                50_000_000;
            private long lastUpdateTime = 0;

            @Override
            public void handle(final long currentTimeNanos) {
                if (currentTimeNanos - lastUpdateTime >=
                    UPDATE_INTERVAL_NANOS
                ) {
                    updateListViewContent(
                        targetList,
                        vehicleCollection
                    );
                    lastUpdateTime = currentTimeNanos;
                }
            }
        };

        dialogStage.setOnShown(_ -> updateTimer.start());
        dialogStage.setOnHidden(_ -> updateTimer.stop());
    }

    private static void updateListViewContent(
        final ObservableList<VehicleInfo> targetList,
        final VehicleCollection vehicleCollection
    ) {
        targetList.setAll(vehicleCollection
            .getSpawnTimesSnapshot()
            .entrySet()
            .stream()
            .sorted(Map.Entry.comparingByKey())
            .map(entry ->
                new VehicleInfo(entry.getKey(), entry.getValue())
            )
            .toList()
        );
    }

    private static final class VehicleInfoCell
        extends ListCell<VehicleInfo>
    {
        private final ResourceBundle resources;
        private final VBox container;
        private final Label identifierLabel;
        private final Label spawnTimeLabel;

        private VehicleInfoCell(final ResourceBundle resources) {
            this.resources = resources;

            container = new VBox();
            container.getStyleClass().add("vehicle-info-cell");

            identifierLabel = new Label();
            identifierLabel.getStyleClass().add("vehicle-id");

            spawnTimeLabel = new Label();
            spawnTimeLabel.getStyleClass().add("vehicle-spawn-time");

            container.getChildren()
                .addAll(identifierLabel, spawnTimeLabel);

            setText(null);
        }

        @Override
        protected void updateItem(
            final VehicleInfo info,
            final boolean empty
        ) {
            super.updateItem(info, empty);

            if (empty || info == null) {
                setGraphic(null);
            } else {
                identifierLabel.setText(resources
                    .getString("dialog.label.id")
                    .formatted(info.id())
                );
                spawnTimeLabel.setText(resources
                    .getString("dialog.label.spawn.time")
                    .formatted(TimeFormatter.formatTime(
                        info.spawnTime(),
                        resources
                    ))
                );
                setGraphic(container);
            }
        }
    }
}