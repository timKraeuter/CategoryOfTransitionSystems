package no.hvl.tim.transitionsystem.pullback;

import no.hvl.tim.transitionsystem.TSMorphism;

public class Cospan {
    private final TSMorphism i1;
    private final TSMorphism i2;

    public Cospan(final TSMorphism i1, final TSMorphism i2) {
        // Constraint 1
        assert i1.getTarget().equals(i2.getTarget());

        this.i1 = i1;
        this.i2 = i2;
    }

    public TSMorphism getI1() {
        return i1;
    }

    public TSMorphism getI2() {
        return i2;
    }
}
