package no.hvl.tim.transitionSystem.pullback;

import no.hvl.tim.transitionSystem.State;
import no.hvl.tim.transitionSystem.TSMorphism;
import no.hvl.tim.transitionSystem.Transition;
import no.hvl.tim.transitionSystem.TransitionSystem;
import no.hvl.tim.transitionSystem.builder.TSMorphismBuilder;
import no.hvl.tim.transitionSystem.builder.TransitionSystemBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class PullbackResultTest {

    TransitionSystemBuilder left;
    TransitionSystemBuilder right;
    TransitionSystemBuilder middle;

    @BeforeEach
    void setUp() {
        left = new TransitionSystemBuilder();
        right = new TransitionSystemBuilder();
        middle = new TransitionSystemBuilder();
    }

    @Test
    void firstTest() {
        // Build left side transition system
        final State l2 = new State("l2");
        final Transition l1_trans = new Transition(new State("l1"), l2, "l1");
        left.addTransition(l1_trans);
        final Transition l2_loop = new Transition(l2, l2, "l2");
        left.addTransition(l2_loop);
        final TransitionSystem left_ts = left.build();

        // Build right side transition system
        final State r1 = new State("r1");
        final Transition r1_loop = new Transition(r1, r1, "r1");
        right.addTransition(r1_loop);
        final TransitionSystem right_ts = right.build();

        // Build middle
        final State m1 = new State("m1");
        final Transition middle_loop = new Transition(m1, m1, "m1");
        middle.addTransition(middle_loop);
        final TransitionSystem middle_ts = middle.build();

        // Build left morphism
        final TSMorphism left_morphism = new TSMorphismBuilder()
                .source(left_ts)
                .target(middle_ts)
                .addTransitionMapping(l1_trans, middle_loop)
                .addTransitionMapping(l2_loop, middle_loop)
                .build();

        // Build right morphism
        final TSMorphism right_morphism = new TSMorphismBuilder()
                .source(right_ts)
                .target(middle_ts)
                .addTransitionMapping(r1_loop, middle_loop)
                .build();

        final PullbackResult result = PullbackResult.calculate(new Cospan(left_morphism, right_morphism));
        // source is the same system
        assertThat(result.getM1().getSource(), is(result.getM2().getSource()));
        // System has two states and transitions
        final TransitionSystem resultSystem = result.getM1().getSource();
        assertThat(resultSystem.getStates().size(), is(2));
        assertThat(resultSystem.getTransitions().size(), is(2));
        // TODO Check for one loop and one normal transition. We need labels for this
    }

    @Test
    void emptyTest() {
        // Build left side transition system
        final TransitionSystem left_ts = left.build();

        // Build right side transition system
        final State r1 = new State("r1");
        final Transition r1_loop = new Transition(r1, r1, "r1");
        right.addTransition(r1_loop);
        final TransitionSystem right_ts = right.build();

        // Build middle
        final State m1 = new State("m1");
        final Transition middle_loop = new Transition(m1, m1, "m1");
        middle.addTransition(middle_loop);
        final TransitionSystem middle_ts = middle.build();

        // Build left morphism
        final TSMorphism left_morphism = new TSMorphismBuilder()
                .source(left_ts)
                .target(middle_ts)
                .build();

        // Build right morphism
        final TSMorphism right_morphism = new TSMorphismBuilder()
                .source(right_ts)
                .target(middle_ts)
                .addTransitionMapping(r1_loop, middle_loop)
                .build();

        final PullbackResult result = PullbackResult.calculate(new Cospan(left_morphism, right_morphism));
        // source is the same system
        assertThat(result.getM1().getSource(), is(result.getM2().getSource()));
        // System is empty
        final TransitionSystem resultSystem = result.getM1().getSource();
        assertThat(resultSystem.getStates().size(), is(0));
        assertThat(resultSystem.getTransitions().size(), is(0));

    }
}