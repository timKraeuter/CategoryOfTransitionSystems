package no.hvl.tim.transitionsystem.coordinationInterface;

import no.hvl.tim.transitionsystem.State;
import no.hvl.tim.transitionsystem.Transition;
import no.hvl.tim.transitionsystem.TransitionSystem;
import no.hvl.tim.transitionsystem.builder.TransitionSystemBuilder;
import no.hvl.tim.transitionsystem.pullback.Cospan;
import no.hvl.tim.transitionsystem.pullback.PullbackResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;


public class CoordinationInterfaceTest {
    TransitionSystemBuilder right;
    TransitionSystemBuilder left;

    @BeforeEach
    void setUp() {
        this.right = new TransitionSystemBuilder();
        this.left = new TransitionSystemBuilder();
    }

    @Test
    void trafficLight() {
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
        final Transition switch_to_cross = new Transition(cross, wait, "switch to cross");
        final Transition switch_to_wait = new Transition(wait, cross, "switch to wait");
        this.right.startState(cross)
                .addTransition(switch_to_cross)
                .addTransition(switch_to_wait);
        final TransitionSystem right_ts = this.right.buildWithIdleTransitions();


        final Map<Transition, Transition> coordinationsPairs = Map.of(turn_red, switch_to_cross, turn_red_amber, switch_to_wait);

        final Cospan coordinatedTSs = PullbackResult.calcCoordinationInterface(left_ts, right_ts, coordinationsPairs);
    }
}
