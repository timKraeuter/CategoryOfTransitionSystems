package no.hvl.tim.transitionsystem;

import java.util.Collections;
import java.util.Map;

public class TSMorphism {

    private final TransitionSystem source;
    private final TransitionSystem target;
    private final Map<State, State> stateMapping;
    private final Map<Transition, Transition> transitionMapping;

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
        for (final Map.Entry<Transition, Transition> aTransitionMapping : this.transitionMapping.entrySet()) {
            final var sourceTransition = aTransitionMapping.getKey();
            final var targetTransition = aTransitionMapping.getValue();
            if (!this.mapState(sourceTransition.getSource()).equals(targetTransition.getSource())) {
                throw new TransitionSystemException(
                        String.format(
                                "State and transition mapping do not match for the source state of the transition %s! The transition is mapped to %s but the source state to %s",
                                sourceTransition,
                                targetTransition,
                                this.mapState(sourceTransition.getSource())));
            }
            if (!this.mapState(sourceTransition.getTarget()).equals(targetTransition.getTarget())) {
                throw new TransitionSystemException(
                        String.format(
                                "State and transition mapping do not match for the target state of the transition %s! The transition is mapped to %s but the target state to %s",
                                sourceTransition,
                                targetTransition,
                                this.mapState(sourceTransition.getTarget())));
            }
        }
    }

    public TransitionSystem getSource() {
        return this.source;
    }

    public TransitionSystem getTarget() {
        return this.target;
    }

    public Map<State, State> getStateMapping() {
        return Collections.unmodifiableMap(this.stateMapping);
    }

    public Map<Transition, Transition> getTransitionMapping() {
        return Collections.unmodifiableMap(this.transitionMapping);
    }

    public State mapState(final State state) {
        final var mappedState = this.stateMapping.get(state);
        if (mappedState == null) {
            throw new TransitionSystemException(
                    String.format(
                            "The state %s is not part of the state mapping. Only the following states are mapped %s",
                            state,
                            this.stateMapping.keySet()));
        }
        return mappedState;
    }

    public Transition mapTransition(final Transition transition) {
        final var mappedTransition = this.transitionMapping.get(transition);
        if (mappedTransition == null) {
            throw new TransitionSystemException(
                    String.format(
                            "The transition %s is not part of the transition mapping. Only the following transitions are mapped %s",
                            transition,
                            this.transitionMapping.keySet()));
        }
        return mappedTransition;
    }

    @Override
    public String toString() {
        final var builder = new StringBuilder();
        builder.append("From:\n")
               .append(this.source.toString())
               .append("\nTo:\n")
               .append(this.target.toString());

        builder.append("\nState mapping:");
        this.stateMapping.forEach((from, to) -> {
            builder.append("\n");
            builder.append(from);
            builder.append(" -> ");
            builder.append(to);
        });

        builder.append("\nTransition mapping:");
        this.transitionMapping.forEach((from, to) -> {
            builder.append("\n");
            builder.append("(");
            builder.append(from);
            builder.append(")");
            builder.append(" -> ");
            builder.append("(");
            builder.append(to);
            builder.append(")");
        });
        return builder.toString();
    }
}
