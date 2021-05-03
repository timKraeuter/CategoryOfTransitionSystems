package no.hvl.tim.transitionsystem.pullback;

import no.hvl.tim.transitionsystem.*;
import no.hvl.tim.transitionsystem.builder.TSMorphismBuilder;
import no.hvl.tim.transitionsystem.builder.TransitionSystemBuilder;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public class PullbackResult {
    /**
     * Pattern for the composite names of the states in a pullback.
     */
    public static final String STATE_NAME_PATTERN = "%s/%s";

    /**
     * Pattern for the execution of parallel transitions in the pullback system.
     */
    public static final String TRANSITION_NAME_FORMAT = "<%s, %s>";

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
        // Start state
        final State startState = new State(String.format(
                STATE_NAME_PATTERN,
                ts1.getStartState().getName(),
                ts2.getStartState().getName()));
        coordinationInterfaceBuilder.startState(startState);

        final Map<State, State> ts1StateMapping = new LinkedHashMap<>();
        final Map<State, State> ts2StateMapping = new LinkedHashMap<>();
        final Map<Transition, Transition> ts1TransitionMapping = new LinkedHashMap<>();
        final Map<Transition, Transition> ts2TransitionMapping = new LinkedHashMap<>();

        ts1StateMapping.put(ts1.getStartState(), startState);
        ts2StateMapping.put(ts2.getStartState(), startState);

        // Synched transitions
        interactions.forEach((Transition ts1Transition, Transition ts2Transition) -> {
            // we expect one transition to be synched with at most one other transition
            createdSynchedTransition(
                    ts1TransitionMapping,
                    ts2TransitionMapping,
                    ts1StateMapping,
                    ts2StateMapping,
                    ts1Transition,
                    ts2Transition,
                    coordinationInterfaceBuilder);
        });

        // Make the mapping total. We expect that all states are reachable.
        // ts1
        final Set<Transition> unmappedts1Transitions = new LinkedHashSet<>(ts1.getTransitions());
        unmappedts1Transitions.removeAll(interactions.keySet());
        finalizeTSMapping(unmappedts1Transitions, ts1StateMapping);

        // ts2
        final Set<Transition> unmappedts2Transitions = new LinkedHashSet<>(ts2.getTransitions());
        unmappedts2Transitions.removeAll(interactions.values());
        finalizeTSMapping(unmappedts2Transitions, ts2StateMapping);

        final TransitionSystem coordinationInterface = coordinationInterfaceBuilder.buildWithIdleTransitions();

        // Build morphisms
        final TSMorphismBuilder ts1MappingBuilder = new TSMorphismBuilder()
                .source(ts1)
                .target(coordinationInterface);
        ts1StateMapping.forEach(ts1MappingBuilder::addStateMapping);
        ts1TransitionMapping.forEach(ts1MappingBuilder::addTransitionMapping);

        final TSMorphismBuilder ts2MappingBuilder = new TSMorphismBuilder()
                .source(ts2)
                .target(coordinationInterface);
        ts2StateMapping.forEach(ts2MappingBuilder::addStateMapping);
        ts2TransitionMapping.forEach(ts2MappingBuilder::addTransitionMapping);

        return new Cospan(ts1MappingBuilder.buildWithIdleTransitions(), ts2MappingBuilder.buildWithIdleTransitions());
    }

    private static void finalizeTSMapping(
            final Set<Transition> unmappedts1Transitions,
            final Map<State, State> ts1StateMapping) {
        while (!unmappedts1Transitions.isEmpty()) {
            final Set<Transition> mappedTransitions = new LinkedHashSet<>();
            for (final Transition transition : unmappedts1Transitions) {
                final State sourceMapped = ts1StateMapping.get(transition.getSource());
                final State targetMapped = ts1StateMapping.get(transition.getTarget());
                if (sourceMapped != null && targetMapped != null && !sourceMapped.equals(targetMapped)) {
                    // TODO: think about what to do in this case.
                    throw new TransitionSystemException("Both states mapped and not similar!");
                }
                if (sourceMapped != null || targetMapped == null) {
                    // Map target to the same as source. Collapse the mapping
                    ts1StateMapping.put(transition.getTarget(), sourceMapped);
                    mappedTransitions.add(transition);
                }
                if (sourceMapped == null || targetMapped != null) {
                    // Map source to the same as target. Collapse the mapping
                    ts1StateMapping.put(transition.getSource(), targetMapped);
                    mappedTransitions.add(transition);
                }
            }
            assert !mappedTransitions.isEmpty(); // Otherwise we will never finish
            unmappedts1Transitions.removeAll(mappedTransitions);
        }
    }

    private static void createdSynchedTransition(
            final Map<Transition, Transition> ts1TransitionMapping,
            final Map<Transition, Transition> ts2TransitionMapping,
            final Map<State, State> ts1StateMapping,
            final Map<State, State> ts2StateMapping,
            final Transition ts1Transition,
            final Transition ts2Transition,
            final TransitionSystemBuilder coordinationInterfaceBuilder) {
        // Handle Source state
        createStateIfNeeded(
                ts1StateMapping,
                ts2StateMapping,
                ts1Transition.getSource(),
                ts2Transition.getSource(),
                coordinationInterfaceBuilder);
        // Handle target state
        createStateIfNeeded(
                ts1StateMapping,
                ts2StateMapping,
                ts1Transition.getTarget(),
                ts2Transition.getTarget(),
                coordinationInterfaceBuilder);
        // Handle transition
        final Transition combinedTransition = new Transition(
                // Since the states are mapped to the same state for ts1 and ts2 we just take ts1 here.
                ts1StateMapping.get(ts1Transition.getSource()),
                ts1StateMapping.get(ts1Transition.getTarget()),
                String.format(TRANSITION_NAME_FORMAT, ts1Transition.getLabel(), ts2Transition.getLabel()));
        coordinationInterfaceBuilder.addTransition(combinedTransition);

        ts1TransitionMapping.put(ts1Transition, combinedTransition);
        ts2TransitionMapping.put(ts2Transition, combinedTransition);
    }

    private static void createStateIfNeeded(
            final Map<State, State> ts1StateMapping,
            final Map<State, State> ts2StateMapping,
            final State ts1State,
            final State ts2State,
            final TransitionSystemBuilder coordinationInterfaceBuilder) {
        final State ts1EquivalentState = ts1StateMapping.get(ts1State);
        final State ts2EquivalentState = ts2StateMapping.get(ts2State);
        if (ts1EquivalentState == null && ts2EquivalentState == null) {
            final State compositeState = new State(String.format(
                    STATE_NAME_PATTERN,
                    ts1State.getName(),
                    ts2State.getName()));
            ts1StateMapping.put(ts1State, compositeState);
            ts2StateMapping.put(ts2State, compositeState);
            coordinationInterfaceBuilder.addState(compositeState);
        }
        if (ts1EquivalentState != null && ts2EquivalentState != null && !ts1EquivalentState.equals(ts2EquivalentState)) {
            throw new TransitionSystemException("Incompatible mapping detected!");
        }
        if (ts1EquivalentState != null && ts2EquivalentState == null) {
            ts2StateMapping.put(ts2State, ts1EquivalentState);
        }
        if (ts1EquivalentState == null && ts2EquivalentState != null) {
            ts1StateMapping.put(ts1State, ts2EquivalentState);
        }
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
        final Map<Transition, Transition> m1TransitionMap = new LinkedHashMap<>();
        final Map<Transition, Transition> m2TransitionMap = new LinkedHashMap<>();
        for (final Transition i1transition : input.getI1().getSource().getTransitions()) {
            for (final Transition i2transition : input.getI2().getSource().getTransitions()) {
                // We loop over the product of transitions, which has to be equalized now.

                // Include a transition-pair if they map to the same transition in the cospan.
                if (input.getI1().mapTransition(i1transition).equals(input.getI2().mapTransition(i2transition))) {
                    // could be made more efficient by iterating once and calculating 2 maps.
                    final State source = pullbackBuilder.getStates().stream()
                            .filter(state -> state.getName().equals(
                                    String.format(
                                            STATE_NAME_PATTERN,
                                            i1transition.getSource().getName(),
                                            i2transition.getSource().getName())))
                            .findFirst().get(); // has to be present
                    final State target = pullbackBuilder.getStates().stream()
                            .filter(state -> state.getName().equals(
                                    String.format(
                                            STATE_NAME_PATTERN,
                                            i1transition.getTarget().getName(),
                                            i2transition.getTarget().getName())))
                            .findFirst().get(); // has to be present;
                    final Transition pullbackTransition = new Transition(
                            source,
                            target,
                            String.format(TRANSITION_NAME_FORMAT, i1transition.getLabel(), i2transition.getLabel()));
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
                                    STATE_NAME_PATTERN,
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
