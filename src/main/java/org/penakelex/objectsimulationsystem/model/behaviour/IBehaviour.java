package org.penakelex.objectsimulationsystem.model.behaviour;

import javafx.scene.canvas.GraphicsContext;

public interface IBehaviour {
    void update(final long time);
    void draw(final GraphicsContext context);
}
