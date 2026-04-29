package org.penakelex.objectsimulationsystem.ui.components;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.function.Consumer;

public final class AIControlRow extends HBox {
    private final FontIcon vehicleIcon;
    private final Label label;
    private final Button toggleButton;
    private final FontIcon toggleIcon;
    private final ComboBox<Integer> priorityComboBox;

    private boolean isPaused = false;

    public AIControlRow() {
        getStyleClass().add("ai-control-row");

        vehicleIcon = new FontIcon();
        vehicleIcon.getStyleClass().add("icon");

        label = new Label();
        label.getStyleClass().add("label");

        toggleButton = new Button();
        toggleButton.getStyleClass().add("toggle-button");

        toggleIcon = new FontIcon("fas-pause");
        toggleIcon.getStyleClass().add("toggle-button-icon");

        toggleButton.setGraphic(toggleIcon);

        priorityComboBox = new ComboBox<>();
        priorityComboBox.getStyleClass().add("priority-combo");

        getChildren().addAll(
            vehicleIcon,
            label,
            toggleButton,
            priorityComboBox
        );
    }

    public void setIconLiteral(final String literal) {
        vehicleIcon.setIconLiteral(literal);
    }

    public String getIconLiteral() {
        return vehicleIcon.getIconLiteral();
    }

    public void setIconColor(final String color) {
        vehicleIcon.setIconColor(Color.web(color));
    }

    public String getIconColor() {
        return vehicleIcon.getIconColor().toString();
    }

    public void setLabelText(final String text) {
        label.setText(text);
    }

    public String getLabelText() {
        return label.getText();
    }

    public void setToggleAction(final Consumer<Boolean> onToggle) {
        toggleButton.setOnAction(_ -> {
            isPaused = !isPaused;
            toggleIcon
                .setIconLiteral(isPaused ? "fas-play" : "fas-pause");
            onToggle.accept(isPaused);
        });
    }

    public ComboBox<Integer> getPriorityComboBox() {
        return priorityComboBox;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void setPausedSilent(final boolean paused) {
        this.isPaused = paused;
        toggleIcon.setIconLiteral(paused ? "fas-play" : "fas-pause");
    }
}