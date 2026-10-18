package edu.automata;

import edu.automata.algorithm.*;
import edu.automata.model.Automaton;
import edu.automata.model.Examples;
import edu.automata.model.State;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class AlgorithmTest {

    @Test
    void regexOpsSimplify() {
        assertEquals("a", RegexOps.union("a", "a"));
        assertEquals("a?", RegexOps.union(RegexOps.EPS, "a"));
        assertEquals("a*", RegexOps.union(RegexOps.EPS, "a*"));
        assertEquals("a|b", RegexOps.union("a", "b"));
        assertNull(RegexOps.concat("a", null));                    // R·∅ = ∅
        assertEquals("ab", RegexOps.concat("a", "b"));
        assertEquals("(a|b)c", RegexOps.concat("a|b", "c"));      // paréntesis solo cuando hacen falta
        assertEquals(RegexOps.EPS, RegexOps.star(null));            // ∅* = ε
        assertEquals("a*", RegexOps.star("a"));
        assertEquals("a*", RegexOps.star("a*"));
        assertEquals("(ab)*", RegexOps.star("ab"));
    }

    @Test
    void labelsAreParsed() {
        assertEquals("a|b", RegexOps.fromLabel("a, b"));
        assertEquals(RegexOps.EPS, RegexOps.fromLabel("eps"));
        assertEquals("\\.", RegexOps.fromLabel("."));              // metacaracteres escapados
    }

    @Test
    void evenZerosGivesKnownRegex() {
        Automaton a = build(Examples.all().values().iterator().next());
        EliminationResult r = StateEliminator.run(a, StateEliminator.Order.MIN_DEGREE);
        assertEquals("(1|01*0)*", r.regex());
        assertEquals(5, r.steps().size());                         // original, normalización, 2 eliminaciones, final
    }

    @Test
    void everyExampleIsEquivalentToItsRegexForBothOrders() {
        for (Consumer<Automaton> ex : Examples.all().values())
            for (StateEliminator.Order order : StateEliminator.Order.values()) {
                Automaton a = build(ex);
                EliminationResult r = StateEliminator.run(a, order);
                EquivalenceChecker.Report rep = EquivalenceChecker.exhaustive(a, r.regex(), 10);
                assertNull(rep.counterexample(), "Contraejemplo: " + rep.counterexample());
            }
    }

    @Test
    void normalizationAddsStartAndAccept() {
        Automaton a = build(Examples.all().values().iterator().next());
        GnfaGraph g = GnfaGraph.fromAutomaton(a);
        g.normalize();
        assertEquals(RegexOps.EPS, g.get(GnfaGraph.START, 0));
        assertEquals(RegexOps.EPS, g.get(0, GnfaGraph.ACCEPT));
        assertEquals(2, g.intermediateIds().size());
    }

    @Test
    void noAcceptingStateGivesEmptyLanguage() {
        Automaton a = new Automaton();
        State q0 = a.addState(10, 10);
        a.toggleInitial(q0.getId());
        a.addTransition(q0.getId(), q0.getId(), "a");
        assertNull(StateEliminator.run(a, StateEliminator.Order.MIN_DEGREE).regex());
    }

    @Test
    void simulatorAcceptsAndRejects() {
        Automaton a = build(Examples.all().values().iterator().next());   // cantidad par de 0s
        assertTrue(DfaSimulator.run(a, "1001").accepted());
        assertFalse(DfaSimulator.run(a, "10010").accepted());
        assertTrue(DfaSimulator.run(a, "").accepted());
        assertTrue(DfaSimulator.run(a, "2").stuck());              // símbolo fuera del alfabeto
    }

    @Test
    void nonDeterminismIsDetected() {
        Automaton a = new Automaton();
        State q0 = a.addState(10, 10), q1 = a.addState(50, 10);
        a.toggleInitial(q0.getId());
        a.addTransition(q0.getId(), q0.getId(), "a");
        a.addTransition(q0.getId(), q1.getId(), "a");
        assertFalse(a.validateAsDfa().isEmpty());
    }

    private static Automaton build(Consumer<Automaton> builder) {
        Automaton a = new Automaton();
        builder.accept(a);
        return a;
    }
}
