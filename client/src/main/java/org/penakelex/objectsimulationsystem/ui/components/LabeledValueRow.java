package org.penakelex.objectsimulationsystem.ui.components;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import org.kordamp.ikonli.javafx.FontIcon;

public final class LabeledValueRow extends HBox {
    private final FontIcon icon = new FontIcon();
    private final Label label = new Label();
    private final Label valueLabel = new Label();

    public LabeledValueRow() {
        getStyleClass().add("labeled-value-row");

        icon.getStyleClass().add("icon");
        label.getStyleClass().add("label");
        valueLabel.getStyleClass().add("row-value");

        getChildren().addAll(icon, label, valueLabel);
    }

    public void setValue(final String value) {
        valueLabel.setText(value);
    }

    public void setValue(final int value) {
        setValue(String.valueOf(value));
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
