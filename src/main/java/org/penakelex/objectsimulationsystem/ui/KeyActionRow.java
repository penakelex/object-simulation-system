package org.penakelex.objectsimulationsystem.ui;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public final class KeyActionRow extends HBox {
    private final Label keyBadge = new Label();
    private final Label actionLabel = new Label();

    public KeyActionRow() {
        getStyleClass().add("key-action-pair");
        keyBadge.getStyleClass().add("key-badge");
        actionLabel.getStyleClass().add("action-description");

        getChildren().addAll(keyBadge, actionLabel);
    }

    public void setKeyBind(final String keyBind) {
        keyBadge.setText(keyBind);
    }

    public String getKeyBind() {
        return keyBadge.getText();
    }

    public void setActionText(final String actionText) {
        actionLabel.setText(actionText);
    }

    public String getActionText() {
        return actionLabel.getText();
    }
}