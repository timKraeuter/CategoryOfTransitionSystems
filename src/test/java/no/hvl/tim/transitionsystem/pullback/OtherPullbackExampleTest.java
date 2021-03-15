package no.hvl.tim.transitionsystem.pullback;

import com.google.common.collect.Sets;
import no.hvl.tim.transitionsystem.State;
import no.hvl.tim.transitionsystem.TSMorphism;
import no.hvl.tim.transitionsystem.Transition;
import no.hvl.tim.transitionsystem.TransitionSystem;
import no.hvl.tim.transitionsystem.TransitionSystemTestHelper;
import no.hvl.tim.transitionsystem.builder.TSMorphismBuilder;
import no.hvl.tim.transitionsystem.builder.TransitionSystemBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class OtherPullbackExampleTest implements TransitionSystemTestHelper {

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
    void otherExample() {
        // Build left side transition system
        final State z0_l = new State("z0");
        final State z1_l = new State("z1");
        final State z2_l = new State("z2");
        final Transition e1_l = new Transition(z0_l, z1_l, "e1");
        final Transition e_l = new Transition(z1_l, z2_l, "e");
        left.addTransition(e1_l);
        left.addTransition(e_l);
        final TransitionSystem left_ts = left.buildWithIdleTransitions();

        // Build right side transition system
        final State z0_r = new State("z'0");
        final State z1_r = new State("z'1");
        final State z2_r = new State("z'2");
        final Transition e2_r = new Transition(z0_r, z1_r, "e2");
        final Transition e_r = new Transition(z1_r, z2_r, "e");
        right.addTransition(e2_r);
        right.addTransition(e_r);
        final TransitionSystem right_ts = right.buildWithIdleTransitions();

        // Build middle
        final State z01 = new State("z01");
        final State z2 = new State("z2");
        final Transition e = new Transition(z01, z2, "e");
        middle.addTransition(e);
        final TransitionSystem middle_ts = middle.buildWithIdleTransitions();

        // Build left morphism
        final TSMorphism left_morphism = new TSMorphismBuilder()
                .source(left_ts)
                .target(middle_ts)
                .addTransitionMapping(e_l, e)
                .addStateMapping(z0_l, z01)
                .addStateMapping(z1_l, z01)
                .buildWithIdleTransitions();

        // Build right morphism
        final TSMorphism right_morphism = new TSMorphismBuilder()
                .source(right_ts)
                .target(middle_ts)
                .addTransitionMapping(e_r, e)
                .addStateMapping(z0_r, z01)
                .addStateMapping(z1_r, z01)
                .buildWithIdleTransitions();

        final PullbackResult result = PullbackResult.calculate(new Cospan(left_morphism, right_morphism));
        // source is the same system
        assertThat(result.getM1().getSource(), is(result.getM2().getSource()));
        // Four states with the given names expected
        final TransitionSystem pullbackSystem = result.getM1().getSource();
        assertThat(
                getStateNamesForTS(pullbackSystem),
                is(Sets.newHashSet("z0/z'0", "z1/z'0", "z0/z'1", "z1/z'1", "z2/z'2")));
        assertThat(pullbackSystem.getTransitions().size(), is(11));
        // 6 Transitions
        expectTransitionWithLabelFromTo(pullbackSystem, "z0/z'0", "z1/z'0", "<e1, *>");
        expectTransitionWithLabelFromTo(pullbackSystem, "z0/z'0", "z0/z'1", "<*, e2>");
        expectTransitionWithLabelFromTo(pullbackSystem, "z0/z'1", "z1/z'1", "<e1, *>");
        expectTransitionWithLabelFromTo(pullbackSystem, "z1/z'0", "z1/z'1", "<*, e2>");
        // The following transition is missing in the picture!
        expectTransitionWithLabelFromTo(pullbackSystem, "z0/z'0", "z1/z'1", "<e1, e2>");
        expectTransitionWithLabelFromTo(pullbackSystem, "z1/z'1", "z2/z'2", "<e, e>");
        // 5 Idle Transitions
        expectTransitionWithLabelFromTo(pullbackSystem, "z0/z'0", "z0/z'0", "<*, *>");
        expectTransitionWithLabelFromTo(pullbackSystem, "z0/z'1", "z0/z'1", "<*, *>");
        expectTransitionWithLabelFromTo(pullbackSystem, "z1/z'0", "z1/z'0", "<*, *>");
        expectTransitionWithLabelFromTo(pullbackSystem, "z1/z'1", "z1/z'1", "<*, *>");
        expectTransitionWithLabelFromTo(pullbackSystem, "z2/z'2", "z2/z'2", "<*, *>");
    }
}
