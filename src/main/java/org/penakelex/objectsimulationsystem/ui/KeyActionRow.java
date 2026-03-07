package org.penakelex.objectsimulationsystem.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public final class KeyActionRow extends HBox {
    private final Label keyBadge = new Label();
    private final Label actionLabel = new Label();

    public KeyActionRow() {
        super(8);

        getStyleClass().add("key-action-pair");
        setAlignment(Pos.CENTER_LEFT);

        keyBadge.getStyleClass().add("key-badge");
        keyBadge.setMinWidth(25);
        keyBadge.setAlignment(Pos.CENTER);

        actionLabel.getStyleClass().add("action-description");

        getChildren().addAll(keyBadge, actionLabel);
    }

    public KeyActionRow(
        final String keyBind,
        final String actionText
    ) {
        this();
        setKeyBind(keyBind);
        setActionText(actionText);
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

    public void addKeyStyleClass(final String styleClass) {
        keyBadge.getStyleClass().add(styleClass);
    }

    public Label getKeyBadge() {
        return keyBadge;
    }
}