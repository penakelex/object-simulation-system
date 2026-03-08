package org.penakelex.objectsimulationsystem.ui;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.javafx.FontIcon;

public final class IconedSection extends VBox {
    private final FontIcon icon = new FontIcon();
    private final Label titleLabel = new Label();

    public IconedSection() {
        super();

        getStyleClass().add("iconed-section");

        icon.getStyleClass().add("section-icon");
        titleLabel.getStyleClass().add("section-title");

        final var header = new HBox(icon, titleLabel);
        header.getStyleClass().add("section-header");

        final var contentContainer = new VBox();
        contentContainer.getStyleClass().add("section-content");
        VBox.setVgrow(contentContainer, Priority.ALWAYS);

        getChildren().addAll(header, contentContainer);
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

    public void setIconColor(final String color) {
        icon.setIconColor(Color.web(color));
    }

    public String getIconColor() {
        return icon.getIconColor().toString();
    }
}
