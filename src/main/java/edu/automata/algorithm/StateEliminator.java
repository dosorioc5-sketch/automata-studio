package edu.automata.algorithm;

import edu.automata.model.Automaton;

import java.util.ArrayList;
import java.util.List;

/**
 * Conversión DFA → Expresión Regular por eliminación de estados.
 *
 *   R_ik' = R_ik  ∪  ( R_ij · (R_jj)* · R_jk )
 *
 * El término (R_jj)* proviene del Lema de Arden: la ecuación X = R_jj·X ∪ S tiene
 * como solución mínima X = (R_jj)*·S.
 */
public final class StateEliminator {
    public enum Order { MIN_DEGREE, CREATION }

    private StateEliminator() {}

    public static EliminationResult run(Automaton automaton, Order order) {
        List<EliminationStep> steps = new ArrayList<>();

        GnfaGraph g = GnfaGraph.fromAutomaton(automaton);
        steps.add(new EliminationStep("Autómata original (DFA)",
                "Se parte del autómata dibujado. Cada transición se interpreta como una expresión regular.",
                null, g.copy(), EliminationStep.NONE, List.of(), List.of()));

        g.normalize();
        steps.add(new EliminationStep("Paso 1 — Normalización",
                "Se añade q_start con ε hacia q0 y q_accept con ε desde cada estado de F.",
                null, g.copy(), EliminationStep.NONE, List.of(), List.of()));

        while (!g.intermediateIds().isEmpty()) {
            int j = pick(g, order);
            GnfaGraph before = g.copy();
            List<Integer> updated = new ArrayList<>();
            List<String> formulas = new ArrayList<>();

            String rjj = g.get(j, j);
            String loop = RegexOps.star(rjj);                       // (R_jj)*
            for (int i : g.predecessors(j)) {
                for (int k : g.successors(j)) {
                    String rik = g.get(i, k), rij = g.get(i, j), rjk = g.get(j, k);
                    String path = RegexOps.concat(RegexOps.concat(rij, loop), rjk);
                    String rikNew = RegexOps.union(rik, path);       // R_ik ∪ (R_ij · R_jj* · R_jk)
                    g.put(i, k, rikNew);
                    updated.add(GnfaGraph.edgeId(i, k));
                    formulas.add(String.format("R(%s,%s)' = %s ∪ ( %s · (%s)* · %s ) = %s",
                            before.nameOf(i), before.nameOf(k),
                            RegexOps.show(rik), RegexOps.show(rij), RegexOps.show(rjj),
                            RegexOps.show(rjk), RegexOps.show(rikNew)));
                }
            }
            g.removeNode(j);
            steps.add(new EliminationStep("Paso 2 — Eliminar " + before.nameOf(j),
                    "Se elimina " + before.nameOf(j) + " y se recalculan las aristas que pasaban por él.",
                    before, g.copy(), j, updated, formulas));
        }

        String regex = g.get(GnfaGraph.START, GnfaGraph.ACCEPT);
        steps.add(new EliminationStep("Resultado final",
                "Queda una sola arista q_start → q_accept.   R = " + RegexOps.show(regex),
                null, g.copy(), EliminationStep.NONE, List.of(), List.of()));
        return new EliminationResult(steps, regex);
    }

    /** Elige el siguiente estado a eliminar. */
    private static int pick(GnfaGraph g, Order order) {
        List<Integer> candidates = g.intermediateIds();
        if (order == Order.CREATION) return candidates.get(0);
        int best = candidates.get(0);
        long bestCost = Long.MAX_VALUE;
        for (int j : candidates) {
            long cost = (long) g.predecessors(j).size() * g.successors(j).size();
            if (cost < bestCost) { bestCost = cost; best = j; }
        }
        return best;
    }
}
