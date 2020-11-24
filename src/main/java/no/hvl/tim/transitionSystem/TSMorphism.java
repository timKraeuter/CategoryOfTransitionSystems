package no.hvl.tim.transitionSystem;

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
    }

    public TransitionSystem getSource() {
        return source;
    }

    public TransitionSystem getTarget() {
        return target;
    }

    public Map<State, State> getStateMapping() {
        return stateMapping;
    }

    public Map<Transition, Transition> getTransitionMapping() {
        return transitionMapping;
    }
}
