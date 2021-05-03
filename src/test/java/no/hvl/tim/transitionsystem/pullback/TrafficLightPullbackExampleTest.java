package no.hvl.tim.transitionsystem.pullback;

import com.google.common.collect.Sets;
import no.hvl.tim.transitionsystem.*;
import no.hvl.tim.transitionsystem.builder.TSMorphismBuilder;
import no.hvl.tim.transitionsystem.builder.TransitionSystemBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class TrafficLightPullbackExampleTest implements TransitionSystemTestHelper {

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
    void trafficLight() {
        // Build left side transition system
        final State cross = new State("cross");
        final State wait = new State("wait");
        final Transition switch_to_cross = new Transition(wait, cross, "switch to cross");
        final Transition switch_to_wait = new Transition(cross, wait, "switch to wait");
        this.left.startState(cross)
                .addTransition(switch_to_cross)
                .addTransition(switch_to_wait);
        final TransitionSystem left_ts = this.left.buildWithIdleTransitions();

        // Build right side transition system
        final State red = new State("red");
        final State red_amber = new State("red-amber");
        final State green = new State("green");
        final State amber = new State("amber");
        final Transition turn_red_amber = new Transition(red, red_amber, "turn red-amber");
        final Transition turn_greenTL = new Transition(red_amber, green, "turn green");
        final Transition turn_amber = new Transition(green, amber, "turn amber");
        final Transition turn_redTL = new Transition(amber, red, "turn red");
        this.right.startState(red)
                .addTransition(turn_red_amber)
                .addTransition(turn_greenTL)
                .addTransition(turn_amber)
                .addTransition(turn_redTL);
        final TransitionSystem right_ts = this.right.buildWithIdleTransitions();

        // Build middle
        final State cross_Red = new State("cross/red");
        final State wait_redAmber = new State("wait/red-amber");
        final Transition stw_turnRedAmber = new Transition(cross_Red, wait_redAmber, "<switch to wait, turn red-amber>");
        final Transition stc_turnRed = new Transition(wait_redAmber, cross_Red, "<switch to cross, turn red>");
        this.middle.startState(cross_Red)
                .addTransition(stw_turnRedAmber)
                .addTransition(stc_turnRed);
        final TransitionSystem middle_ts = this.middle.buildWithIdleTransitions();

        // Build left morphism
        final TSMorphism left_morphism = new TSMorphismBuilder()
                .source(left_ts)
                .target(middle_ts)
                .addTransitionMapping(switch_to_wait, stw_turnRedAmber)
                .addTransitionMapping(switch_to_cross, stc_turnRed)
                .buildWithIdleTransitions();

        // Build right morphism
        final TSMorphism right_morphism = new TSMorphismBuilder()
                .source(right_ts)
                .target(middle_ts)
                .addTransitionMapping(turn_red_amber, stw_turnRedAmber)
                .addStateMapping(red_amber, wait_redAmber)
                .addStateMapping(green, wait_redAmber)
                .addStateMapping(amber, wait_redAmber)
                .addTransitionMapping(turn_redTL, stc_turnRed)
                .buildWithIdleTransitions();

        final PullbackResult result = PullbackResult.calculate(new Cospan(left_morphism, right_morphism));
        // source is the same system
        assertThat(result.getM1().getSource(), is(result.getM2().getSource()));
        // Four states with the given names expected
        final TransitionSystem pullbackSystem = result.getM1().getSource();
        assertThat(
                this.getStateNamesForTS(pullbackSystem),
                is(Sets.newHashSet("cross/red", "wait/red-amber", "wait/green", "wait/amber")));
        assertThat(pullbackSystem.getTransitions().size(), is(8));
        // 4 Transitions
        this.expectTransitionWithLabelFromTo(pullbackSystem, "cross/red", "wait/red-amber", "<switch to wait, turn red-amber>");
        this.expectTransitionWithLabelFromTo(pullbackSystem, "wait/red-amber", "wait/green", "<*, turn green>");
        this.expectTransitionWithLabelFromTo(pullbackSystem, "wait/green", "wait/amber", "<*, turn amber>");
        this.expectTransitionWithLabelFromTo(pullbackSystem, "wait/amber", "cross/red", "<switch to cross, turn red>");
        // 4 Idle Transitions
        this.expectTransitionWithLabelFromTo(pullbackSystem, "cross/red", "cross/red", "<*, *>");
        this.expectTransitionWithLabelFromTo(pullbackSystem, "wait/red-amber", "wait/red-amber", "<*, *>");
        this.expectTransitionWithLabelFromTo(pullbackSystem, "wait/green", "wait/green", "<*, *>");
        this.expectTransitionWithLabelFromTo(pullbackSystem, "wait/amber", "wait/amber", "<*, *>");
    }
}
