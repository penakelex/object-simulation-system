package org.penakelex.objectsimulationsystem.ui.helpers;

import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;
import org.penakelex.objectsimulationsystem.model.habitat.Habitat;
import org.penakelex.objectsimulationsystem.model.habitat.VehicleStatistics;
import org.penakelex.objectsimulationsystem.ui.components.LabeledValueRow;
import org.penakelex.objectsimulationsystem.ui.TimeFormatter;
import org.penakelex.objectsimulationsystem.viewmodel.SimulationState;

import java.util.ResourceBundle;

public final class SimulationViewHelper {
    private SimulationViewHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void bindCanvasSize(
        final Canvas simulationCanvas,
        final StackPane simulationField
    ) {
        simulationCanvas.widthProperty()
            .bind(simulationField.widthProperty());
        simulationCanvas.heightProperty()
            .bind(simulationField.heightProperty());
    }

    public static void draw(
        final Canvas simulationCanvas,
        final Habitat habitat
    ) {
        final var graphicsContext =
            simulationCanvas.getGraphicsContext2D();
        graphicsContext.clearRect(
            0, 0,
            simulationCanvas.getWidth(),
            simulationCanvas.getHeight()
        );
        habitat.draw(graphicsContext);
    }

    public static void updateMenuItems(
        final SimulationState state,
        final MenuItem menuStart,
        final MenuItem menuRestart,
        final MenuItem menuStop,
        final MenuItem menuPause
    ) {
        switch (state) {
            case Stopped -> {
                menuStart.setDisable(false);
                setMenuItemsDisabled(true,
                    menuPause,
                    menuStop,
                    menuRestart
                );
            }
            case Running -> {
                menuStart.setDisable(true);
                setMenuItemsDisabled(false,
                    menuPause,
                    menuStop,
                    menuRestart
                );
            }
            case Paused -> {
                menuPause.setDisable(true);
                setMenuItemsDisabled(false,
                    menuStart,
                    menuStop,
                    menuRestart
                );
            }
        }
    }

    public static void updateMenuTimeText(
        final boolean showTime,
        final ResourceBundle resources,
        final CheckMenuItem menuToggleTime,
        final FontIcon menuTimeIcon
    ) {
        final String textKey, iconLiteral, styleClass;

        if (showTime) {
            textKey = "menu.view.show.time";
            iconLiteral = "fas-clock";
            styleClass = "menu-icon-time-active";
        } else {
            textKey = "menu.view.hide.time";
            iconLiteral = "fas-eye-slash";
            styleClass = "menu-icon-time-inactive";
        }

        menuToggleTime.setText(resources.getString(textKey));
        menuToggleTime.setSelected(showTime);
        menuTimeIcon.setIconLiteral(iconLiteral);
        menuTimeIcon.getStyleClass()
            .setAll("menu-icon", styleClass);
    }

    public static void updatePanelStatistics(
        final VehicleStatistics statistics,
        final LabeledValueRow truckRow,
        final LabeledValueRow carRow,
        final LabeledValueRow totalRow
    ) {
        truckRow.setValue(statistics.trucks());
        carRow.setValue(statistics.cars());
        totalRow.setValue(statistics.total());
    }

    public static void updateStatusTime(
        final long elapsedTime,
        final boolean showTime,
        final ResourceBundle resources,
        final LabeledValueRow statusTimeRow
    ) {
        if (showTime) {
            statusTimeRow.setValue(TimeFormatter.formatTime(
                elapsedTime,
                resources
            ));
        }
    }

    public static void updateStatus(
        final SimulationState state,
        final ResourceBundle resources,
        final Label statusLabel,
        final FontIcon statusIcon
    ) {
        statusLabel.setText(resources.getString(state.messageKey));
        statusLabel.getStyleClass()
            .setAll("status-value", state.styleClass);
        statusIcon.setIconLiteral(state.iconLiteral);
        statusIcon.getStyleClass()
            .setAll("status-icon", state.styleClass);
    }

    public static void setStatusTimeVisible(
        final boolean visible,
        final StackPane statusTimeContainer,
        final VBox statusContainer
    ) {
        setNodeVisible(statusTimeContainer, visible);
        updateStatusContainerHeight(visible, statusContainer);
    }

    public static void updateStatusContainerHeight(
        final boolean isTimeVisible,
        final VBox statusContainer
    ) {
        statusContainer.setMinHeight(isTimeVisible ? 120 : 70);
    }

    public static void setNodeVisible(
        final Node node,
        final boolean visible
    ) {
        node.setVisible(visible);
        node.setManaged(visible);
    }

    public static void setNodesVisible(
        final boolean visible,
        final Node... nodes
    ) {
        for (final var node : nodes) {
            setNodeVisible(node, visible);
        }
    }

    private static void setMenuItemsDisabled(
        final boolean disabled,
        final MenuItem... menuItems
    ) {
        for (final var item : menuItems) {
            item.setDisable(disabled);
        }
    }
}