package org.penakelex.objectsimulationsystem.model.ai;

import javafx.geometry.Point2D;
import org.penakelex.objectsimulationsystem.model.vehicle.Vehicle;

import java.awt.*;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Supplier;

public abstract sealed class BaseAI<T extends Vehicle>
    implements Runnable permits TruckAI, CarAI
{
    protected final Supplier<Iterator<T>> vehiclesSupplier;
    protected final double speed;

    private final Thread thread;
    private final Object pauseLock = new Object();
    private final AtomicBoolean running = new AtomicBoolean(true);

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
        final Supplier<Iterator<T>> vehiclesSupplier,
        final double speed
    ) {
        this.vehiclesSupplier = vehiclesSupplier;
        this.speed = speed;

        this.thread = new Thread(this, getClass().getSimpleName());
        thread.setDaemon(true);
    }

    public void start() {
        if (thread.getState() == Thread.State.NEW) {
            thread.start();
        } else {
            resume();
        }
    }

    public void resume() {
        running.set(true);
        lastUpdateTime = System.currentTimeMillis();

        synchronized (pauseLock) {
            pauseLock.notifyAll();
        }
    }

    public void pause() {
        running.set(false);
    }

    public void setPriority(final int priority) {
        thread.setPriority(Math.min(
            Thread.MAX_PRIORITY,
            Math.max(Thread.MIN_PRIORITY, priority)
        ));
    }

    public boolean isRunning() {
        return running.get();
    }

    @Override
    public final void run() {
        try {
            while (!Thread.interrupted()) {
                while (!running.get()) {
                    synchronized (pauseLock) {
                        pauseLock.wait();
                    }
                }

                final var now = System.currentTimeMillis();
                final var elapsed = now - lastUpdateTime;

                if (elapsed >= FRAME_INTERVAL_MS) {
                    executeBehavior(now);
                    lastUpdateTime = now;
                } else {
                    LockSupport.parkNanos(Math.max(
                        1_000_000L,
                        TimeUnit.MILLISECONDS
                            .toNanos(FRAME_INTERVAL_MS - elapsed)
                    ));
                }
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

        for (
            final var vehicle : (Iterable<T>) vehiclesSupplier::get
        ) {
            if (vehicle.isArrived()) {
                continue;
            }

            if (!vehicle.hasTarget()) {
                if (isVehicleArrivedAtSpawn(
                    vehicle.getRelativeX(),
                    vehicle.getRelativeY()
                )) {
                    vehicle.markArrived();
                    continue;
                } else {
                    final var targetPoint = getRelativeTargetPoint();

                    vehicle.setMovementSpeed(speed);
                    vehicle.setTarget(
                        targetPoint.getX(),
                        targetPoint.getY()
                    );
                }
            }

            vehicle.move(deltaTimeSeconds);
        }
    }

    protected abstract boolean isVehicleArrivedAtSpawn(
        final double relativeX,
        final double relativeY
    );

    protected abstract Point2D getRelativeTargetPoint();
}