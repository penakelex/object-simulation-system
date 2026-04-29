package org.penakelex.objectsimulationsystem.model.vehicle.images;

import javafx.scene.image.Image;
import org.apache.commons.lang3.tuple.Pair;
import org.penakelex.objectsimulationsystem.model.habitat.Configuration;
import org.penakelex.objectsimulationsystem.model.vehicle.images.exceptions.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

public sealed abstract class VehicleImages
    permits CarImages, TruckImages
{
    protected final List<Image> images;
    private final Map<Short, Image> pathsToImages = new HashMap<>();
    private final Map<Image, Short> imagesPaths = new HashMap<>();

    protected VehicleImages() {
        images = loadImages();
    }

    protected abstract String getImagesPath();

    private List<Image> loadImages() {
        final var imagesNames = discoverImageFiles();

        if (imagesNames.isEmpty()) {
            throw new NoImagesFoundException(
                getImagesPath(),
                getClass().getSimpleName()
            );
        }

        return IntStream.range(0, imagesNames.size()).mapToObj(i -> {
            final var name = imagesNames.get(i);
            final Image image;

            try {
                image = new Image(
                    Objects.requireNonNull(
                        getClass().getResourceAsStream(name)
                    ),
                    Configuration.VEHICLE_IMAGE_SIZE,
                    Configuration.VEHICLE_IMAGE_SIZE,
                    true,
                    true
                );
            } catch (final Exception exception) {
                throw new ImageLoadException(name, exception);
            }

            if (image.isError()) {
                throw new ImageLoadException(name, null);
            }

            pathsToImages.put((short) i, image);
            imagesPaths.put(image, (short) i);
            return image;
        }).toList();
    }

    private List<String> discoverImageFiles() {
        final var imagesPath = getImagesPath();

        try {
            final var resourceURL =
                getClass().getResource(imagesPath);

            if (resourceURL == null) {
                throw new ResourcePathNotFoundException(
                    imagesPath,
                    getClass().getSimpleName()
                );
            }

            return scanDirectoryForResource(
                imagesPath,
                resourceURL.toURI()
            );
        } catch (final URISyntaxException exception) {
            throw new InvalidImagesUriException(
                imagesPath,
                exception.getMessage(),
                exception
            );
        } catch (final IOException exception) {
            throw new DirectoryScanFailedException(
                imagesPath,
                exception.getMessage(),
                exception
            );
        }
    }

    private List<String> scanDirectoryForResource(
        final String imagesPath,
        final URI uri
    ) throws IOException {
        final Path path;
        final FileSystem fs;

        if ("jar".equals(uri.getScheme())) {
            fs = FileSystems.newFileSystem(uri, Map.of());
            path = fs.getPath(imagesPath);
        } else {
            fs = null;
            path = Paths.get(uri);
        }

        try {
            return scanPathForImages(path, imagesPath);
        } finally {
            if (fs != null) {
                fs.close();
            }
        }
    }

    private List<String> scanPathForImages(
        final Path path,
        final String imagesPath
    ) throws IOException {
        if (!Files.isDirectory(path)) {
            return List.of();
        }

        try (final var stream = Files.list(path)) {
            return stream
                .filter(Files::isRegularFile)
                .map(filePath -> filePath.getFileName().toString())
                .filter(this::isImageFile)
                .sorted()
                .map(name -> String.format("%s/%s",
                    imagesPath, name
                ))
                .toList();
        }
    }

    private boolean isImageFile(final String fileName) {
        return Arrays.stream(Configuration.VEHICLE_IMAGE_EXTENSIONS)
            .anyMatch(extension -> fileName
                .toLowerCase()
                .endsWith(extension.trim())
            );
    }

    public Optional<Image> getImageForResourceIndex(
        final short imagePath
    ) {
        return Optional.ofNullable(pathsToImages.get(imagePath));
    }

    public Pair<Image, Short> getRandomImageWithIndex() {
        final var selectedImage = images.get(ThreadLocalRandom
            .current()
            .nextInt(images.size())
        );

        return Pair.of(selectedImage, imagesPaths.get(selectedImage));
    }
}