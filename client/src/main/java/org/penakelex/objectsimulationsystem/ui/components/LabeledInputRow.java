package org.penakelex.objectsimulationsystem.ui.components;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;

public final class LabeledInputRow extends HBox {
    private final FontIcon icon = new FontIcon();
    private final Label label = new Label();
    private final TextField textField = new TextField();
    private final ComboBox<String> comboBox = new ComboBox<>();

    public LabeledInputRow() {
        getStyleClass().add("labeled-input-row");
        icon.getStyleClass().add("icon");
        label.getStyleClass().add("label");
        textField.getStyleClass().add("input-field");
        comboBox.getStyleClass().add("combobox");

        getChildren().addAll(icon, label, textField, comboBox);
    }

    public void initializeComboBoxValues(
        final List<String> values,
        final String initialValue
    ) {
        comboBox.getItems().addAll(values);
        comboBox.setValue(initialValue);
    }

    public void setTextFieldValue(final int value) {
        textField.setText(String.valueOf(value));
    }

    public StringProperty textProperty() {
        return textField.textProperty();
    }

    public String getFieldText() {
        return textField.getText();
    }

    public void setComboBoxValue(final String value) {
        comboBox.setValue(value);
    }

    public ObjectProperty<String> comboBoxValueProperty() {
        return comboBox.valueProperty();
    }

    public String getComboBoxValue() {
        return comboBox.getValue();
    }

    public ReadOnlyBooleanProperty textFieldFocusedProperty() {
        return textField.focusedProperty();
    }

    public void setError(final boolean error) {
        final var inputErrorClass = "input-error";

        if (error) {
            textField.getStyleClass().add(inputErrorClass);
        } else {
            textField.getStyleClass().remove(inputErrorClass);
        }
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

    public String getIconColor() {
        return icon.getIconColor().toString();
    }
}