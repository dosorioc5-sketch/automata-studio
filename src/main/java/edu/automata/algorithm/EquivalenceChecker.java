package edu.automata.algorithm;

import edu.automata.model.Automaton;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/** Compara el DFA con la expresión regular obtenida usando el motor de java.util.regex. */
public final class EquivalenceChecker {
    private EquivalenceChecker() {}

    /** ¿La regex (en nuestra notación) acepta TODA la cadena? */
    public static boolean regexAccepts(String regex, String input) {
        return Pattern.compile(RegexOps.toJava(regex)).matcher(input).matches();
    }

    /** @param counterexample primera cadena donde DFA y regex discrepan (null si no hay) */
    public record Report(int tested, int maxLength, String counterexample) { }

    /** Prueba todas las cadenas sobre Σ hasta una longitud máxima (acotada para no explotar). */
    public static Report exhaustive(Automaton a, String regex, int maxLength) {
        List<Character> sigma = new ArrayList<>(a.getAlphabet());
        Pattern p = Pattern.compile(RegexOps.toJava(regex));
        int tested = 0;
        List<String> level = new ArrayList<>(List.of(""));
        int reached = 0;
        for (int len = 0; len <= maxLength; len++) {
            for (String s : level) {
                tested++;
                boolean dfa = DfaSimulator.run(a, s).accepted();
                boolean re = p.matcher(s).matches();
                if (dfa != re) return new Report(tested, len, s);
            }
            reached = len;
            if (len == maxLength || (long) level.size() * sigma.size() > 200_000) break;
            List<String> next = new ArrayList<>();
            for (String s : level) for (char c : sigma) next.add(s + c);
            level = next;
        }
        return new Report(tested, reached, null);
    }
}
