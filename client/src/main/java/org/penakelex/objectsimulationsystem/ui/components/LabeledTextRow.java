package org.penakelex.objectsimulationsystem.ui.components;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import org.kordamp.ikonli.javafx.FontIcon;

public final class LabeledTextRow extends HBox {
    private final FontIcon icon = new FontIcon();
    private final Label label = new Label();
    private final TextField textField = new TextField();

    public LabeledTextRow() {
        getStyleClass().add("labeled-text-row");
        icon.getStyleClass().add("icon");
        label.getStyleClass().add("label");
        textField.getStyleClass().add("input-field");
        getChildren().addAll(icon, label, textField);
    }

    public String getFieldText() {
        return textField.getText();
    }

    public void setTextFieldValue(final String value) {
        textField.setText(value);
    }

    public void setTextFieldValue(final int value) {
        textField.setText(String.valueOf(value));
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
}