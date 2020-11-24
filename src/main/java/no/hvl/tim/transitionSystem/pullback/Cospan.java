package no.hvl.tim.transitionSystem.pullback;

import no.hvl.tim.transitionSystem.TSMorphism;

public class Cospan {
    private final TSMorphism i1;
    private final TSMorphism i2;

    public Cospan(final TSMorphism i1, final TSMorphism i2) {
        // Constraint 1
        assert i1.getTarget() == i2.getTarget();
        
        this.i1 = i1;
        this.i2 = i2;
    }
}
