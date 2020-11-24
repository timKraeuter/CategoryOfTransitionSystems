package no.hvl.tim.transitionSystem.builder;

import no.hvl.tim.transitionSystem.State;
import no.hvl.tim.transitionSystem.TSMorphism;
import no.hvl.tim.transitionSystem.Transition;
import no.hvl.tim.transitionSystem.TransitionSystem;

import java.util.HashMap;
import java.util.Map;

/**
 * Ein Builder für unveränderliche Morphismen.
 */
public class TSMorphismBuilder {

    private TransitionSystem source;
    private TransitionSystem target;
    private final Map<State, State> stateMapping = new HashMap<>();
    private final Map<Transition, Transition> transitionMapping = new HashMap<>();

    public TSMorphismBuilder source(final TransitionSystem source) {
        this.source = source;
        return this;
    }

    public TSMorphismBuilder target(final TransitionSystem target) {
        this.target = target;
        return this;
    }

    public TSMorphismBuilder addStateMapping(final State from, final State to) {
        assert this.source != null;
        assert this.target != null;
        assert this.source.getStates().contains(from)
                : "The from state has to be contained in the states of the source ts!";
        assert this.target.getStates().contains(to)
                : "The to state has to be contained in the states of the target ts!";
        // No mapping or the one which should be set.
        assert this.stateMapping.get(from) == null || this.stateMapping.get(from) == to;

        this.stateMapping.put(from, to);
        return this;
    }

    /**
     * Maps to transitions into each other. Automatically adds the source and target mapping for the states.
     */
    public TSMorphismBuilder addTransitionMapping(final Transition from, final Transition to) {
        assert this.source != null;
        assert this.target != null;

        this.addStateMapping(from.getSource(), to.getSource());
        this.addStateMapping(from.getTarget(), to.getTarget());
        this.transitionMapping.put(from, to);
        return this;
    }

    public TSMorphism build() {
        assert this.source != null;
        assert this.target != null;
        return new TSMorphism(this.source, this.target, this.stateMapping, this.transitionMapping);
    }
}
