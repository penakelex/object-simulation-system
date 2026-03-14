package org.penakelex.objectsimulationsystem.vehicle.images;

import javafx.scene.image.Image;
import org.penakelex.objectsimulationsystem.habitat.Configuration;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public sealed abstract class VehicleImages
    permits CarImages, TruckImages
{
    protected final List<Image> images;

    protected VehicleImages() throws IllegalStateException {
        images = loadImages();
    }

    protected abstract List<String> getImagesNames();

    private List<Image> loadImages() throws IllegalStateException {
        final var imagesNames = getImagesNames();

        if (imagesNames.isEmpty()) {
            throw new IllegalStateException(String.format(
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
    }

    public final Image getRandomImage() {
        return images.get(
            ThreadLocalRandom.current().nextInt(images.size())
        );
    }
}