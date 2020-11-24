package no.hvl.tim.transitionSystem;

import java.util.HashSet;
import java.util.Set;

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
        return states;
    }

    public Set<Transition> getTransitions() {
        return transitions;
    }
}
