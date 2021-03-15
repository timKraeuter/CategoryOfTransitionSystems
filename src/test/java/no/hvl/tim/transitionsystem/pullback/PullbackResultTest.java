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

class PullbackResultTest implements TransitionSystemTestHelper {

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
        // State names have to be the following. Or should state names come from the synchronization?
        assertThat(getStateNamesForTS(resultSystem), is(Sets.newHashSet("l1/r1", "l2/r1")));
        assertThat(resultSystem.getTransitions().size(), is(2));
        final Transition trans1 = getTransitionForLabel(resultSystem, "<l2, r1>");
        final Transition trans2 = getTransitionForLabel(resultSystem, "<l1, r1>");
        // Check loop
        assertThat(trans1.getSource().getName(), is("l2/r1"));
        assertThat(trans1.getSource(), is(trans1.getTarget()));
        // Check normal transition
        assertThat(trans2.getSource().getName(), is("l1/r1"));
        assertThat(trans2.getTarget().getName(), is("l2/r1"));

        assertThat(resultSystem.printToString(), is("States:[l1/r1, l2/r1]\n" +
                "Transitions:\n" +
                "l1/r1 --<l1, r1>--> l2/r1,\n" +
                "l2/r1 --<l2, r1>--> l2/r1"));
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

    @Test
    void handshakeTest() {
        // Build left side transition system
        final State s1_1 = new State("1");
        final State s1_2 = new State("2");
        final State s1_3 = new State("3");
        final Transition a_trans = new Transition(s1_1, s1_2, "a");
        left.addTransition(a_trans);
        final Transition b_trans = new Transition(s1_2, s1_3, "b");
        left.addTransition(b_trans);
        final TransitionSystem left_ts = left.buildWithIdleTransitions();

        // Build right side transition system
        final State s2_2 = new State("2");
        final State s2_3 = new State("3");
        final State s2_4 = new State("4");
        final Transition sysb_b_trans = new Transition(s2_2, s2_3, "b");
        right.addTransition(sysb_b_trans);
        final Transition sysb_c_trans = new Transition(s2_3, s2_4, "c");
        right.addTransition(sysb_c_trans);
        final TransitionSystem right_ts = right.buildWithIdleTransitions();

        // Build middle
        final State m2 = new State("2");
        final State m3 = new State("3");
        final Transition middle_b_trans = new Transition(m2, m3, "b");
        middle.addTransition(middle_b_trans);
        final TransitionSystem middle_ts = middle.buildWithIdleTransitions();

        // Build left morphism
        final TSMorphism left_morphism = new TSMorphismBuilder()
                .source(left_ts)
                .target(middle_ts)
                .addTransitionMappingToIdle(a_trans, m2)
                .addTransitionMapping(b_trans, middle_b_trans)
                .buildWithIdleTransitions();

        // Build right morphism
        final TSMorphism right_morphism = new TSMorphismBuilder()
                .source(right_ts)
                .target(middle_ts)
                .addTransitionMappingToIdle(sysb_c_trans, m3)
                .addTransitionMapping(sysb_b_trans, middle_b_trans)
                .buildWithIdleTransitions();

        final PullbackResult result = PullbackResult.calculate(new Cospan(left_morphism, right_morphism));
        // source is the same system
        assertThat(result.getM1().getSource(), is(result.getM2().getSource()));
        // System has two states and transitions
        final TransitionSystem resultSystem = result.getM1().getSource();
        // State names have to be the following. Or should state names come from the synchronization?
        assertThat(getStateNamesForTS(resultSystem), is(Sets.newHashSet("1/2", "2/2", "3/3", "3/4")));
        assertThat(resultSystem.getTransitions().size(), is(7));
        final Transition trans1 = getTransitionForLabel(resultSystem, "<a, *>");
        final Transition trans2 = getTransitionForLabel(resultSystem, "<b, b>");
        final Transition trans3 = getTransitionForLabel(resultSystem, "<*, c>");
        // Check transitions
        assertThat(trans1.getSource().getName(), is("1/2"));
        assertThat(trans1.getTarget().getName(), is("2/2"));

        assertThat(trans2.getSource().getName(), is("2/2"));
        assertThat(trans2.getTarget().getName(), is("3/3"));

        assertThat(trans3.getSource().getName(), is("3/3"));
        assertThat(trans3.getTarget().getName(), is("3/4"));

        assertThat(resultSystem.printToString(), is("States:[1/2, 2/2, 3/3, 3/4]\n" +
                "Transitions:\n" +
                "1/2 --<a, *>--> 2/2,\n" +
                "2/2 --<b, b>--> 3/3,\n" +
                "1/2 --<*, *>--> 1/2,\n" +
                "2/2 --<*, *>--> 2/2,\n" +
                "3/3 --<*, c>--> 3/4,\n" +
                "3/3 --<*, *>--> 3/3,\n" +
                "3/4 --<*, *>--> 3/4"));
    }
}