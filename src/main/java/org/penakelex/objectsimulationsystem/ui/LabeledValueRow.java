package org.penakelex.objectsimulationsystem.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.javafx.FontIcon;

public final class LabeledValueRow extends HBox {
    private final FontIcon icon = new FontIcon();
    private final Label label = new Label();
    private final Label valueLabel = new Label();

    public LabeledValueRow() {
        super(10);

        getStyleClass().add("labeled-value-row");
        setAlignment(Pos.CENTER_LEFT);

        icon.setIconSize(14);

        label.getStyleClass().add("row-label");

        valueLabel.getStyleClass().add("row-value");
        HBox.setHgrow(valueLabel, Priority.ALWAYS);

        getChildren().addAll(icon, label, valueLabel);
    }

    public LabeledValueRow(
        final String iconLiteral,
        final String iconColor,
        final String labelText,
        final String value
    ) {
        this();
        setIconLiteral(iconLiteral);
        setIconColor(iconColor);
        setLabelText(labelText);
        setValue(value);
    }

    public void setValue(final String value) {
        valueLabel.setText(value);
    }

    public void setValue(final int value) {
        setValue(String.valueOf(value));
    }

    public void setValue(final long value) {
        setValue(String.valueOf(value));
    }

    public void setValue(final double value) {
        setValue(String.valueOf(value));
    }

    public String getValue() {
        return valueLabel.getText();
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

    public void addValueStyleClass(final String styleClass) {
        valueLabel.getStyleClass().add(styleClass);
    }

    public void removeValueStyleClass(final String styleClass) {
        valueLabel.getStyleClass().remove(styleClass);
    }

    public Label getValueLabel() {
        return valueLabel;
    }
}
