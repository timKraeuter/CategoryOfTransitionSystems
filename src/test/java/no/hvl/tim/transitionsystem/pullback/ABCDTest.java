package no.hvl.tim.transitionsystem.pullback;

import no.hvl.tim.transitionsystem.*;
import no.hvl.tim.transitionsystem.builder.TSMorphismBuilder;
import no.hvl.tim.transitionsystem.builder.TransitionSystemBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * No Real tests just playing around with the synchronisation when 3 systems are targeted.
 */
class ABCDTest implements TransitionSystemTestHelper {

    TransitionSystemBuilder left;
    TransitionSystemBuilder right;
    TransitionSystemBuilder middle;

    @BeforeEach
    void setUp() {
        this.left = new TransitionSystemBuilder();
        this.right = new TransitionSystemBuilder();
        this.middle = new TransitionSystemBuilder();
    }

    @Test
    void abc() {
        // Build left side transition system
        final State a = new State("a");
        final State bl = new State("b");
        final State cl = new State("c");
        final Transition abl = new Transition(a, bl, "ab");
        final Transition blcl = new Transition(bl, cl, "bc");
        this.left.startState(a)
                .addTransition(abl)
                .addTransition(blcl);
        final TransitionSystem left_ts = this.left.buildWithIdleTransitions();

        // Build right side transition system
        final State br = new State("b");
        final State cr = new State("c");
        final State d = new State("d");
        final Transition brcr = new Transition(br, cr, "bc");
        final Transition crd = new Transition(cr, d, "cd");
        this.right.startState(br)
                .addTransition(brcr)
                .addTransition(crd);
        final TransitionSystem right_ts = this.right.buildWithIdleTransitions();

        // Build middle
        final State bm = new State("bm");
        final State cm = new State("cm");
        final Transition bmcm = new Transition(bm, cm, "bmcm");
        this.middle.startState(bm)
                .addTransition(bmcm);
        final TransitionSystem middle_ts = this.middle.buildWithIdleTransitions();

        // Build left morphism
        final TSMorphism left_morphism = new TSMorphismBuilder()
                .source(left_ts)
                .target(middle_ts)
                .addStateMapping(a, bm)
                .addTransitionMapping(blcl, bmcm)
                .buildWithIdleTransitions();

        // Build right morphism
        final TSMorphism right_morphism = new TSMorphismBuilder()
                .source(right_ts)
                .target(middle_ts)
                .addTransitionMapping(brcr, bmcm)
                .addStateMapping(d, cm)
                .buildWithIdleTransitions();

        final PullbackResult result = PullbackResult.calculate(new Cospan(left_morphism, right_morphism));
        // source is the same system
        assertThat(result.getM1().getSource(), is(result.getM2().getSource()));
        // Four states with the given names expected
        final TransitionSystem pullbackSystem = result.getM1().getSource();
    }

    @Test
    void bcd() {
        // Build left side transition system
        final State bl = new State("b");
        final State cl = new State("c");
        final State d = new State("d");
        final Transition blcl = new Transition(bl, cl, "bc");
        final Transition cldl = new Transition(cl, d, "cd");
        this.left.startState(bl)
                .addTransition(blcl)
                .addTransition(cldl);
        final TransitionSystem left_ts = this.left.buildWithIdleTransitions();

        // Build right side transition system
        final State cr = new State("c");
        final State dr = new State("d");
        final State er = new State("e");
        final Transition crdr = new Transition(cr, dr, "cd");
        final Transition drer = new Transition(dr, er, "de");
        this.right.startState(cr)
                .addTransition(crdr)
                .addTransition(drer);
        final TransitionSystem right_ts = this.right.buildWithIdleTransitions();

        // Build middle
        final State cm = new State("cm");
        final State dm = new State("dm");
        final Transition cmdm = new Transition(cm, dm, "cd");
        this.middle.startState(cm)
                .addTransition(cmdm);
        final TransitionSystem middle_ts = this.middle.buildWithIdleTransitions();

        // Build left morphism
        final TSMorphism left_morphism = new TSMorphismBuilder()
                .source(left_ts)
                .target(middle_ts)
                .addStateMapping(bl, cm)
                .addTransitionMapping(cldl, cmdm)
                .buildWithIdleTransitions();

        // Build right morphism
        final TSMorphism right_morphism = new TSMorphismBuilder()
                .source(right_ts)
                .target(middle_ts)
                .addTransitionMapping(crdr, cmdm)
                .addStateMapping(er, dm)
                .buildWithIdleTransitions();

        final PullbackResult result = PullbackResult.calculate(new Cospan(left_morphism, right_morphism));
        // source is the same system
        assertThat(result.getM1().getSource(), is(result.getM2().getSource()));
        // Four states with the given names expected
    }

