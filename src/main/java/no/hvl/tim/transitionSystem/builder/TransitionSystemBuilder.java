package no.hvl.tim.transitionSystem.builder;

import no.hvl.tim.transitionSystem.State;
import no.hvl.tim.transitionSystem.Transition;
import no.hvl.tim.transitionSystem.TransitionSystem;

import java.util.HashSet;
import java.util.Set;

/**
 * Erstellt einen unveränderliches Transitionssystem Schritt für Schritt.
 */
public class TransitionSystemBuilder {
    private final Set<State> states = new HashSet<>();
    private final Set<Transition> transitions = new HashSet<>();

    /**
     * Adds a state to the transition system which is build.
     */
    public TransitionSystemBuilder addState(final State state) {
        this.states.add(state);
        return this;
    }

    /**
     * Adds a transition alongside the transitions source and target state
     * to the transition system which is build.
     */
    public TransitionSystemBuilder addTransition(Transition transition) {
        this.states.add(transition.getSource());
        this.states.add(transition.getTarget());

        this.transitions.add(transition);
        return this;
    }

    public TransitionSystem build() {
        return new TransitionSystem(this.states, this.transitions);
    }

}
