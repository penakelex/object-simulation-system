package org.penakelex.objectsimulationsystem.ui.dialogs;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.penakelex.objectsimulationsystem.SimulationApplication;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.stream.Stream;

public final class ConsoleDialog {
    private ConsoleDialog() {
        throw new UnsupportedOperationException("Utility class");
    }

    private record CommandInfo(Runnable action, String responseKey) {
    }

    private static final class ConsoleTextArea extends TextArea {
        private int inputStartIndex = 0;

        public void setInputStartIndex(final int index) {
            inputStartIndex = Math.max(0, index);
        }

        public int getInputStartIndex() {
            return inputStartIndex;
        }

        @Override
        public void replaceText(
            final int start,
            final int end,
            final String text
        ) {
            if (start < this.inputStartIndex) {
                positionCaret(getText().length());
                return;
            }

            super.replaceText(start, end, text);
        }

        @Override
        public void replaceSelection(final String replacement) {
            final var selectionStart = getSelection().getStart();

            if (selectionStart < this.inputStartIndex) {
                positionCaret(getText().length());
                deselect();
                return;
            }

            super.replaceSelection(replacement);
        }
    }

    public static void showDialog(
        final Stage ownerStage,
        final Runnable stopAI,
        final Runnable resumeAI,
        final ResourceBundle resources,
        final Consumer<Boolean> onAIStateChanged
    ) {
        final var dialogStage = new Stage();
        dialogStage.initOwner(ownerStage);
        dialogStage.initModality(Modality.NONE);
        dialogStage.setTitle(resources.getString(
            "dialog.console.title"
        ));

        final var consoleArea = new ConsoleTextArea();
        consoleArea.getStyleClass().add("console-text-area");
        consoleArea.setWrapText(false);
        consoleArea
            .setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        final PipedOutputStream pipeOut;
        final PipedInputStream pipeIn;

        try {
            pipeOut = new PipedOutputStream();
            pipeIn = new PipedInputStream(pipeOut);
        } catch (final IOException exception) {
            throw new UncheckedIOException(exception);
        }

        final var commandMap = buildCommandMap(
            resources,
            stopAI,
            resumeAI,
            onAIStateChanged
        );
        final var availableCommandsText =
            getAvailableCommandsText(resources);

        consoleArea.setText("> ");
        consoleArea.setInputStartIndex(2);

        final var commandProcessor = new Thread(
            () -> processCommands(
                pipeIn,
                commandMap,
                availableCommandsText,
                resources,
                consoleArea
            ),
            "ConsoleProcessor"
        );
        commandProcessor.setDaemon(true);
        commandProcessor.start();

        consoleArea.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER &&
                !event.isShiftDown()) {
                event.consume();

                final var fullText = consoleArea.getText();
                final var currentIndex =
                    consoleArea.getInputStartIndex();
                final var input =
                    fullText.substring(currentIndex).trim();

                if (!input.isEmpty()) {
                    try {
                        pipeOut.write((input + "\n").getBytes());
                    } catch (final IOException _) {
                        appendToConsole(
                            consoleArea,
                            resources.getString(
                                "dialog.console.error.channel"
                            )
                        );
                    }
                } else {
                    consoleArea.appendText("> ");
                    consoleArea.setInputStartIndex(
                        consoleArea.getText().length()
                    );
                }

                consoleArea.positionCaret(
                    consoleArea.getText().length()
                );

                return;
            }

