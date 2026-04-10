package org.penakelex.objectsimulationsystem.ui;

import javafx.animation.AnimationTimer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.kordamp.ikonli.javafx.FontIcon;
import org.penakelex.objectsimulationsystem.model.collection.VehicleCollection;
import org.penakelex.objectsimulationsystem.ui.helpers.MutableHolder;

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
        final ResourceBundle localizedResources
    ) {
        final var dialogStage = new Stage();

        dialogStage.initOwner(ownerStage);
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initStyle(StageStyle.TRANSPARENT);

        final var rootContainer = createRootContainer();

        final var vehicleInfoList =
            FXCollections.<VehicleInfo>observableArrayList();

        final var objectsListView = new ListView<>(vehicleInfoList);
        objectsListView.getStyleClass().add("dialog-list-view");
        objectsListView.setCellFactory(_ ->
            new VehicleInfoCell(localizedResources)
        );

        final var emptyListLabel = new Label(localizedResources
            .getString("dialog.current.objects.empty")
        );
        emptyListLabel.getStyleClass().add("dialog-list-empty-label");
        objectsListView.setPlaceholder(emptyListLabel);

        rootContainer.getChildren().addAll(
            createHeaderContainer(localizedResources, dialogStage),
            createListContentContainer(objectsListView),
            createButtonBarContainer(localizedResources, dialogStage)
        );

        final var dialogScene = new Scene(rootContainer);
        dialogScene.getStylesheets()
            .addAll(ownerStage.getScene().getStylesheets());

        dialogScene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                dialogStage.close();
            }
        });

        dialogStage.setScene(dialogScene);
        dialogStage.sizeToScene();
        dialogStage.centerOnScreen();

        setupLiveUpdateTimer(
            dialogStage,
            vehicleInfoList,
            vehicleCollection
        );
        dialogStage.showAndWait();
    }

    private static VBox createRootContainer() {
        final var container = new VBox();
        container.getStyleClass()
            .addAll("dialog-pane", "current-objects-dialog-pane");
        return container;
    }

    private static HBox createHeaderContainer(
        final ResourceBundle localizedResources,
        final Stage dialogStage
    ) {
        final var headerContainer = new HBox();
        headerContainer.getStyleClass().add("dialog-header");

        final var titleLabel = new Label(localizedResources
            .getString("dialog.current.objects.header")
        );
        titleLabel.getStyleClass().add("dialog-header-label");

        final var spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        final var closeButton = new Button();
        closeButton.getStyleClass().add("dialog-close-button");
        closeButton.setGraphic(new FontIcon("fas-times"));
        closeButton.setOnAction(_ -> dialogStage.close());

        headerContainer.getChildren()
            .addAll(titleLabel, spacer, closeButton);
        setupWindowDragging(headerContainer, dialogStage);

        return headerContainer;
    }

    private static void setupWindowDragging(
        final HBox draggableHeader,
        final Stage targetStage
    ) {
        final var dragCoordinates = new MutableHolder<>(Point2D.ZERO);

        draggableHeader.setOnMousePressed(event ->
            dragCoordinates.setContainedValue(
                new Point2D(event.getSceneX(), event.getSceneY())
            )
        );

        draggableHeader.setOnMouseDragged(event -> {
            final var initialPoint =
                dragCoordinates.getContainedValue();
            targetStage.setX(
                event.getScreenX() - initialPoint.getX()
            );
            targetStage.setY(
                event.getScreenY() - initialPoint.getY()
            );
        });
    }

    private static VBox createListContentContainer(final ListView<VehicleInfo> listView) {
        final var contentContainer = new VBox();
        contentContainer.getStyleClass().add("dialog-content");
        VBox.setVgrow(listView, Priority.ALWAYS);
        contentContainer.getChildren().add(listView);
        return contentContainer;
    }

    private static HBox createButtonBarContainer(
        final ResourceBundle localizedResources,
        final Stage dialogStage
    ) {
        final var buttonBarContainer = new HBox();
        buttonBarContainer.getStyleClass().add("dialog-button-bar");

        final var closeTextButton = new Button(localizedResources
            .getString("dialog.button.close")
        );
        closeTextButton.setDefaultButton(true);
        closeTextButton.setOnAction(_ -> dialogStage.close());

        buttonBarContainer.getChildren().add(closeTextButton);
        return buttonBarContainer;
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