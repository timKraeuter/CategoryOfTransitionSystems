package no.hvl.tim.transitionsystem.pullback;

import no.hvl.tim.transitionsystem.*;
import no.hvl.tim.transitionsystem.builder.TSMorphismBuilder;
import no.hvl.tim.transitionsystem.builder.TransitionSystemBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class TrafficLightPullbackExampleTest implements TransitionSystemTestHelper {

    TransitionSystemBuilder right;
    TransitionSystemBuilder left;
    TransitionSystemBuilder middle;

    @BeforeEach
    void setUp() {
        this.right = new TransitionSystemBuilder();
        this.left = new TransitionSystemBuilder();
        this.middle = new TransitionSystemBuilder();
    }

    @Test
    void trafficLight() {
        // Build left side transition system
        final State red = new State("red");
        final State red_amber = new State("red-amber");
        final State green = new State("green");
        final State amber = new State("amber");
        final Transition turn_red_amber = new Transition(red, red_amber, "turn red-amber");
        final Transition turn_greenTL = new Transition(red_amber, green, "turn green");
        final Transition turn_amber = new Transition(green, amber, "turn amber");
        final Transition turn_redTL = new Transition(amber, red, "turn red");
        this.left.startState(red)
                .addTransition(turn_red_amber)
                .addTransition(turn_greenTL)
                .addTransition(turn_amber)
                .addTransition(turn_redTL);
        final TransitionSystem left_ts = this.left.buildWithIdleTransitions();

        // Build right side transition system
        final State cross = new State("cross");
        final State wait = new State("wait");
        final Transition switch_to_cross = new Transition(wait, cross, "switch to cross");
        final Transition switch_to_wait = new Transition(cross, wait, "switch to wait");
        this.right.startState(cross)
                .addTransition(switch_to_cross)
                .addTransition(switch_to_wait);
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

        // Build right morphism
        final TSMorphism right_morphism = new TSMorphismBuilder()
                .source(right_ts)
                .target(middle_ts)
                .addTransitionMapping(switch_to_wait, stw_turnRedAmber)
                .addTransitionMapping(switch_to_cross, stc_turnRed)
                .buildWithIdleTransitions();

        // Build left morphism
        final TSMorphism left_morphism = new TSMorphismBuilder()
                .source(left_ts)
                .target(middle_ts)
                .addTransitionMapping(turn_red_amber, stw_turnRedAmber)
                .addStateMapping(red_amber, wait_redAmber)
                .addStateMapping(green, wait_redAmber)
                .addStateMapping(amber, wait_redAmber)
                .addTransitionMapping(turn_redTL, stc_turnRed)
                .buildWithIdleTransitions();

        final PullbackResult result = PullbackResult.calculate(new Cospan(left_morphism, right_morphism));
        checkTLPullback(result);
    }
}
