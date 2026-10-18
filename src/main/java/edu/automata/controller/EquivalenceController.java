package edu.automata.controller;

import edu.automata.algorithm.DfaSimulator;
import edu.automata.algorithm.EquivalenceChecker;
import edu.automata.model.Automaton;

import java.util.List;

/** Verifica que el DFA y la expresión regular obtenida aceptan exactamente las mismas cadenas. */
public class EquivalenceController {
    public record Comparison(boolean dfaAccepts, boolean regexAccepts, boolean agree) { }

    private final Automaton automaton;

    public EquivalenceController(Automaton automaton) { this.automaton = automaton; }

    public List<String> errors() { return automaton.validateAsDfa(); }

    public Comparison compare(String regex, String input) {
        boolean dfa = DfaSimulator.run(automaton, input).accepted();
        boolean re = EquivalenceChecker.regexAccepts(regex, input);
        return new Comparison(dfa, re, dfa == re);
    }

    public EquivalenceChecker.Report exhaustive(String regex, int maxLength) {
        return EquivalenceChecker.exhaustive(automaton, regex, maxLength);
    }
}
