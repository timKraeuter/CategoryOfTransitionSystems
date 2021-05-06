package no.hvl.tim.transitionsystem;

import com.google.common.collect.Sets;
import no.hvl.tim.transitionsystem.pullback.PullbackResult;

import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public interface TransitionSystemTestHelper {

    default Set<String> getStateNamesForTS(final TransitionSystem system) {
        return system.getStates().stream()
                .map(State::getName)
                .collect(Collectors.toSet());
    }

    default void expectTransitionWithLabelFromTo(final TransitionSystem system, final String sourceName, final String targetName, final String labelname) {
        system.getTransitions()
                .stream()
                .filter(transition -> transition.getLabel().equals(labelname))
                .filter(transition -> transition.getSource().getName().equals(sourceName))
                .filter(transition -> transition.getTarget().getName().equals(targetName))
                .findAny()
                .orElseThrow(RuntimeException::new);
    }

    default Transition getTransitionForLabel(final TransitionSystem system, final String labelname) {
        return system.getTransitions()
                .stream()
                .filter(transition -> transition.getLabel().equals(labelname))
                .findAny()
                .orElseThrow(RuntimeException::new);
    }

    default void expectStateMapping(final TSMorphism morphism, final String fromStateName, final String toStateName) {
        morphism.getStateMapping().entrySet()
                .stream()
                .filter(stateStateEntry ->
                        stateStateEntry.getKey().getName().equals(fromStateName)
                                && stateStateEntry.getValue().getName().equals(toStateName))
                .findAny()
                .orElseThrow(RuntimeException::new);
    }

    default void checkTLPullback(PullbackResult result) {
        // source is the same system
        assertThat(result.getM1().getSource(), is(result.getM2().getSource()));
        // Four states with the given names expected
        final TransitionSystem pullbackSystem = result.getM1().getSource();
        assertThat(
                this.getStateNamesForTS(pullbackSystem),
                is(Sets.newHashSet("red/cross", "red-amber/wait", "green/wait", "amber/wait")));
        assertThat(pullbackSystem.getTransitions().size(), is(8));
        // 4 Transitions
        this.expectTransitionWithLabelFromTo(pullbackSystem, "red/cross", "red-amber/wait", "<turn red-amber, switch to wait>");
        this.expectTransitionWithLabelFromTo(pullbackSystem, "red-amber/wait", "green/wait", "<turn green, *>");
        this.expectTransitionWithLabelFromTo(pullbackSystem, "green/wait", "amber/wait", "<turn amber, *>");
        this.expectTransitionWithLabelFromTo(pullbackSystem, "amber/wait", "red/cross", "<turn red, switch to cross>");
        // 4 Idle Transitions
        this.expectTransitionWithLabelFromTo(pullbackSystem, "red/cross", "red/cross", "<*, *>");
        this.expectTransitionWithLabelFromTo(pullbackSystem, "red-amber/wait", "red-amber/wait", "<*, *>");
        this.expectTransitionWithLabelFromTo(pullbackSystem, "green/wait", "green/wait", "<*, *>");
        this.expectTransitionWithLabelFromTo(pullbackSystem, "amber/wait", "amber/wait", "<*, *>");
    }



    default void checkABCPullback(PullbackResult result) {
        // source is the same system
        assertThat(result.getM1().getSource(), is(result.getM2().getSource()));
        // Four states with the given names expected
        final TransitionSystem pullbackSystem = result.getM1().getSource();
        assertThat(
                this.getStateNamesForTS(pullbackSystem),
                is(Sets.newHashSet("A/B", "B/B", "C/C", "C/D")));
        assertThat(pullbackSystem.getTransitions().size(), is(7));
        // 3 Transitions
        this.expectTransitionWithLabelFromTo(pullbackSystem, "A/B", "B/B", "<ab, *>");
        this.expectTransitionWithLabelFromTo(pullbackSystem, "B/B", "C/C", "<bc, bc>");
        this.expectTransitionWithLabelFromTo(pullbackSystem, "C/C", "C/D", "<*, cd>");
        // 4 Idle Transitions
        this.expectTransitionWithLabelFromTo(pullbackSystem, "A/B", "A/B", "<*, *>");
        this.expectTransitionWithLabelFromTo(pullbackSystem, "B/B", "B/B", "<*, *>");
        this.expectTransitionWithLabelFromTo(pullbackSystem, "C/C", "C/C", "<*, *>");
        this.expectTransitionWithLabelFromTo(pullbackSystem, "C/D", "C/D", "<*, *>");
    }
}
