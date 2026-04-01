package org.penakelex.objectsimulationsystem.ui;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.penakelex.objectsimulationsystem.model.collection.VehicleCollection;

import java.util.Comparator;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public final class CurrentObjectsDialog {
    private CurrentObjectsDialog() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void showDialog(
        final Stage owner,
        final VehicleCollection vehicleCollection,
        final ResourceBundle resources
    ) {
        final var alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(owner);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.setTitle(resources.getString(
            "dialog.current.objects.title"
        ));
        alert.setHeaderText(resources.getString(
            "dialog.current.objects.header"
        ));

        final var listView = new ListView<VehicleInfo>();
        listView.setEditable(false);
        listView.getStyleClass().add("dialog-list-view");

        listView.setCellFactory(_ -> new VehicleInfoCell(resources));

        updateListView(listView, vehicleCollection);

        final var updateTimer = new AnimationTimer() {
            private static final long UPDATE_INTERVAL_NANOS =
                50_000_000;
            private long lastUpdateTime = 0;

            @Override
            public void handle(final long now) {
                if (now - lastUpdateTime >= UPDATE_INTERVAL_NANOS) {
                    Platform.runLater(() ->
                        updateListView(listView, vehicleCollection)
                    );
                    lastUpdateTime = now;
                }
            }
        };

        alert.setOnShown(_ -> updateTimer.start());
        alert.setOnHidden(_ -> updateTimer.stop());

        final var content = new VBox(listView);
        content.getStyleClass().add("dialog-content");

        alert.getDialogPane().setContent(content);
        alert.getDialogPane().getStyleClass().addAll(
            "dialog-pane",
            "current-objects-dialog-pane"
        );

        final var closeButtonType = new ButtonType(
            resources.getString("dialog.button.close"),
            ButtonBar.ButtonData.CANCEL_CLOSE
        );
        alert.getButtonTypes().setAll(closeButtonType);

        alert.showAndWait();
    }

    private static void updateListView(
        final ListView<VehicleInfo> listView,
        final VehicleCollection vehicleCollection
    ) {
        final var items = vehicleCollection
            .getSpawnTimesSnapshot()
            .entrySet()
            .stream()
            .sorted(Map.Entry.comparingByKey())
            .map(vehicle -> new VehicleInfo(
                vehicle.getKey(),
                vehicle.getValue()
            ))
            .sorted(Comparator.comparingInt(VehicleInfo::id))
            .collect(Collectors.toCollection(
                FXCollections::observableArrayList
            ));

        listView.setItems(items);
    }

    private record VehicleInfo(
        int id,
        long spawnTime
    )
    {
    }

    private static final class VehicleInfoCell
        extends ListCell<VehicleInfo>
    {
        private final ResourceBundle resources;
        private final VBox container;
        private final Label idLabel;
        private final Label spawnTimeLabel;

        private VehicleInfoCell(final ResourceBundle resources) {
            this.resources = resources;

            container = new VBox();
            container.getStyleClass().add("vehicle-info-cell");

            idLabel = new Label();
            idLabel.getStyleClass().add("vehicle-id");

            spawnTimeLabel = new Label();
            spawnTimeLabel.getStyleClass().add("vehicle-spawn-time");

            container.getChildren().addAll(idLabel, spawnTimeLabel);
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
                idLabel.setText(resources
                    .getString("dialog.label.id")
                    .formatted(info.id())
                );
                spawnTimeLabel.setText(resources
                    .getString("dialog.label.spawn.time")
                    .formatted(TimeFormatter
                        .formatTime(info.spawnTime, resources)
                    )
                );

                setGraphic(container);
            }
        }
    }
}