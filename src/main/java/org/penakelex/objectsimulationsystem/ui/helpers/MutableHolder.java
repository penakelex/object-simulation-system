package org.penakelex.objectsimulationsystem.ui.helpers;

public final class MutableHolder<T> {
    private T containedValue;

    public MutableHolder(final T initialValue) {
        this.containedValue = initialValue;
    }

    public T getContainedValue() {
        return containedValue;
    }

    public void setContainedValue(final T newValue) {
        this.containedValue = newValue;
    }
}