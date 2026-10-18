package edu.automata.algorithm;

import edu.automata.model.Automaton;
import edu.automata.model.Edge;
import edu.automata.model.State;
import edu.automata.model.Transition;

import java.util.*;

/**
 * Grafo generalizado (GNFA): cada arista (i → k) lleva una EXPRESIÓN REGULAR R_ik.
 *
 * Estructura de datos: lista de adyacencia con mapas anidados
 *      edges.get(i).get(k) = R_ik      (si no hay entrada, R_ik = ∅)
 * Esto da acceso O(1) a R_ik y permite recorrer predecesores/sucesores de un estado.
 */
public class GnfaGraph {
    public static final int START = -1;   // q_start
    public static final int ACCEPT = -2;  // q_accept

    private final Map<Integer, State> nodes = new LinkedHashMap<>();
    private final Map<Integer, Map<Integer, String>> edges = new LinkedHashMap<>();

    /** Identificador estable para dibujar/resaltar una arista (from → to). */
    public static int edgeId(int from, int to) { return (from + 3) * 100000 + (to + 3); }

    /** Construye el grafo a partir del DFA (uniendo con ∪ las aristas paralelas). */
    public static GnfaGraph fromAutomaton(Automaton a) {
        GnfaGraph g = new GnfaGraph();
        for (State s : a.getStates()) g.nodes.put(s.getId(), s.copy());
        for (Transition t : a.getTransitions()) {
            String r = RegexOps.fromLabel(t.getLabel());
            if (r != null) g.put(t.getFrom(), t.getTo(), RegexOps.union(g.get(t.getFrom(), t.getTo()), r));
        }
        return g;
    }

    /** PASO 1 — Normalización: q_start --ε--> q0  y  q ∈ F --ε--> q_accept. */
    public void normalize() {
        double minX = Double.MAX_VALUE, maxX = -Double.MAX_VALUE, sumY = 0;
        for (State s : nodes.values()) {
            minX = Math.min(minX, s.getX()); maxX = Math.max(maxX, s.getX()); sumY += s.getY();
        }
        if (nodes.isEmpty()) { minX = 150; maxX = 150; sumY = 200; }
        double cy = nodes.isEmpty() ? 200 : sumY / nodes.size();
        double dx = Math.max(0, 125 - (minX - 130));      // deja espacio para la flecha de entrada
        for (State s : nodes.values()) s.setPosition(s.getX() + dx, s.getY());

        State start = new State(START, "q_start", minX - 130 + dx, cy);
        start.setInitial(true);
        State accept = new State(ACCEPT, "q_accept", maxX + 130 + dx, cy);
        accept.setAccepting(true);

        for (State s : new ArrayList<>(nodes.values())) {
            if (s.isInitial()) put(START, s.getId(), RegexOps.EPS);
            if (s.isAccepting()) put(s.getId(), ACCEPT, RegexOps.EPS);
            s.setInitial(false); s.setAccepting(false);
        }
        nodes.put(START, start);
        nodes.put(ACCEPT, accept);
    }

    public GnfaGraph copy() {
        GnfaGraph g = new GnfaGraph();
        for (State s : nodes.values()) g.nodes.put(s.getId(), s.copy());
        for (var e : edges.entrySet()) g.edges.put(e.getKey(), new LinkedHashMap<>(e.getValue()));
        return g;
    }

    public String get(int i, int k) {
        Map<Integer, String> row = edges.get(i);
        return row == null ? null : row.get(k);
    }

    public void put(int i, int k, String r) {
        if (r == null) { Map<Integer, String> row = edges.get(i); if (row != null) row.remove(k); return; }
        edges.computeIfAbsent(i, x -> new LinkedHashMap<>()).put(k, r);
    }

    public List<Integer> predecessors(int j) {
        List<Integer> out = new ArrayList<>();
        for (var e : edges.entrySet()) if (e.getKey() != j && e.getValue().containsKey(j)) out.add(e.getKey());
        return out;
    }

    public List<Integer> successors(int j) {
        List<Integer> out = new ArrayList<>();
        Map<Integer, String> row = edges.get(j);
        if (row != null) for (int k : row.keySet()) if (k != j) out.add(k);
        return out;
    }

    public void removeNode(int j) {
        nodes.remove(j);
        edges.remove(j);
        for (Map<Integer, String> row : edges.values()) row.remove(j);
    }

    public List<Integer> intermediateIds() {
        List<Integer> out = new ArrayList<>();
        for (int id : nodes.keySet()) if (id != START && id != ACCEPT) out.add(id);
        return out;
    }

    public Collection<State> nodes() { return nodes.values(); }
    public String nameOf(int id) { State s = nodes.get(id); return s == null ? "?" : s.getName(); }

    public List<Edge> edgeList() {
        List<Edge> out = new ArrayList<>();
        for (var e : edges.entrySet())
            for (var f : e.getValue().entrySet())
                out.add(new Edge(edgeId(e.getKey(), f.getKey()), e.getKey(), f.getKey(), f.getValue()));
        return out;
    }
}
