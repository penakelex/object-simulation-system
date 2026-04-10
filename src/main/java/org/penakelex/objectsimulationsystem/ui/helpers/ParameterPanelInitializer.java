package org.penakelex.objectsimulationsystem.ui.helpers;

import javafx.stage.Stage;
import org.penakelex.objectsimulationsystem.model.habitat.Configuration;
import org.penakelex.objectsimulationsystem.model.habitat.Habitat;
import org.penakelex.objectsimulationsystem.model.habitat.TimeUnit;
import org.penakelex.objectsimulationsystem.ui.WarningDialog;
import org.penakelex.objectsimulationsystem.ui.components.LabeledInputRow;
import org.penakelex.objectsimulationsystem.ui.components.LabeledProbabilityBox;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public final class ParameterPanelInitializer {
    private ParameterPanelInitializer() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void initialize(
        final Supplier<Habitat> habitatSupplier,
        final ResourceBundle resources,
        final Stage ownerStage,
        final LabeledInputRow truckPeriodInput,
        final LabeledInputRow carPeriodInput,
        final LabeledProbabilityBox truckProbabilityBox,
        final LabeledProbabilityBox carProbabilityBox,
        final LabeledInputRow truckLifetimeInput,
        final LabeledInputRow carLifetimeInput
    ) {
        final var timeUnitLabels = Arrays.stream(TimeUnit.values())
            .map(timeUnit -> resources
                .getString(timeUnit.messageKey)
            )
            .toList();

        initializePeriodInput(
            habitatSupplier,
            resources,
            ownerStage,
            truckPeriodInput,
            Configuration.TRUCK_SPAWN_PERIOD,
            Configuration.TRUCK_SPAWN_TIME_UNIT,
            timeUnitLabels,
            Habitat::updateTruckPeriod,
            Habitat::updateTruckPeriodTimeUnit
        );

        initializePeriodInput(
            habitatSupplier,
            resources,
            ownerStage,
            carPeriodInput,
            Configuration.CAR_SPAWN_PERIOD,
            Configuration.CAR_SPAWN_TIME_UNIT,
            timeUnitLabels,
            Habitat::updateCarPeriod,
            Habitat::updateCarPeriodTimeUnit
        );

        final var probabilityLabels = IntStream.rangeClosed(0, 10)
            .mapToObj(i -> resources
                .getString("format.probability.percent")
                .formatted(i * 10)
            )
            .toList();

        initializeProbabilityBox(
            habitatSupplier,
            truckProbabilityBox,
            probabilityLabels,
            Configuration.TRUCK_SPAWN_PROBABILITY,
            Habitat::updateTruckProbability
        );

        initializeProbabilityBox(
            habitatSupplier,
            carProbabilityBox,
            probabilityLabels,
            Configuration.CAR_SPAWN_PROBABILITY,
            Habitat::updateCarProbability
        );

        initializeLifetimeInput(
            habitatSupplier,
            resources,
            ownerStage,
            truckLifetimeInput,
            Configuration.TRUCK_LIFETIME,
            Configuration.TRUCK_LIFETIME_TIME_UNIT,
            timeUnitLabels,
            Habitat::updateTruckLifeTime,
            Habitat::updateTruckLifeTimeUnit
        );

        initializeLifetimeInput(
            habitatSupplier,
            resources,
            ownerStage,
            carLifetimeInput,
            Configuration.CAR_LIFETIME,
            Configuration.CAR_LIFETIME_TIME_UNIT,
            timeUnitLabels,
            Habitat::updateCarLifeTime,
            Habitat::updateCarLifeTimeUnit
        );
    }

    private static void initializePeriodInput(
        final Supplier<Habitat> habitatSupplier,
        final ResourceBundle resources,
        final Stage ownerStage,
        final LabeledInputRow input,
        final int defaultValue,
        final TimeUnit defaultTimeUnit,
        final List<String> timeUnitLabels,
        final BiConsumer<Habitat, Integer> periodSetter,
        final BiConsumer<Habitat, TimeUnit> timeUnitSetter
    ) {
        input.setTextFieldValue(defaultValue);
        input.initializeComboBoxValues(timeUnitLabels,
            resources.getString(defaultTimeUnit.messageKey)
        );

        input.textProperty().addListener((_, _, newValue) -> {
            final var habitat = habitatSupplier.get();
            if (habitat == null) {
                return;
            }

            final var validated = validatePositiveInteger(newValue);
            if (validated.isPresent()) {
                periodSetter.accept(habitat, validated.get());
                input.setError(false);
            } else {
                input.setError(true);
            }
        });

        input.comboBoxValueProperty()
            .addListener((_, _, newValue) -> {
                final var habitat = habitatSupplier.get();

                if (habitat != null) {
                    findTimeUnit(resources, newValue)
                        .ifPresent(timeUnit -> timeUnitSetter
                            .accept(habitat, timeUnit)
                        );
                }
            });

        input.textFieldFocusedProperty()
            .addListener((_, _, focused) ->
                handleInputFocusLoss(
                    focused,
                    input,
                    defaultValue,
                    defaultTimeUnit,
                    resources,
                    ownerStage
                )
            );
    }

    private static void initializeLifetimeInput(
        final Supplier<Habitat> habitatSupplier,
        final ResourceBundle resources,
        final Stage ownerStage,
        final LabeledInputRow input,
        final int defaultValue,
        final TimeUnit defaultTimeUnit,
        final List<String> timeUnitLabels,
        final BiConsumer<Habitat, Integer> lifetimeSetter,
        final BiConsumer<Habitat, TimeUnit> timeUnitSetter
    ) {
        input.setTextFieldValue(defaultValue);
        input.initializeComboBoxValues(timeUnitLabels,
            resources.getString(defaultTimeUnit.messageKey)
        );

        input.textProperty().addListener((_, _, newValue) -> {
            final var habitat = habitatSupplier.get();
            if (habitat == null) {
                return;
            }

            final var validated = validatePositiveInteger(newValue);
            if (validated.isPresent()) {
                lifetimeSetter.accept(habitat, validated.get());
                input.setError(false);
            } else {
                input.setError(true);
            }
        });

        input.comboBoxValueProperty()
            .addListener((_, _, newValue) -> {
                final var habitat = habitatSupplier.get();

                if (habitat != null) {
                    findTimeUnit(resources, newValue)
                        .ifPresent(timeUnit -> timeUnitSetter
                            .accept(habitat, timeUnit)
                        );
                }
            });

        input.textFieldFocusedProperty()
            .addListener((_, _, focused) -> handleInputFocusLoss(
                focused,
                input,
                defaultValue,
                defaultTimeUnit,
                resources,
                ownerStage
            ));
    }

    private static void initializeProbabilityBox(
        final Supplier<Habitat> habitatSupplier,
        final LabeledProbabilityBox box,
        final List<String> probabilityLabels,
        final double defaultValue,
        final BiConsumer<Habitat, Double> probabilitySetter
    ) {
        box.initializeProbabilities(
            probabilityLabels,
            (int) (defaultValue * 10)
        );
        box.selectedIndexProperty().addListener((_, _, newIndex) -> {
            final var habitat = habitatSupplier.get();

            if (habitat != null) {
                probabilitySetter.accept(
                    habitat,
                    newIndex.intValue() / 10.
                );
            }
        });
    }

    private static Optional<Integer> validatePositiveInteger(final String value) {
        try {
            final var parsed = Integer.parseUnsignedInt(value);
            if (parsed > 0) {
                return Optional.of(parsed);
            }
        } catch (final NumberFormatException _) {
        }

        return Optional.empty();
    }

    private static Optional<TimeUnit> findTimeUnit(
        final ResourceBundle resources,
        final String label
    ) {
        return Arrays.stream(TimeUnit.values())
            .filter(timeUnit -> resources
                .getString(timeUnit.messageKey)
                .equals(label)
            )
            .findFirst();
    }

    private static void handleInputFocusLoss(
        final boolean focused,
        final LabeledInputRow input,
        final int defaultValue,
        final TimeUnit defaultTimeUnit,
        final ResourceBundle resources,
        final Stage ownerStage
    ) {
        if (focused) {
            return;
        }

        final var fieldText = input.getFieldText();
        if (validatePositiveInteger(fieldText).isPresent()) {
            return;
        }

        final var wrongTimeUnit = input.getComboBoxValue();
        final var defaultTimeUnitLabel =
            resources.getString(defaultTimeUnit.messageKey);

        input.setTextFieldValue(defaultValue);
        input.setComboBoxValue(defaultTimeUnitLabel);
        input.setError(false);

        WarningDialog.showWarning(
            ownerStage,
            resources,
            resources.getString("error.invalid.period").formatted(
                fieldText,
                wrongTimeUnit,
                defaultValue,
                defaultTimeUnitLabel
            )
        );
    }
}