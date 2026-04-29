package org.penakelex.objectsimulationsystem.ui.components;

import javafx.beans.DefaultProperty;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

@DefaultProperty("content")
public final class IconedSection extends VBox {
    private final FontIcon icon = new FontIcon();
    private final Label titleLabel = new Label();
    private final VBox contentContainer = new VBox();

    public IconedSection() {
        getStyleClass().add("iconed-section");

        icon.getStyleClass().add("section-icon");
        titleLabel.getStyleClass().add("section-title");

        contentContainer.getStyleClass().add("section-content");

        final var header = new HBox(icon, titleLabel);
        header.getStyleClass().add("section-header");

        getChildren().addAll(header, contentContainer);
    }

    public ObservableList<Node> getContent() {
        return contentContainer.getChildren();
    }

    public void setTitle(final String title) {
        titleLabel.setText(title);
    }

    public String getTitle() {
        return titleLabel.getText();
    }

    public void setIconLiteral(final String iconLiteral) {
        icon.setIconLiteral(iconLiteral);
    }

    public String getIconLiteral() {
        return icon.getIconLiteral();
    }
}