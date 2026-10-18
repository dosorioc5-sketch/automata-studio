package edu.automata.model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/** Autómatas de ejemplo para probar rápidamente la aplicación. */
public final class Examples {
    private Examples() {}

    public static Map<String, Consumer<Automaton>> all() {
        Map<String, Consumer<Automaton>> m = new LinkedHashMap<>();
        m.put("Cantidad par de 0s  (Σ={0,1})", Examples::evenZeros);
        m.put("Cadenas que terminan en 'ab'  (Σ={a,b})", Examples::endsWithAb);
        m.put("Binarios múltiplos de 3  (Σ={0,1})", Examples::multiplesOfThree);
        return m;
    }

    private static void evenZeros(Automaton a) {
        a.clear();
        State q0 = a.addState(200, 220), q1 = a.addState(450, 220);
        a.toggleInitial(q0.getId()); a.toggleAccepting(q0.getId());
        a.addTransition(q0.getId(), q0.getId(), "1");
        a.addTransition(q0.getId(), q1.getId(), "0");
        a.addTransition(q1.getId(), q0.getId(), "0");
        a.addTransition(q1.getId(), q1.getId(), "1");
    }

    private static void endsWithAb(Automaton a) {
        a.clear();
        State q0 = a.addState(180, 220), q1 = a.addState(400, 220), q2 = a.addState(620, 220);
        a.toggleInitial(q0.getId()); a.toggleAccepting(q2.getId());
        a.addTransition(q0.getId(), q0.getId(), "b");
        a.addTransition(q0.getId(), q1.getId(), "a");
        a.addTransition(q1.getId(), q1.getId(), "a");
        a.addTransition(q1.getId(), q2.getId(), "b");
        a.addTransition(q2.getId(), q1.getId(), "a");
        a.addTransition(q2.getId(), q0.getId(), "b");
    }

    private static void multiplesOfThree(Automaton a) {
        a.clear();
        State q0 = a.addState(200, 150), q1 = a.addState(450, 150), q2 = a.addState(325, 360);
        a.toggleInitial(q0.getId()); a.toggleAccepting(q0.getId());
        a.addTransition(q0.getId(), q0.getId(), "0");
        a.addTransition(q0.getId(), q1.getId(), "1");
        a.addTransition(q1.getId(), q2.getId(), "0");
        a.addTransition(q1.getId(), q0.getId(), "1");
        a.addTransition(q2.getId(), q1.getId(), "0");
        a.addTransition(q2.getId(), q2.getId(), "1");
    }
}
