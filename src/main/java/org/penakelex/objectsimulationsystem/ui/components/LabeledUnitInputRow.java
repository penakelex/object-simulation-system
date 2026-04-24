package org.penakelex.objectsimulationsystem.ui.components;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import org.kordamp.ikonli.javafx.FontIcon;

public final class LabeledUnitInputRow extends HBox {
    private final FontIcon icon = new FontIcon();
    private final Label label = new Label();
    private final TextField textField = new TextField();
    private final Label unitLabel = new Label();

    public LabeledUnitInputRow() {
        getStyleClass().add("labeled-unit-input-row");
        icon.getStyleClass().add("icon");
        label.getStyleClass().add("label");
        textField.getStyleClass().add("input-field");
        unitLabel.getStyleClass().add("unit-label");
        getChildren().addAll(icon, label, textField, unitLabel);
    }

    public StringProperty textProperty() {
        return textField.textProperty();
    }

    public String getFieldText() {
        return textField.getText();
    }

    public void setTextFieldValue(final int value) {
        textField.setText(String.valueOf(value));
    }

    public ReadOnlyBooleanProperty textFieldFocusedProperty() {
        return textField.focusedProperty();
    }

    public void setError(final boolean error) {
        final var errorClass = "input-error";
        if (error) {
            textField.getStyleClass().add(errorClass);
        } else {
            textField.getStyleClass().remove(errorClass);
        }
    }

    public void setLabelText(final String text) {
        label.setText(text);
    }

    public String getLabelText() {
        return label.getText();
    }

    public void setIconLiteral(final String literal) {
        icon.setIconLiteral(literal);
    }

    public String getIconLiteral() {
        return icon.getIconLiteral();
    }

    public void setUnitText(final String unit) {
        unitLabel.setText(unit);
    }

    public String getUnitText() {
        return unitLabel.getText();
    }

    public StringProperty unitTextProperty() {
        return unitLabel.textProperty();
    }
}