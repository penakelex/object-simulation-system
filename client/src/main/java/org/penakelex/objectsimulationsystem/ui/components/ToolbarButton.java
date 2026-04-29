package org.penakelex.objectsimulationsystem.ui.components;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

public final class ToolbarButton extends Button {
    private final StringProperty actionText =
        new SimpleStringProperty();
    private final StringProperty keyBind = new SimpleStringProperty();

    private final FontIcon icon = new FontIcon();
    private final Tooltip tooltip = new Tooltip();

    public ToolbarButton() {
        getStyleClass().add("toolbar-button");

        setPickOnBounds(true);

        icon.getStyleClass().add("icon");
        setGraphic(icon);

        tooltip.setShowDelay(Duration.millis(200));
        tooltip.setHideDelay(Duration.millis(250));
        tooltip.getStyleClass().add("tooltip");
        setTooltip(tooltip);

        actionTextProperty().addListener((_, _, _) -> updateTooltip());
        keyBindProperty().addListener((_, _, _) -> updateTooltip());

        setOnMouseExited(_ -> setFocused(false));
    }

    private void updateTooltip() {
        if (actionText.get() != null) {
            final var tooltipContent = new KeyActionRow();
            tooltipContent.setActionText(actionText.get());
            tooltipContent.setKeyBind(keyBind.get());

            tooltip.setGraphic(tooltipContent);
        }
    }

    public void setIconLiteral(final String iconLiteral) {
        icon.setIconLiteral(iconLiteral);
    }

    public String getIconLiteral() {
        return icon.getIconLiteral();
    }

    public StringProperty actionTextProperty() {
        return actionText;
    }

    public void setActionText(final String actionText) {
        this.actionText.set(actionText);
    }

    public String getActionText() {
        return actionText.get();
    }

    public StringProperty keyBindProperty() {
        return keyBind;
    }

    public void setKeyBind(final String keyBind) {
        this.keyBind.set(keyBind);
    }

    public String getKeyBind() {
        return keyBind.get();
    }
}