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

class TrafficLightPullbackExampleTest implements TransitionSystemTestHelper {

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
    void trafficLight() {
        // Build left side transition system
        final State go = new State("go");
        final State wait = new State("wait");
        final Transition turn_red = new Transition(go, wait, "turn red");
        final Transition turn_green = new Transition(wait, go, "turn green");
        left.addTransition(turn_red);
        left.addTransition(turn_green);
        final TransitionSystem left_ts = left.buildWithIdleTransitions();

        // Build right side transition system
        final State red = new State("red");
        final State red_amber = new State("red-amber");
        final State green = new State("green");
        final State amber = new State("amber");
        final Transition turn_red_amber = new Transition(red, red_amber, "turn red-amber");
        final Transition turn_greenTL = new Transition(red_amber, green, "turn green");
        final Transition turn_amber = new Transition(green, amber, "turn amber");
        final Transition turn_redTL = new Transition(amber, red, "turn red");
        right.addTransition(turn_red_amber);
        right.addTransition(turn_greenTL);
        right.addTransition(turn_amber);
        right.addTransition(turn_redTL);
        final TransitionSystem right_ts = right.buildWithIdleTransitions();

        // Build middle
        final State goRed = new State("go/red");
        final State wait_redAmber = new State("wait/red-amber");
        final Transition turnRed_turnRedAmber = new Transition(goRed, wait_redAmber, "<turn red, turn green>");
        final Transition turnGreen_turnRed = new Transition(wait_redAmber, goRed, "<turn green, turn amber>");
        middle.addTransition(turnRed_turnRedAmber);
        middle.addTransition(turnGreen_turnRed);
        final TransitionSystem middle_ts = middle.buildWithIdleTransitions();

        // Build left morphism
        final TSMorphism left_morphism = new TSMorphismBuilder()
                .source(left_ts)
                .target(middle_ts)
                .addTransitionMapping(turn_red, turnRed_turnRedAmber)
                .addTransitionMapping(turn_green, turnGreen_turnRed)
                .buildWithIdleTransitions();

        // Build right morphism
        final TSMorphism right_morphism = new TSMorphismBuilder()
                .source(right_ts)
                .target(middle_ts)
                .addTransitionMapping(turn_red_amber, turnRed_turnRedAmber)
                .addStateMapping(red_amber, wait_redAmber)
                .addStateMapping(green, wait_redAmber)
                .addStateMapping(amber, wait_redAmber)
                .addTransitionMapping(turn_redTL, turnGreen_turnRed)
                .buildWithIdleTransitions();

        final PullbackResult result = PullbackResult.calculate(new Cospan(left_morphism, right_morphism));
        // source is the same system
        assertThat(result.getM1().getSource(), is(result.getM2().getSource()));
        // Four states with the given names expected
        final TransitionSystem pullbackSystem = result.getM1().getSource();
        assertThat(
                getStateNamesForTS(pullbackSystem),
                is(Sets.newHashSet("go/red", "wait/red-amber", "wait/green", "wait/amber")));
        assertThat(pullbackSystem.getTransitions().size(), is(8));
        // 4 Transitions
        expectTransitionWithLabelFromTo(pullbackSystem, "go/red", "wait/red-amber", "<turn red, turn red-amber>");
        expectTransitionWithLabelFromTo(pullbackSystem, "wait/red-amber", "wait/green", "<*, turn green>");
        expectTransitionWithLabelFromTo(pullbackSystem, "wait/green", "wait/amber", "<*, turn amber>");
        expectTransitionWithLabelFromTo(pullbackSystem, "wait/amber", "go/red", "<turn green, turn red>");
        // 4 Idle Transitions
        expectTransitionWithLabelFromTo(pullbackSystem, "go/red", "go/red", "<*, *>");
        expectTransitionWithLabelFromTo(pullbackSystem, "wait/red-amber", "wait/red-amber", "<*, *>");
        expectTransitionWithLabelFromTo(pullbackSystem, "wait/green", "wait/green", "<*, *>");
        expectTransitionWithLabelFromTo(pullbackSystem, "wait/amber", "wait/amber", "<*, *>");
    }
}
