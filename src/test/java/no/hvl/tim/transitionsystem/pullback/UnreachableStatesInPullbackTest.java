package no.hvl.tim.transitionsystem.pullback;

import com.google.common.collect.Sets;
import no.hvl.tim.transitionsystem.*;
import no.hvl.tim.transitionsystem.builder.TSMorphismBuilder;
import no.hvl.tim.transitionsystem.builder.TransitionSystemBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class UnreachableStatesInPullbackTest implements TransitionSystemTestHelper {

    TransitionSystemBuilder left;
    TransitionSystemBuilder right;
    TransitionSystemBuilder middle;

    @BeforeEach
    void setUp() {
        this.left = new TransitionSystemBuilder();
        this.right = new TransitionSystemBuilder();
        this.middle = new TransitionSystemBuilder();
    }

    @Test
    void pullbackUnreachable() {
        // Build left side transition system
        final State z1_l = new State("z1");
        final State z1_prime_l = new State("z1'");
        final Transition a_l = new Transition(z1_l, z1_prime_l, "a");
        final Transition b_l = new Transition(z1_l, z1_prime_l, "b");
        this.left.startState(z1_l)
                .addTransition(a_l)
                .addTransition(b_l);
        final TransitionSystem left_ts = this.left.buildWithIdleTransitions();

        // Build right side transition system
        final State z1_r = new State("z1");
        final State z1_prime_r = new State("z1'");
        final Transition a_r = new Transition(z1_r, z1_prime_r, "a");
        this.right.startState(z1_r)
                .addTransition(a_r);
        final TransitionSystem right_ts = this.right.buildWithIdleTransitions();

        // Build middle
        final State z1_m = new State("(z1, z1')");
        final Transition a_m = new Transition(z1_m, z1_m, "a");
        this.middle.startState(z1_m)
                .addTransition(a_m);
        final TransitionSystem middle_ts = this.middle.buildWithIdleTransitions();

        // Build left morphism
        final TSMorphism left_morphism = new TSMorphismBuilder()
                .source(left_ts)
                .target(middle_ts)
                .addTransitionMapping(a_l, a_m)
                .buildWithIdleTransitions();

        // Build right morphism
        final TSMorphism right_morphism = new TSMorphismBuilder()
                .source(right_ts)
                .target(middle_ts)
                .addTransitionMapping(a_r, a_m)
                .buildWithIdleTransitions();

        final PullbackResult result = PullbackResult.calculate(new Cospan(left_morphism, right_morphism));
        // source is the same system
        assertThat(result.getM1().getSource(), is(result.getM2().getSource()));
        // Four states with the given names expected
        final TransitionSystem pullbackSystem = result.getM1().getSource();
        System.out.println(pullbackSystem.toString());
        assertThat(
                this.getStateNamesForTS(pullbackSystem),
                is(Sets.newHashSet(
                        "(z1, z1)",
                        "(z1', z1')",
                        "(z1, z1')",
                        "(z1', z1)")));
        assertThat(pullbackSystem.getTransitions().size(), is(7));
        // 3 Transitions
        this.expectTransitionWithLabelFromTo(pullbackSystem, "(z1, z1)", "(z1', z1')", "<a, a>");
        this.expectTransitionWithLabelFromTo(pullbackSystem, "(z1, z1')", "(z1', z1')", "<b, *>");
        this.expectTransitionWithLabelFromTo(pullbackSystem, "(z1, z1)", "(z1', z1)", "<b, *>");
        // 4 Idle Transitions
        this.expectTransitionWithLabelFromTo(pullbackSystem, "(z1, z1)", "(z1, z1)", "<*, *>");
        this.expectTransitionWithLabelFromTo(pullbackSystem, "(z1', z1)", "(z1', z1)", "<*, *>");
        this.expectTransitionWithLabelFromTo(pullbackSystem, "(z1, z1')", "(z1, z1')", "<*, *>");
        this.expectTransitionWithLabelFromTo(pullbackSystem, "(z1', z1')", "(z1', z1')", "<*, *>");
    }

    @Test
    void pullbackUnreachableAdrian() { // Different less confusing naming scheme but the same as the other test above.
        // Build left side transition system
        final State z1_l = new State("1");
        final State z1_prime_l = new State("2");
        final Transition a_l = new Transition(z1_l, z1_prime_l, "a");
        final Transition b_l = new Transition(z1_l, z1_prime_l, "b");
        this.left.startState(z1_l)
                .addTransition(a_l)
                .addTransition(b_l);
        final TransitionSystem left_ts = this.left.buildWithIdleTransitions();

        // Build right side transition system
        final State z1_r = new State("3");
        final State z1_prime_r = new State("4");
        final Transition a_r = new Transition(z1_r, z1_prime_r, "a");
        this.right.startState(z1_r)
                .addTransition(a_r);
        final TransitionSystem right_ts = this.right.buildWithIdleTransitions();

        // Build middle
        final State z1_m = new State("(z1, z1')");
        final Transition a_m = new Transition(z1_m, z1_m, "a");
        this.middle.startState(z1_m)
                .addTransition(a_m);
        final TransitionSystem middle_ts = this.middle.buildWithIdleTransitions();

        // Build left morphism
        final TSMorphism left_morphism = new TSMorphismBuilder()
                .source(left_ts)
                .target(middle_ts)
                .addTransitionMapping(a_l, a_m)
                .buildWithIdleTransitions();

        // Build right morphism
        final TSMorphism right_morphism = new TSMorphismBuilder()
                .source(right_ts)
                .target(middle_ts)
                .addTransitionMapping(a_r, a_m)
                .buildWithIdleTransitions();

        final PullbackResult result = PullbackResult.calculate(new Cospan(left_morphism, right_morphism));
        // Four states with the given names expected
        final TransitionSystem pullbackSystem = result.getM1().getSource();
        assertThat(pullbackSystem.toString(), is("States:[(1, 3), (1, 4), (2, 3), (2, 4)]\n" +
                "Transitions:\n" +
                "(1, 3) --<a, a>--> (2, 4),\n" +
                "(1, 3) --<b, *>--> (2, 3),\n" +
                "(1, 4) --<b, *>--> (2, 4),\n" +
                "(1, 3) --<*, *>--> (1, 3),\n" +
                "(1, 4) --<*, *>--> (1, 4),\n" +
                "(2, 3) --<*, *>--> (2, 3),\n" +
                "(2, 4) --<*, *>--> (2, 4)"));
    }
}