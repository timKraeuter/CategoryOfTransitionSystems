package no.hvl.tim.transitionsystem.pullback;

import no.hvl.tim.transitionsystem.*;
import no.hvl.tim.transitionsystem.builder.TSMorphismBuilder;
import no.hvl.tim.transitionsystem.builder.TransitionSystemBuilder;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Map;

public class PullbackResult {
    /**
     * Pattern for the composite names of the states in a pullback.
     */
    public static final String COMPOSITE_STATE_NAME_PATTERN = "%s/%s";

    /**
     * Pattern for the execution of parallel transitions in the pullback system.
     */
    public static final String TRANSITION_FORMAT = "<%s, %s>";

    private final TSMorphism m1;
    private final TSMorphism m2;

    public PullbackResult(final TSMorphism m1, final TSMorphism m2) {
        // Constraint 2
        assert m1.getSource() == m2.getSource();

        this.m1 = m1;
        this.m2 = m2;
    }

    public static Cospan calcCoordinationInterface(
            final TransitionSystem ts1,
            final TransitionSystem ts2,
            final Map<Transition, Transition> interactions) {
        if (!interactions.entrySet().stream().allMatch(transPair -> ts1.getTransitions().contains(transPair.getKey())
                && ts2.getTransitions().contains(transPair.getValue()))) {
            // TODO better exception.
            throw new RuntimeException("Interactions must be contained in transition systems!");
        }
        final TransitionSystemBuilder coordinationInterfaceBuilder = new TransitionSystemBuilder();
        final TSMorphismBuilder ts1MappingBuilder = new TSMorphismBuilder().source(ts1);
        final TSMorphismBuilder ts2MappingBuilder = new TSMorphismBuilder().source(ts2);
        // Start state
        final State startState = new State(String.format(
                COMPOSITE_STATE_NAME_PATTERN,
                ts1.getStartState().getName(),
                ts2.getStartState().getName()));
        coordinationInterfaceBuilder.startState(startState);
        // Synched transitions

        // Rest
        // TS not build yet but add mappings later
        // ts1MappingBuilder.addStateMapping(ts1.getStartState(), startState);
        // ts2MappingBuilder.addStateMapping(ts2.getStartState(), startState);
        return null;
    }

    public static PullbackResult calculate(final Cospan input) {
        final TransitionSystemBuilder pullbackBuilder = new TransitionSystemBuilder();
        final TSMorphismBuilder m1Builder = new TSMorphismBuilder().target(input.getI1().getSource());
        final TSMorphismBuilder m2Builder = new TSMorphismBuilder().target(input.getI2().getSource());

        // Determine states + state mappings
        final Pair<Map<State, State>, Map<State, State>> stateMappings = calcPullbackStates(input, pullbackBuilder);

        // Determine transitions + finalize mappings
        calcPullbackTransitions(input, stateMappings, pullbackBuilder, m1Builder, m2Builder);

        return new PullbackResult(m1Builder.build(), m2Builder.build());
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent") // must be present
    private static void calcPullbackTransitions(
            final Cospan input,
            final Pair<Map<State, State>, Map<State, State>> stateMappings,
            final TransitionSystemBuilder pullbackBuilder,
            final TSMorphismBuilder m1Builder,
            final TSMorphismBuilder m2Builder) {
        final Map<Transition, Transition> m1TransitionMap = new HashMap<>();
        final Map<Transition, Transition> m2TransitionMap = new HashMap<>();
        for (final Transition i1transition : input.getI1().getSource().getTransitions()) {
            for (final Transition i2transition : input.getI2().getSource().getTransitions()) {
                // We loop over the product of transitions, which has to be equalized now.

                // Include a transition-pair if they map to the same transition in the cospan.
                if (input.getI1().mapTransition(i1transition).equals(input.getI2().mapTransition(i2transition))) {
                    // could be made more efficient by iterating once and calculating 2 maps.
                    final State source = pullbackBuilder.getStates().stream()
                            .filter(state -> state.getName().equals(
                                    String.format(
                                            COMPOSITE_STATE_NAME_PATTERN,
                                            i1transition.getSource().getName(),
                                            i2transition.getSource().getName())))
                            .findFirst().get(); // has to be present
                    final State target = pullbackBuilder.getStates().stream()
                            .filter(state -> state.getName().equals(
                                    String.format(
                                            COMPOSITE_STATE_NAME_PATTERN,
                                            i1transition.getTarget().getName(),
                                            i2transition.getTarget().getName())))
                            .findFirst().get(); // has to be present;
                    final Transition pullbackTransition = new Transition(
                            source,
                            target,
                            String.format(TRANSITION_FORMAT, i1transition.getLabel(), i2transition.getLabel()));
                    pullbackBuilder.addTransition(pullbackTransition);
                    m1TransitionMap.put(pullbackTransition, i1transition);
                    m2TransitionMap.put(pullbackTransition, i2transition);
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
        m1TransitionMap.forEach(m1Builder::addTransitionMapping);
        m2TransitionMap.forEach(m2Builder::addTransitionMapping);
    }

    private static Pair<Map<State, State>, Map<State, State>> calcPullbackStates(
            final Cospan input,
            final TransitionSystemBuilder pullbackBuilder) {
        boolean foundStartState = false;

        final Map<State, State> m1_state_map = new HashMap<>();
        final Map<State, State> m2_state_map = new HashMap<>();
        for (final State i1state : input.getI1().getSource().getStates()) {
            for (final State i2state : input.getI2().getSource().getStates()) {
                // We loop over the product of states, which has to be equalized now.

                // Include a state if they map to the same state in in the cospan.
                if (input.getI1().mapState(i1state).equals(input.getI2().mapState(i2state))) {
                    // The condition guarantees commutativity of the pullback square for states.
                    final State pullbackState = new State(
                            String.format(
                                    COMPOSITE_STATE_NAME_PATTERN,
                                    i1state.getName(),
                                    i2state.getName()));
                    pullbackBuilder.addState(pullbackState);
                    m1_state_map.put(pullbackState, i1state);
                    m2_state_map.put(pullbackState, i2state);
                    // Setting the start state could be made smarter
                    if (!foundStartState && input.getI1().getSource().getStartState().equals(i1state)
                            && input.getI2().getSource().getStartState().equals(i2state)) {
                        foundStartState = true;
                        pullbackBuilder.startState(pullbackState);
                    }
                }
            }
        }
        if (!foundStartState) {
            throw new TransitionSystemException("Start state mappings do not match in pullback calculation!");
        }
        return Pair.of(m1_state_map, m2_state_map);
    }

    public TSMorphism getM1() {
        return this.m1;
    }

    public TSMorphism getM2() {
        return this.m2;
    }
}
