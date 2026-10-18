package edu.automata.model;

import java.util.*;

/** Modelo matemático del autómata: A = (Q, Σ, δ, q0, F). No depende de ninguna clase gráfica. */
public class Automaton {
    private final Map<Integer, State> states = new LinkedHashMap<>();
    private final Map<Integer, Transition> transitions = new LinkedHashMap<>();
    private int nextStateId = 0;
    private int nextTransitionId = 0;

    // ---------- Estados ----------
    public State addState(double x, double y) {
        State s = new State(nextStateId, "q" + nextStateId, x, y);
        states.put(s.getId(), s);
        nextStateId++;
        return s;
    }

    public void removeState(int id) {
        states.remove(id);
        transitions.values().removeIf(t -> t.getFrom() == id || t.getTo() == id);
    }

    public State getState(int id) { return states.get(id); }
    public List<State> getStates() { return new ArrayList<>(states.values()); }

    /** Solo puede existir un estado inicial; si ya lo era, se desmarca. */
    public void toggleInitial(int id) {
        State target = states.get(id);
        if (target == null) return;
        boolean makeInitial = !target.isInitial();
        for (State s : states.values()) s.setInitial(false);
        target.setInitial(makeInitial);
    }

    public void toggleAccepting(int id) {
        State s = states.get(id);
        if (s != null) s.setAccepting(!s.isAccepting());
    }

    public State getInitial() {
        for (State s : states.values()) if (s.isInitial()) return s;
        return null;
    }

    public List<State> getAcceptingStates() {
        List<State> out = new ArrayList<>();
        for (State s : states.values()) if (s.isAccepting()) out.add(s);
        return out;
    }

    // ---------- Transiciones ----------
    public Transition addTransition(int from, int to, String label) {
        Transition t = new Transition(nextTransitionId++, from, to, label);
        transitions.put(t.getId(), t);
        return t;
    }

    public void removeTransition(int id) { transitions.remove(id); }
    public Transition getTransition(int id) { return transitions.get(id); }
    public List<Transition> getTransitions() { return new ArrayList<>(transitions.values()); }

    public void clear() {
        states.clear(); transitions.clear();
        nextStateId = 0; nextTransitionId = 0;
    }

    /** Alfabeto Σ inferido: símbolos de un solo carácter que aparecen en las etiquetas. */
    public SortedSet<Character> getAlphabet() {
        SortedSet<Character> sigma = new TreeSet<>();
        for (Transition t : transitions.values())
            for (String s : t.symbols())
                if (s.length() == 1 && !Transition.isEpsilon(s)) sigma.add(s.charAt(0));
        return sigma;
    }

    /** Transición que sale de 'from' consumiendo 'c' (null si no existe). */
    public Transition find(int from, char c) {
        for (Transition t : transitions.values())
            if (t.getFrom() == from && t.symbols().contains(String.valueOf(c))) return t;
        return null;
    }

    /** Errores que impiden tratar el autómata como un DFA simulable (lista vacía = válido). */
    public List<String> validateAsDfa() {
        List<String> errors = new ArrayList<>();
        if (states.isEmpty()) errors.add("No hay estados en el lienzo.");
        if (getInitial() == null) errors.add("Falta marcar el estado inicial (q0).");
        Set<String> seen = new HashSet<>();
        for (Transition t : transitions.values()) {
            String src = states.get(t.getFrom()).getName();
            for (String s : t.symbols()) {
                if (Transition.isEpsilon(s))
                    errors.add("Un DFA no admite transiciones ε (en " + src + ").");
                else if (s.length() != 1)
                    errors.add("La etiqueta '" + s + "' no es un símbolo único; solo se simulan símbolos de 1 carácter.");
                else if (!seen.add(t.getFrom() + "|" + s))
                    errors.add("No determinista: " + src + " tiene más de una transición con '" + s + "'.");
            }
        }
        return errors;
    }
}
