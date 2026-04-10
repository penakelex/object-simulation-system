package org.penakelex.objectsimulationsystem.model.ai;

import javafx.geometry.Point2D;
import org.penakelex.objectsimulationsystem.model.vehicle.Vehicle;

import java.awt.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public abstract sealed class BaseAI<T extends Vehicle>
    implements Runnable permits TruckAI, CarAI
{
    protected final Supplier<List<T>> vehiclesSupplier;
    private final Thread thread;
    private final Object pauseLock = new Object();
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final AtomicBoolean paused = new AtomicBoolean(false);

    protected final double speed;

    private static final int FRAME_INTERVAL_MS;
    private long lastUpdateTime = 0;

    static {
        final var refreshRate = GraphicsEnvironment
            .getLocalGraphicsEnvironment()
            .getDefaultScreenDevice()
            .getDisplayMode()
            .getRefreshRate();

        FRAME_INTERVAL_MS = refreshRate > 0
            ? (int) Math.round(1e3 / refreshRate)
            : 16;
    }

    public BaseAI(
        final Supplier<List<T>> vehiclesSupplier,
        final double speed
    ) {
        this.vehiclesSupplier = vehiclesSupplier;
        this.speed = speed;

        this.thread = new Thread(
            this,
            getClass().getSimpleName()
        );
        this.thread.setDaemon(true);
    }

    public void start() {
        if (thread.getState() == Thread.State.NEW) {
            thread.start();
        }
    }

    public void stop() {
        running.set(false);
        resume();

        try {
            thread.join(300);
        } catch (final InterruptedException _) {
            Thread.currentThread().interrupt();
        }
    }

    public void pause() {
        paused.set(true);
    }

    public void resume() {
        paused.set(false);
        synchronized (pauseLock) {
            pauseLock.notifyAll();
        }
    }

    public void setPriority(final int priority) {
        thread.setPriority(Math.min(
            Thread.MAX_PRIORITY,
            Math.max(Thread.MIN_PRIORITY, priority)
        ));
    }

    public boolean isPaused() {
        return paused.get();
    }

    public boolean isRunning() {
        return running.get();
    }

    @Override
    public final void run() {
        try {
            while (running.get()) {
                synchronized (pauseLock) {
                    while (paused.get() && running.get()) {
                        pauseLock.wait();
                    }
                }

                if (!running.get()) {
                    break;
                }

                final var now = System.currentTimeMillis();

                if (now - lastUpdateTime >= FRAME_INTERVAL_MS) {
                    executeBehavior(now);
                    lastUpdateTime = now;
                }

                Thread.yield();
            }
        } catch (final InterruptedException _) {
            Thread.currentThread().interrupt();
        }
    }

    private void executeBehavior(final long currentTime) {
        final long deltaTimeMs = lastUpdateTime > 0
            ? currentTime - lastUpdateTime
            : FRAME_INTERVAL_MS;
        lastUpdateTime = currentTime;

        final double deltaTimeSeconds = deltaTimeMs / 1e3;

        for (final var vehicle : vehiclesSupplier.get()) {
            if (vehicle.isArrived()) {
                continue;
            }

            if (!vehicle.hasTarget()) {
                if (isVehicleArrived(
                    vehicle.getAbsoluteX(),
                    vehicle.getAbsoluteY()
                )) {
                    vehicle.markArrived();
                } else {
                    final var targetPoint = getTargetPoint();

                    vehicle.setMovementSpeed(speed);
                    vehicle.setTarget(
                        targetPoint.getX(),
                        targetPoint.getY()
                    );
                }
            }

            if (vehicle.hasTarget() && !vehicle.isArrived()) {
                vehicle.move(deltaTimeSeconds);
            }
        }
    }

    protected abstract boolean isVehicleArrived(
        final double x,
        final double y
    );

    protected abstract Point2D getTargetPoint();
}