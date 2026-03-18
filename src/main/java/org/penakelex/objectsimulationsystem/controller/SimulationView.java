package org.penakelex.objectsimulationsystem.controller;

import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;
import org.penakelex.objectsimulationsystem.habitat.Habitat;
import org.penakelex.objectsimulationsystem.habitat.VehicleStatistics;
import org.penakelex.objectsimulationsystem.ui.LabeledValueRow;

import java.util.ResourceBundle;

public final class SimulationView {

    private final Canvas simulationCanvas;
    private final GraphicsContext graphicsContext;

    private final LabeledValueRow truckRow, carRow, totalRow;
    private final LabeledValueRow overlayTruckRow, overlayCarRow,
        overlayTotalRow;

    private final StackPane statusTimeContainer;
    private final LabeledValueRow statusTimeRow;

    private final Label overlayTimeLabel;
    private final Label statusLabel;
    private final FontIcon statusIcon;

    private final VBox infoContainer, statisticsOverlay;

    private final ResourceBundle resources;

    public SimulationView(
        final Canvas simulationCanvas,
        final StackPane simulationField,
        final LabeledValueRow truckRow,
        final LabeledValueRow carRow,
        final LabeledValueRow totalRow,
        final LabeledValueRow overlayTruckRow,
        final LabeledValueRow overlayCarRow,
        final LabeledValueRow overlayTotalRow,
        final StackPane statusTimeContainer,
        final LabeledValueRow statusTimeRow,
        final Label overlayTimeLabel,
        final Label statusLabel,
        final FontIcon statusIcon,
        final VBox infoContainer,
        final VBox statisticsOverlay,
        final ResourceBundle resources
    ) {
        this.simulationCanvas = simulationCanvas;
        this.graphicsContext =
            simulationCanvas.getGraphicsContext2D();
        this.truckRow = truckRow;
        this.carRow = carRow;
        this.totalRow = totalRow;
        this.overlayTruckRow = overlayTruckRow;
        this.overlayCarRow = overlayCarRow;
        this.overlayTotalRow = overlayTotalRow;
        this.statusTimeRow = statusTimeRow;
        this.overlayTimeLabel = overlayTimeLabel;
        this.statusLabel = statusLabel;
        this.statusIcon = statusIcon;
        this.infoContainer = infoContainer;
        this.statusTimeContainer = statusTimeContainer;
        this.statisticsOverlay = statisticsOverlay;
        this.resources = resources;

        bindCanvasSize(simulationField);
    }

    private void bindCanvasSize(final StackPane simulationField) {
        simulationCanvas.widthProperty()
            .bind(simulationField.widthProperty());
        simulationCanvas.heightProperty()
            .bind(simulationField.heightProperty());
    }

    public void draw(final Habitat habitat) {
        graphicsContext.clearRect(
            0, 0,
            simulationCanvas.getWidth(),
            simulationCanvas.getHeight()
        );
        habitat.draw(graphicsContext);
    }

    public void updatePanelStatistics
        (
            final VehicleStatistics statistics
        ) {
        updateStatisticsRow(truckRow, carRow, totalRow, statistics);
    }

    public void updateOverlayStatistics(
        final VehicleStatistics statistics
    ) {
        updateStatisticsRow(
            overlayTruckRow,
            overlayCarRow,
            overlayTotalRow,
            statistics
        );
    }

    private void updateStatisticsRow(
        final LabeledValueRow truckRow,
        final LabeledValueRow carRow,
        final LabeledValueRow totalRow,
        final VehicleStatistics statistics
    ) {
        truckRow.setValue(statistics.trucks());
        carRow.setValue(statistics.cars());
        totalRow.setValue(statistics.total());
    }

    public void updateStatusTime(
        final long elapsedTime,
        final boolean showTime
    ) {
        if (showTime) {
            statusTimeRow.setValue(formatTime(elapsedTime));
        }
    }

    public void updateOverlayTime(final long elapsedTime) {
        overlayTimeLabel.setText(formatTime(elapsedTime));
    }

    private String formatTime(final long elapsedTime) {
        return resources
            .getString("format.time.milliseconds")
            .formatted(elapsedTime);
    }

    public void updateStatus(final SimulationState state) {
        statusLabel.setText(resources.getString(state.messageKey));
        statusLabel
            .getStyleClass()
            .setAll("status-value", state.styleClass);
        statusIcon.setIconLiteral(state.iconLiteral);
        statusIcon
            .getStyleClass()
            .setAll("status-icon", state.styleClass);
    }

    public void showInfoPanel() {
        setNodeVisible(infoContainer, true);
        setNodeVisible(statisticsOverlay, false);
    }

    public void showOverlay() {
        setNodeVisible(infoContainer, false);
        setNodeVisible(statisticsOverlay, true);
    }

    public void setStatusTimeVisible(final boolean visible) {
        setNodeVisible(statusTimeContainer, visible);
    }

    public void setNodeVisible(
        final Node node,
        final boolean visible
    ) {
        node.setVisible(visible);
        node.setManaged(visible);
    }

    public void setNodesVisible(
        final boolean visible,
        final Node... nodes
    ) {
        for (final var node : nodes) {
            setNodeVisible(node, visible);
        }
    }
}