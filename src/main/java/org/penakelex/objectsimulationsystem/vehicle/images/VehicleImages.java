package org.penakelex.objectsimulationsystem.vehicle.images;

import javafx.application.Platform;
import javafx.scene.image.Image;
import org.penakelex.objectsimulationsystem.habitat.Configuration;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

public sealed abstract class VehicleImages
    permits CarImages, TruckImages
{
    private static final Logger LOGGER =
        Logger.getLogger(VehicleImages.class.getName());

    protected List<Image> images;
    private boolean isLoaded = false;

    protected VehicleImages() throws AssertionError {
        loadImages();
    }

    protected abstract List<String> getImagesNames();

    private void loadImages() throws AssertionError {
        CompletableFuture.supplyAsync(() -> {
            final var imagesNames = getImagesNames();

            if (imagesNames.isEmpty()) {
                throw new AssertionError(String.format(
                    "Images can't be empty for %s",
                    getClass().getSimpleName()
                ));
            }

            return imagesNames.stream().map(name -> {
                final var image = new Image(
                    Objects.requireNonNull(getClass().getResourceAsStream(
                        String.format("%s/%s",
                            Configuration.VEHICLE_IMAGES_BASE_PATH,
                            name
                        )
                    )),
                    Configuration.VEHICLE_IMAGE_SIZE,
                    Configuration.VEHICLE_IMAGE_SIZE,
                    true,
                    true
                );

                if (image.isError()) {
                    throw new AssertionError(String.format(
                        "Failed to load image: %s", name
                    ));
                }

                return image;
            }).toList();
        }).thenAccept(loadedImages ->
            Platform.runLater(() -> {
                this.images = loadedImages;
                isLoaded = true;
            })
        ).exceptionally(exception -> {
            LOGGER.log(Level.SEVERE,
                String.format("Failed to load images for %s",
                    getClass().getSimpleName()
                ),
                exception
            );
            return null;
        });
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    public final Image getRandomImage() {
        return images.get(
            ThreadLocalRandom.current().nextInt(images.size())
        );
    }
}