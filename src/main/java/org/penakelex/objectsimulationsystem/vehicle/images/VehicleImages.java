package org.penakelex.objectsimulationsystem.vehicle.images;

import javafx.scene.image.Image;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public sealed abstract class VehicleImages
    permits CarImages, TruckImages
{
    private static final String BASE_PATH = "/images/vehicles";
    public static final int IMAGE_SIZE = 64;

    protected final List<Image> images;

    protected VehicleImages() throws AssertionError {
        this.images = loadImages();
    }

    protected abstract List<String> getImagesNames();

    private List<Image> loadImages() throws AssertionError {
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
                    String.format("%s/%s", BASE_PATH, name)
                )),
                IMAGE_SIZE,
                IMAGE_SIZE,
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
    }

    public final Image getRandomImage() {
        return images.get(
            ThreadLocalRandom.current().nextInt(images.size())
        );
    }
}
