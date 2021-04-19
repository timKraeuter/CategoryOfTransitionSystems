package no.hvl.tim.transitionsystem.builder;

import no.hvl.tim.transitionsystem.State;
import no.hvl.tim.transitionsystem.TSMorphism;
import no.hvl.tim.transitionsystem.Transition;
import no.hvl.tim.transitionsystem.TransitionSystem;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
        assert this.stateMapping.get(from) == null || this.stateMapping.get(from) == to
                : String.format("The state %s was expected to be %s.", to, this.stateMapping.get(from));

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

    /**
     * Maps to transitions into each other. Automatically adds the source and target mapping for the states.
     */
    public TSMorphismBuilder addTransitionMappingToIdle(final Transition from, final State idleState) {
        assert this.source != null;
        assert this.target != null;

        final Transition idleTransitionForState = this.findIdleTransitionForState(idleState, this.target);
        this.addTransitionMapping(from, idleTransitionForState);
        return this;
    }

    public TSMorphism buildWithIdleTransitions() {
        assert this.source != null;
        assert this.target != null;

        // State mapping has to be total
        assert this.stateMapping.keySet().containsAll(this.source.getStates());

        // Automatically map idle transitions to idle transitions
        this.stateMapping.forEach((sourceState, targetState) ->
                this.transitionMapping.put(
                        this.findIdleTransitionForState(sourceState, source),
                        this.findIdleTransitionForState(targetState, target))
        );
        // Automatically map undefined transitions to idle transitions
        final Set<Transition> mappedTransitions = this.transitionMapping.keySet();
        this.source.getTransitions().stream()
                   .filter(transition -> !mappedTransitions.contains(transition))
                   .forEach(unmappedTransition -> {
                       final Transition idleTranstionInTheTargetSystemState = this.findIdleTransitionForState(
                               this.stateMapping.get(unmappedTransition.getSource()), target);
                       this.transitionMapping.put(
                               unmappedTransition,
                               idleTranstionInTheTargetSystemState);
                   });

        return new TSMorphism(this.source, this.target, this.stateMapping, this.transitionMapping);
    }

    private Transition findIdleTransitionForState(final State stateOfSystem, final TransitionSystem system) {
        return system.getTransitions().stream()
                     .filter(transition -> transition.getSource().equals(stateOfSystem) && transition.getLabel().equals("*"))
                     .findAny()
                     .orElseThrow(RuntimeException::new);
    }

    public TSMorphism build() {
        assert this.source != null;
        assert this.target != null;

        // State and transition mapping has to be total
        assert this.stateMapping.keySet().containsAll(this.source.getStates());
        assert this.transitionMapping.keySet().containsAll(this.source.getTransitions());

        return new TSMorphism(this.source, this.target, this.stateMapping, this.transitionMapping);
    }
}
