package no.hvl.tim.transitionSystem.pullback;

import no.hvl.tim.transitionSystem.TSMorphism;

public class PullbackResult {
    private final TSMorphism m1;
    private final TSMorphism m2;

    public PullbackResult(final TSMorphism m1, final TSMorphism m2) {
        // Constraint 2
        assert m1.getSource() == m2.getSource();

        this.m1 = m1;
        this.m2 = m2;
    }
}
