package no.hvl.tim.transitionsystem;

/**
 * Desribes a transition from one state to another in a transition system.
 */
public class Transition {

    private final State source;
    private final State target;
    private final String label;

    public Transition(final State source, final State target, final String label) {
        this.source = source;
        this.target = target;
        this.label = label;
    }

    public State getSource() {
        return source;
    }

    public State getTarget() {
        return target;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return String.format("%s --%s--> %s", source, label, target);
    }
}
