package org.penakelex.objectsimulationsystem.ui.components;

import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;

public final class LabeledProbabilityBox extends HBox {
    private final FontIcon icon = new FontIcon();
    private final Label label = new Label();
    private final ComboBox<String> comboBox = new ComboBox<>();

    public LabeledProbabilityBox() {
        getStyleClass().add("labeled-probability-box");
        icon.getStyleClass().add("icon");
        label.getStyleClass().add("label");
        comboBox.getStyleClass().add("combobox");

        getChildren().addAll(icon, label, comboBox);
    }

    public void initializeProbabilities(
        final List<String> probabilities,
        final int initialProbabilityIndex
    ) {
        comboBox.getItems().addAll(probabilities);
        comboBox.setValue(probabilities.get(initialProbabilityIndex));
    }

    public ReadOnlyIntegerProperty selectedIndexProperty() {
        return comboBox.getSelectionModel().selectedIndexProperty();
    }

    public void setLabelText(final String text) {
        label.setText(text);
    }

    public String getLabelText() {
        return label.getText();
    }

    public void setIconLiteral(final String iconLiteral) {
        icon.setIconLiteral(iconLiteral);
    }

    public String getIconLiteral() {
        return icon.getIconLiteral();
    }

    public void setIconColor(final String color) {
        icon.setIconColor(Color.web(color));
    }
}