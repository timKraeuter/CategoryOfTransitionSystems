package no.hvl.tim.transitionSystem.pullback;

import no.hvl.tim.transitionSystem.State;
import no.hvl.tim.transitionSystem.TSMorphism;
import no.hvl.tim.transitionSystem.Transition;
import no.hvl.tim.transitionSystem.TransitionSystem;
import no.hvl.tim.transitionSystem.builder.TSMorphismBuilder;
import no.hvl.tim.transitionSystem.builder.TransitionSystemBuilder;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Map;

public class PullbackResult {
    /**
     * Pattern for the composite names of the states in a pullback.
     */
    public static final String compositeStateNamePattern = "%s/%s";

    /**
     * Pattern for the execution of parallel transitions in the pullback system.
     */
    public static final String transitionFormat = "<%s, %s>";

    private final TSMorphism m1;
    private final TSMorphism m2;

    public static PullbackResult calculate(Cospan input) {
        final TransitionSystemBuilder pullbackBuilder = new TransitionSystemBuilder();
        final TSMorphismBuilder m1Builder = new TSMorphismBuilder().target(input.getI1().getSource());
        final TSMorphismBuilder m2Builder = new TSMorphismBuilder().target(input.getI2().getSource());

        // Determine states + state mappings
        final Pair<Map<State, State>, Map<State, State>> stateMappings = calcPullbackStates(input, pullbackBuilder);

        // Determine transitions + finalize mappings
        calcPullbackTransitions(input, stateMappings, pullbackBuilder, m1Builder, m2Builder);

        return new PullbackResult(m1Builder.build(), m2Builder.build());
    }

    private static void calcPullbackTransitions(
            final Cospan input,
            final Pair<Map<State, State>, Map<State, State>> stateMappings,
            final TransitionSystemBuilder pullbackBuilder,
            final TSMorphismBuilder m1Builder,
            final TSMorphismBuilder m2Builder) {
        Map<Transition, Transition> m1_transition_map = new HashMap<>();
        Map<Transition, Transition> m2_transition_map = new HashMap<>();
        for (final Transition i1transition : input.getI1().getSource().getTransitions()) {
            for (final Transition i2transition : input.getI2().getSource().getTransitions()) {
                // We loop over the product of transitions, which has to be equalized now.

                // Include a transition-pair if they map to the same transition in the cospan.
                if (input.getI1().mapTransition(i1transition) == input.getI2().mapTransition(i2transition)) {
                    // could be made more efficient by iterating once and calculating 2 maps.
                    State source = pullbackBuilder.getStates().stream()
                                                  .filter(state -> state.getName().equals(
                                                          String.format(
                                                                  compositeStateNamePattern,
                                                                  i1transition.getSource().getName(),
                                                                  i2transition.getSource().getName())))
                                                  .findFirst().get(); // has to be present
                    State target = pullbackBuilder.getStates().stream()
                                                  .filter(state -> state.getName().equals(
                                                          String.format(
                                                                  compositeStateNamePattern,
                                                                  i1transition.getTarget().getName(),
                                                                  i2transition.getTarget().getName())))
                                                  .findFirst().get(); // has to be present;
                    final Transition pullbackTransition = new Transition(
                            source,
                            target,
                            String.format(transitionFormat, i1transition.getLabel(), i2transition.getLabel()));
                    pullbackBuilder.addTransition(pullbackTransition);
                    m1_transition_map.put(pullbackTransition, i1transition);
                    m2_transition_map.put(pullbackTransition, i2transition);
                }
            }
        }
        // Build PB-System
        final TransitionSystem pbSystem = pullbackBuilder.build();
        m1Builder.source(pbSystem);
        m2Builder.source(pbSystem);
        // Add state mappings for morphisms
        stateMappings.getKey().forEach(m1Builder::addStateMapping);
        stateMappings.getValue().forEach(m2Builder::addStateMapping);
        // Add transition mappings (checks if state mappings are compatible)
        m1_transition_map.forEach(m1Builder::addTransitionMapping);
        m2_transition_map.forEach(m2Builder::addTransitionMapping);
    }

    private static Pair<Map<State, State>, Map<State, State>> calcPullbackStates(
            final Cospan input,
            final TransitionSystemBuilder pullbackBuilder) {
        Map<State, State> m1_state_map = new HashMap<>();
        Map<State, State> m2_state_map = new HashMap<>();
        for (final State i1state : input.getI1().getSource().getStates()) {
            for (final State i2state : input.getI2().getSource().getStates()) {
                // We loop over the product of states, which has to be equalized now.

                // Include a state if they map to the same state in in the cospan.
                if (input.getI1().mapState(i1state) == input.getI2().mapState(i2state)) {
                    // The condition guarantees commutativity of the pullback square.
                    final State pullbackState = new State(
                            String.format(
                                    compositeStateNamePattern,
                                    i1state.getName(),
                                    i2state.getName()));
                    pullbackBuilder.addState(pullbackState);
                    m1_state_map.put(pullbackState, i1state);
                    m2_state_map.put(pullbackState, i2state);
                }
            }
        }
        return Pair.of(m1_state_map, m2_state_map);
    }

    public PullbackResult(final TSMorphism m1, final TSMorphism m2) {
        // Constraint 2
        assert m1.getSource() == m2.getSource();

        this.m1 = m1;
        this.m2 = m2;
    }

    public TSMorphism getM1() {
        return m1;
    }

    public TSMorphism getM2() {
        return m2;
    }
}
