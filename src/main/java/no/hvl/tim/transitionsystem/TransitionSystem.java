package no.hvl.tim.transitionsystem;

import com.google.common.base.Objects;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TransitionSystem {
    private Set<State> states;
    private Set<Transition> transitions;

    private static TransitionSystem emptySystemInstance;

    public static TransitionSystem emptySystem() {
        if (emptySystemInstance == null) {
            emptySystemInstance = new TransitionSystem(new HashSet<>(), new HashSet<>());
        }
        return emptySystemInstance;
    }

    public TransitionSystem(final Set<State> states, final Set<Transition> transitions) {
        this.states = states;
        this.transitions = transitions;
    }

    public Set<State> getStates() {
        return Collections.unmodifiableSet(states);
    }

    public Set<Transition> getTransitions() {
        return Collections.unmodifiableSet(transitions);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof TransitionSystem)) return false;
        final TransitionSystem that = (TransitionSystem) o;
        return Objects.equal(getStates(), that.getStates()) && Objects.equal(getTransitions(), that.getTransitions());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getStates(), getTransitions());
    }

    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("States:");
        final List<String> stateNames = this.states.stream().map(State::getName).collect(Collectors.toList());
        builder.append(stateNames);
        builder.append("\nTransitions:\n");
        final Iterator<Transition> it = transitions.iterator();
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
