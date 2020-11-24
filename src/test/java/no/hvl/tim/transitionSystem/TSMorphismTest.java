package no.hvl.tim.transitionSystem;

import com.google.common.collect.Lists;
import no.hvl.tim.transitionSystem.builder.TSMorphismBuilder;
import no.hvl.tim.transitionSystem.builder.TransitionSystemBuilder;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TSMorphismTest {

    @Test
    public void builderEmpty() {
        final TransitionSystemBuilder targetBuilder = new TransitionSystemBuilder();

        final TransitionSystem target = targetBuilder.build();

        final TSMorphism morphism = new TSMorphismBuilder()
                .source(TransitionSystem.emptySystem())
                .target(target)
                .build();

        assertThat(morphism.getSource(), is(TransitionSystem.emptySystem()));
        assertThat(morphism.getTarget(), is(target));
    }

    @Test
    public void mappingTransitionsAddsStatesAutomatically() {
        final State s1 = new State("s1");
        final State s2 = new State("s2");
        final State t1 = new State("t1");
        final State t2 = new State("t2");

        final TransitionSystemBuilder sourceBuilder = new TransitionSystemBuilder();
        final Transition trans1 = new Transition(s1, s2, "a");
        sourceBuilder.addTransition(trans1);
        final TransitionSystemBuilder targetBuilder = new TransitionSystemBuilder();
        final Transition trans2 = new Transition(t1, t2, "a");
        targetBuilder.addTransition(trans2);

        final TransitionSystem source = sourceBuilder.build();
        assertTrue(source.getStates().containsAll(Lists.newArrayList(s1, s2)));

        final TransitionSystem target = targetBuilder.build();

        final TSMorphism morphism = new TSMorphismBuilder()
                .source(source)
                .target(target)
                .addTransitionMapping(trans1, trans2)
                .build();

        assertThat(morphism.getSource(), is(source));
        assertThat(morphism.getTarget(), is(target));

        assertThat(morphism.getStateMapping().size(), is(2));
        assertThat(morphism.getTransitionMapping().size(), is(1));

        assertThat(morphism.getStateMapping().get(trans1.getSource()), is(trans2.getSource()));
        assertThat(morphism.getStateMapping().get(trans1.getTarget()), is(trans2.getTarget()));

        assertThat(morphism.getTransitionMapping().get(trans1), is(trans2));
    }
}