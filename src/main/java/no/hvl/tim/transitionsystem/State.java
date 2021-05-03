package no.hvl.tim.transitionsystem;

import com.google.common.base.Objects;

/**
 * Desribes a state in a transition system.
 */
public class State {

    private final String name;

    public State(final String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof State)) {
            return false;
        }
        final var state = (State) o;
        return Objects.equal(this.getName(), state.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.getName());
    }
}
