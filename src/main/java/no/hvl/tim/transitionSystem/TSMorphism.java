package no.hvl.tim.transitionSystem;

import java.util.Collections;
import java.util.Map;

public class TSMorphism {

    private TransitionSystem source;
    private TransitionSystem target;
    private Map<State, State> stateMapping;
    private Map<Transition, Transition> transitionMapping;

    public TSMorphism(
            final TransitionSystem source,
            final TransitionSystem target,
            final Map<State, State> stateMapping,
            final Map<Transition, Transition> transitionMapping) {
        this.source = source;
        this.target = target;
        this.stateMapping = stateMapping;
        this.transitionMapping = transitionMapping;
        this.checkTotality();
        this.checkMappingCriteria();
    }

    private void checkTotality() {
        assert this.stateMapping.keySet().containsAll(this.source.getStates()) : "state mapping must be total";
        assert this.transitionMapping.keySet().containsAll(this.source.getTransitions()) : "transition mapping must be total";
    }

    private void checkMappingCriteria() {
        for (final Map.Entry<Transition, Transition> transitionMapping : transitionMapping.entrySet()) {
            final Transition sourceTransition = transitionMapping.getKey();
            final Transition targetTransition = transitionMapping.getValue();
            if (!this.mapState(sourceTransition.getSource()).equals(targetTransition.getSource())) {
                throw new RuntimeException(
                        String.format(
                                "State and transition mapping do not match for the source state of the transition %s! The transition is mapped to %s but the source state to %s",
                                sourceTransition,
                                targetTransition,
                                this.mapState(sourceTransition.getSource())));
            }
            if (!this.mapState(sourceTransition.getTarget()).equals(targetTransition.getTarget())) {
                throw new RuntimeException(
                        String.format(
                                "State and transition mapping do not match for the target state of the transition %s! The transition is mapped to %s but the target state to %s",
                                sourceTransition,
                                targetTransition,
                                this.mapState(sourceTransition.getTarget())));
            }
        }
    }

    public TransitionSystem getSource() {
        return source;
    }

    public TransitionSystem getTarget() {
        return target;
    }

    public Map<State, State> getStateMapping() {
        return Collections.unmodifiableMap(stateMapping);
    }

    public Map<Transition, Transition> getTransitionMapping() {
        return Collections.unmodifiableMap(transitionMapping);
    }

    public State mapState(State state) {
        final State mappedState = this.stateMapping.get(state);
        if (mappedState == null) {
            throw new RuntimeException(
                    String.format(
                            "The state %s is not part of the state mapping. Only the following states are mapped %s",
                            state,
                            stateMapping.keySet()));
        }
        return mappedState;
    }

    public Transition mapTransition(final Transition transition) {
        final Transition mappedTransition = this.transitionMapping.get(transition);
        if (mappedTransition == null) {
            throw new RuntimeException(
                    String.format(
                            "The transition %s is not part of the transition mapping. Only the following transitions are mapped %s",
                            transition,
                            transitionMapping.keySet()));
        }
        return mappedTransition;
    }
}
