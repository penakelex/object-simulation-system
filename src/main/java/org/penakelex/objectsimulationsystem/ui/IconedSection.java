package org.penakelex.objectsimulationsystem.ui;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.javafx.FontIcon;

public final class IconedSection extends VBox {
    private final FontIcon icon = new FontIcon();
    private final Label titleLabel = new Label();
    private final VBox contentContainer = new VBox(5);

    public IconedSection() {
        super(5);

        getStyleClass().add("iconed-section");

        icon.setIconSize(16);
        titleLabel.getStyleClass().add("section-title");

        final var header = new HBox(8, icon, titleLabel);
        header.getStyleClass().add("section-header");
        header.setAlignment(Pos.CENTER_LEFT);

        contentContainer.getStyleClass().add("section-content");
        VBox.setVgrow(contentContainer, Priority.ALWAYS);

        getChildren().addAll(header, contentContainer);
    }

    public IconedSection(
        final String iconLiteral,
        final String title,
        final String iconColor,
        final Node... content
    ) {
        this();
        setIconLiteral(iconLiteral);
        setIconColor(iconColor);
        setTitle(title);

        if (content != null && content.length > 0) {
            contentContainer.getChildren().addAll(content);
        }
    }

    public void setContent(final Node... children) {
        contentContainer.getChildren().setAll(children);
    }

    public void addContent(final Node... children) {
        contentContainer.getChildren().addAll(children);
    }

    public void clearContent() {
        contentContainer.getChildren().clear();
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

    public VBox getContentContainer() {
        return contentContainer;
    }
}
