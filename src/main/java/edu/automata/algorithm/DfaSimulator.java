package edu.automata.algorithm;

import edu.automata.model.Automaton;
import edu.automata.model.State;
import edu.automata.model.Transition;

import java.util.ArrayList;
import java.util.List;

/** Ejecuta una cadena sobre el DFA y devuelve el recorrido completo (para animarlo). */
public final class DfaSimulator {
    private DfaSimulator() {}

    /** Un paso del recorrido: δ(from, symbol) = to usando la transición 'transitionId'. */
    public record Step(int from, char symbol, int transitionId, int to) { }

    /**
     * @param steps       pasos realizados con éxito
     * @param stuck       true si se quedó sin transición antes de consumir toda la cadena
     * @param finalState  último estado alcanzado
     */
    public record Result(List<Step> steps, boolean accepted, boolean stuck, String message, int finalState) { }

    public static Result run(Automaton a, String input) {
        State current = a.getInitial();
        List<Step> steps = new ArrayList<>();
        for (char c : input.toCharArray()) {
            Transition t = a.find(current.getId(), c);
            if (t == null) {
                String msg = a.getAlphabet().contains(c)
                        ? "No existe transición desde " + current.getName() + " con '" + c + "'."
                        : "El símbolo '" + c + "' no pertenece al alfabeto del autómata.";
                return new Result(steps, false, true, msg, current.getId());
            }
            steps.add(new Step(current.getId(), c, t.getId(), t.getTo()));
            current = a.getState(t.getTo());
        }
        return new Result(steps, current.isAccepting(), false,
                current.isAccepting() ? "Terminó en el estado de aceptación " + current.getName()
                                      : "Terminó en " + current.getName() + ", que no es de aceptación.",
                current.getId());
    }
}
