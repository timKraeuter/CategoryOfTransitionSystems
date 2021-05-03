package no.hvl.tim.transitionsystem;

import com.google.common.base.Objects;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TransitionSystem {
    private final State startState;
    private final Set<State> states;
    private final Set<Transition> transitions;

    public TransitionSystem(final State startState, final Set<State> states, final Set<Transition> transitions) {
        assert states.contains(startState);
        this.startState = startState;
        this.states = states;
        this.transitions = transitions;
    }

    public State getStartState() {
        return this.startState;
    }

    public Set<State> getStates() {
        return Collections.unmodifiableSet(this.states);
    }

    public Set<Transition> getTransitions() {
        return Collections.unmodifiableSet(this.transitions);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TransitionSystem)) {
            return false;
        }
        final TransitionSystem that = (TransitionSystem) o;
        return Objects.equal(this.getStates(), that.getStates()) && Objects.equal(this.getTransitions(), that.getTransitions());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.getStates(), this.getTransitions());
    }

    @Override
    public String toString() {
        final var builder = new StringBuilder();
        builder.append("States:");
        final List<String> stateNames = this.states.stream().map(State::getName).collect(Collectors.toList());
        builder.append(stateNames);
        builder.append("\nTransitions:\n");
        final Iterator<Transition> it = this.transitions.iterator();
        if (it.hasNext()) {
            builder.append(it.next());
        }
        while (it.hasNext()) {
            builder.append(",\n");
            builder.append(it.next());
        }
        return builder.toString();
    }
}
