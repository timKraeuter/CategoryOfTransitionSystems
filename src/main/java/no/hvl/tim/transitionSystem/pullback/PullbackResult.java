package no.hvl.tim.transitionSystem.pullback;

import no.hvl.tim.transitionSystem.State;
import no.hvl.tim.transitionSystem.TSMorphism;
import no.hvl.tim.transitionSystem.builder.TSMorphismBuilder;
import no.hvl.tim.transitionSystem.builder.TransitionSystemBuilder;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Map;

public class PullbackResult {
    private final TSMorphism m1;
    private final TSMorphism m2;

    public static PullbackResult calculate(Cospan input) {
        final TransitionSystemBuilder pullbackBuilder = new TransitionSystemBuilder();
        final TSMorphismBuilder m1Builder = new TSMorphismBuilder();
        final TSMorphismBuilder m2Builder = new TSMorphismBuilder();
        // Determine states + state mappings
        final Pair<Map<State, State>, Map<State, State>> stateMappings = calcPullbackStates(input, pullbackBuilder);
        // Determine transitions + transition mappings
        
        return new PullbackResult(m1Builder.build(), m2Builder.build());
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
                    // Guarantees commutativity of the pullback square
                    final State pullbackState = new State(i1state.getName() + "/" + i2state.getName());
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