            if (event.getCode() == KeyCode.LEFT ||
                event.getCode() == KeyCode.HOME) {
                if (consoleArea.getCaretPosition() <=
                    consoleArea.getInputStartIndex()) {
                    event.consume();
                }
            }
        });

        consoleArea.addEventFilter(
            MouseEvent.MOUSE_CLICKED,
            _ -> {
                if (consoleArea.getCaretPosition() <
                    consoleArea.getInputStartIndex()) {
                    consoleArea.positionCaret(
                        consoleArea.getInputStartIndex()
                    );
                }
            }
        );

        final var root = new VBox(consoleArea);
        root.getStyleClass().add("console-dialog-pane");
        VBox.setVgrow(consoleArea, Priority.ALWAYS);
        final var scene = new Scene(root);
        applyStylesheets(scene, ownerStage);

        dialogStage.setScene(scene);
        dialogStage.sizeToScene();
        dialogStage.centerOnScreen();
        dialogStage.show();

        Platform.runLater(() -> {
            consoleArea.requestFocus();
            consoleArea.positionCaret(consoleArea.getLength());
            dialogStage.setMinWidth(root.getMinWidth());
            dialogStage.setMinHeight(root.getMinHeight());
        });

        dialogStage.setOnCloseRequest(_ -> {
            try {
                pipeOut.close();
            } catch (final IOException _) {
            }

            commandProcessor.interrupt();
        });
    }

    private static Map<String, CommandInfo> buildCommandMap(
        final ResourceBundle resources,
        final Runnable stopAI,
        final Runnable resumeAI,
        final Consumer<Boolean> onAIStateChanged
    ) {
        final var map = new LinkedHashMap<String, CommandInfo>();

        parseAliases(resources,
            "console.command.stop_ai",
            new CommandInfo(() -> {
                stopAI.run();
                onAIStateChanged.accept(true);
            },
                "dialog.console.response.ai.stop"
            ),
            map
        );
        parseAliases(resources,
            "console.command.resume_ai",
            new CommandInfo(() -> {
                resumeAI.run();
                onAIStateChanged.accept(false);
            },
                "dialog.console.response.ai.resume"
            ),
            map
        );

        return map;
    }

    private static void parseAliases(
        final ResourceBundle resources,
        final String propertyKey,
        final CommandInfo commandInfo,
        final Map<String, CommandInfo> targetMap
    ) {
        final var aliases = resources.getString(propertyKey);

        for (final var alias : aliases.split(",")) {
            final var normalized = alias.trim().toLowerCase();

            if (!normalized.isEmpty()) {
                targetMap.put(normalized, commandInfo);
            }
        }
    }

    private static String getAvailableCommandsText(final ResourceBundle res) {
        return Stream.of(
            "console.command.stop_ai",
                "console.command.resume_ai"
            )
            .map(key -> res.getString(key).split(",")[0].trim())
            .map("'%s'"::formatted)
            .reduce((a, b) -> a + ", " + b)
            .orElse("???");
    }

    private static void processCommands(
        final PipedInputStream pipeIn,
        final Map<String, CommandInfo> commandMap,
        final String availableCommandsText,
        final ResourceBundle resources,
        final ConsoleTextArea consoleArea
    ) {
        try (
            final var reader = new BufferedReader(
                new InputStreamReader(pipeIn)
            )
        ) {
            String line;

            while ((line = reader.readLine()) != null) {
                final var commandInfo =
                    commandMap.get(line.trim().toLowerCase());

                if (commandInfo != null) {
                    commandInfo.action.run();
                    appendToConsole(
                        consoleArea,
                        resources.getString(commandInfo.responseKey)
                    );
                } else {
                    appendToConsole(
                        consoleArea,
                        resources
                            .getString("dialog.console.error.unknown")
                            .formatted(availableCommandsText)
                    );
                }
            }
        } catch (final IOException _) {
        }
    }

    private static void appendToConsole(
        final ConsoleTextArea area,
        final String text
    ) {
        Platform.runLater(() -> {
            area.appendText(text);
            area.appendText("\n> ");
            area.setInputStartIndex(area.getText().length());
            area.positionCaret(area.getText().length());
        });
    }

    private static void applyStylesheets(
        final Scene dialogScene,
        final Stage ownerStage
    ) {
        if (ownerStage != null && ownerStage.getScene() != null) {
            final var ownerStylesheets =
                ownerStage.getScene().getStylesheets();

            if (!ownerStylesheets.isEmpty()) {
                dialogScene.getStylesheets().addAll(ownerStylesheets);
                return;
            }
        }

        final var cssURL =
            SimulationApplication.class.getResource("css/main.css");

        if (cssURL != null) {
            dialogScene.getStylesheets().add(cssURL.toExternalForm());
        }
    }
}