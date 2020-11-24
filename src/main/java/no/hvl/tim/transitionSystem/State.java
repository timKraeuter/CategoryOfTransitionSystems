package no.hvl.tim.transitionSystem;

/**
 * Desribes a state in a transition system.
 */
public class State {

    private final String name;

    public State(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