    @Test
    void abcdCombined() {
        // Build left side transition system
        final State a = new State("a");
        final State bl = new State("b");
        final State cl = new State("c");
        final Transition abl = new Transition(a, bl, "ab");
        final Transition blcl = new Transition(bl, cl, "bc");
        this.left.startState(a)
                .addTransition(abl)
                .addTransition(blcl);
        final TransitionSystem left_ts = this.left.buildWithIdleTransitions();

        // Build right side transition system
        final State br = new State("b");
        final State cr = new State("c");
        final State d = new State("d");
        final Transition brcr = new Transition(br, cr, "bc");
        final Transition crd = new Transition(cr, d, "cd");
        this.right.startState(br)
                .addTransition(brcr)
                .addTransition(crd);
        final TransitionSystem right_ts = this.right.buildWithIdleTransitions();

        // Build middle
        final State bm = new State("bm");
        final State cm = new State("cm");
        final Transition bmcm = new Transition(bm, cm, "bmcm");
        this.middle.startState(bm)
                .addTransition(bmcm);
        final TransitionSystem middle_ts = this.middle.buildWithIdleTransitions();

        // Build left morphism
        final TSMorphism left_morphism = new TSMorphismBuilder()
                .source(left_ts)
                .target(middle_ts)
                .addStateMapping(a, bm)
                .addTransitionMapping(blcl, bmcm)
                .buildWithIdleTransitions();

        // Build right morphism
        final TSMorphism right_morphism = new TSMorphismBuilder()
                .source(right_ts)
                .target(middle_ts)
                .addTransitionMapping(brcr, bmcm)
                .addStateMapping(d, cm)
                .buildWithIdleTransitions();

        // Left side pullback
        final PullbackResult leftSide = PullbackResult.calculate(new Cospan(left_morphism, right_morphism));
        // Right side pullback
        final PullbackResult rightSide = this.getRightSide();

        // TODO not similar to the by hand one. D/E is missing
        final PullbackResult finalResult = PullbackResult.calculate(new Cospan(leftSide.getM2(), rightSide.getM1()));
    }

    private PullbackResult getRightSide() {
        this.setUp();

        // Build left side transition system
        final State bl = new State("b");
        final State cl = new State("c");
        final State d = new State("d");
        final Transition blcl = new Transition(bl, cl, "bc");
        final Transition cldl = new Transition(cl, d, "cd");
        this.left.startState(bl)
                .addTransition(blcl)
                .addTransition(cldl);
        final TransitionSystem left_ts = this.left.buildWithIdleTransitions();

        // Build right side transition system
        final State cr = new State("c");
        final State dr = new State("d");
        final State er = new State("e");
        final Transition crdr = new Transition(cr, dr, "cd");
        final Transition drer = new Transition(dr, er, "de");
        this.right.startState(cr)
                .addTransition(crdr)
                .addTransition(drer);
        final TransitionSystem right_ts = this.right.buildWithIdleTransitions();

        // Build middle
        final State cm = new State("cm");
        final State dm = new State("dm");
        final Transition cmdm = new Transition(cm, dm, "cd");
        this.middle.startState(cm)
                .addTransition(cmdm);
        final TransitionSystem middle_ts = this.middle.buildWithIdleTransitions();

        // Build left morphism
        final TSMorphism left_morphism = new TSMorphismBuilder()
                .source(left_ts)
                .target(middle_ts)
                .addStateMapping(bl, cm)
                .addTransitionMapping(cldl, cmdm)
                .buildWithIdleTransitions();

        // Build right morphism
        final TSMorphism right_morphism = new TSMorphismBuilder()
                .source(right_ts)
                .target(middle_ts)
                .addTransitionMapping(crdr, cmdm)
                .addStateMapping(er, dm)
                .buildWithIdleTransitions();

        final PullbackResult result = PullbackResult.calculate(new Cospan(left_morphism, right_morphism));
        // source is the same system
        assertThat(result.getM1().getSource(), is(result.getM2().getSource()));
        // Four states with the given names expected
        // TODO there is no d/e here
        return result;
    }

    @Test
    void abcdByHand() {
        // Build left side transition system
        final State abl = new State("A/B");
        final State bbl = new State("B/B");
        final State ccl = new State("C/C");
        final State cdl = new State("C/D");
        final Transition abl_bbl = new Transition(abl, bbl, "<ab, *>");
        final Transition bbl_ccl = new Transition(bbl, ccl, "<bc, bc>");
        final Transition ccl_cdl = new Transition(ccl, cdl, "<*, cd>");
        this.left.startState(abl)
                .addTransition(abl_bbl)
                .addTransition(bbl_ccl)
                .addTransition(ccl_cdl);
        final TransitionSystem left_ts = this.left.buildWithIdleTransitions();

        // Build right side transition system
        final State bcr = new State("B/C");
        final State ccr = new State("C/C");
        final State ddr = new State("D/D");
        final State der = new State("D/E");
        final Transition bcr_cr = new Transition(bcr, ccr, "<bc, *>");
        final Transition ccr_ddr = new Transition(ccr, ddr, "<cd, cd>");
        final Transition ddr_der = new Transition(ddr, der, "<*, de>");
        this.right.startState(bcr)
                .addTransition(bcr_cr)
                .addTransition(ccr_ddr)
                .addTransition(ddr_der);
        final TransitionSystem right_ts = this.right.buildWithIdleTransitions();

        // Build middle
        final State bm = new State("B");
        final State cm = new State("C");
        final State dm = new State("D");
        final Transition bcm = new Transition(bm, cm, "bc");
        final Transition cdm = new Transition(cm, dm, "cd");
        this.middle.startState(bm)
                .addTransition(bcm)
                .addTransition(cdm);
        final TransitionSystem middle_ts = this.middle.buildWithIdleTransitions();

        // Build left morphism
        final TSMorphism left_morphism = new TSMorphismBuilder()
                .source(left_ts)
                .target(middle_ts)
                .addStateMapping(abl, bm)
                .addTransitionMapping(bbl_ccl, bcm)
                .addTransitionMapping(ccl_cdl, cdm)
                .buildWithIdleTransitions();

        // Build right morphism
        final TSMorphism right_morphism = new TSMorphismBuilder()
                .source(right_ts)
                .target(middle_ts)
                .addStateMapping(der, dm)
                .addTransitionMapping(bcr_cr, bcm)
                .addTransitionMapping(ccr_ddr, cdm)
                .buildWithIdleTransitions();

        final PullbackResult result = PullbackResult.calculate(new Cospan(left_morphism, right_morphism));
        // source is the same system
        assertThat(result.getM1().getSource(), is(result.getM2().getSource()));
        // Four states with the given names expected
        final TransitionSystem pullbackSystem = result.getM1().getSource();
    }
}
