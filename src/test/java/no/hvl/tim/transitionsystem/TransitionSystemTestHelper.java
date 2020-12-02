package no.hvl.tim.transitionsystem;

import java.util.Set;
import java.util.stream.Collectors;

public interface TransitionSystemTestHelper {

    default Set<String> getStateNamesForTS(final TransitionSystem system) {
        return system.getStates().stream()
                     .map(State::getName)
                     .collect(Collectors.toSet());
    }

    default void expectTransitionWithLabelFromTo(TransitionSystem system, String sourceName, String targetName, String labelname) {
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
}
