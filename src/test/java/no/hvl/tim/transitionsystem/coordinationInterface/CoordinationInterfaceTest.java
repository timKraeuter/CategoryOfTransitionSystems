package no.hvl.tim.transitionsystem.coordinationInterface;

import com.google.common.collect.Sets;
import no.hvl.tim.transitionsystem.State;
import no.hvl.tim.transitionsystem.Transition;
import no.hvl.tim.transitionsystem.TransitionSystem;
import no.hvl.tim.transitionsystem.TransitionSystemTestHelper;
import no.hvl.tim.transitionsystem.builder.TransitionSystemBuilder;
import no.hvl.tim.transitionsystem.pullback.Cospan;
import no.hvl.tim.transitionsystem.pullback.PullbackResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;


class CoordinationInterfaceTest implements TransitionSystemTestHelper {
    TransitionSystemBuilder right;
    TransitionSystemBuilder left;

    @BeforeEach
    void setUp() {
        this.right = new TransitionSystemBuilder();
        this.left = new TransitionSystemBuilder();
    }

    @Test
    void calcCoordinationInterfaceForTrafficLights() {
        // Build left side transition system
        final State red = new State("red");
        final State red_amber = new State("red-amber");
        final State green = new State("green");
        final State amber = new State("amber");
        final Transition turn_red_amber = new Transition(red, red_amber, "turn red-amber");
        final Transition turn_green = new Transition(red_amber, green, "turn green");
        final Transition turn_amber = new Transition(green, amber, "turn amber");
        final Transition turn_red = new Transition(amber, red, "turn red");
        this.left.startState(red)
                .addTransition(turn_red_amber)
                .addTransition(turn_green)
                .addTransition(turn_amber)
                .addTransition(turn_red);
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

        // Need a Map with order, so we do not get undeterministic behavior.
        final Map<Transition, Transition> coordinationsPairs = new LinkedHashMap<>();
        coordinationsPairs.put(turn_red_amber, switch_to_wait);
        coordinationsPairs.put(turn_red, switch_to_cross);

        final Cospan coordinatedTSs = PullbackResult.calcCoordinationInterface(left_ts, right_ts, coordinationsPairs);
        final TransitionSystem coordinationSystem = coordinatedTSs.getI1().getTarget();
        assertThat(
                this.getStateNamesForTS(coordinationSystem),
                is(Sets.newHashSet("red/cross", "red-amber/wait")));
        assertThat(coordinationSystem.getTransitions().size(), is(4));
        // 2 Transitions (and 2 idle transitions)
        this.expectTransitionWithLabelFromTo(
                coordinationSystem,
                "red/cross",
                "red-amber/wait",
                "<turn red-amber, switch to wait>");
        this.expectTransitionWithLabelFromTo(
                coordinationSystem,
                "red-amber/wait",
                "red/cross",
                "<turn red, switch to cross>");

        // Check state mapping
        this.expectStateMapping(coordinatedTSs.getI1(), "red", "red/cross");
        this.expectStateMapping(coordinatedTSs.getI1(), "green", "red-amber/wait");
        this.expectStateMapping(coordinatedTSs.getI1(), "red-amber", "red-amber/wait");
        this.expectStateMapping(coordinatedTSs.getI1(), "amber", "red-amber/wait");

        this.expectStateMapping(coordinatedTSs.getI2(), "cross", "red/cross");
        this.expectStateMapping(coordinatedTSs.getI2(), "wait", "red-amber/wait");

        final PullbackResult pullbackResult = PullbackResult.calculate(coordinatedTSs);
        this.checkTLPullback(pullbackResult);
    }
}
