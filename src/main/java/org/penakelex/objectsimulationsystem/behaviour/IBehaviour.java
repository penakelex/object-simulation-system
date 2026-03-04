package org.penakelex.objectsimulationsystem.behaviour;

import javafx.scene.canvas.GraphicsContext;

public interface IBehaviour {
    void update(final long time);
    void draw(final GraphicsContext context);
}
