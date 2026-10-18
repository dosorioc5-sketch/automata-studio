package edu.automata.algorithm;

import java.util.List;

/**
 * Un paso del algoritmo, con todo lo necesario para animarlo.
 * @param before      grafo antes de eliminar (null si el paso no elimina nada)
 * @param after       grafo resultante del paso
 * @param removedId   estado eliminado (o Integer.MIN_VALUE si no aplica)
 * @param updatedEdges ids (GnfaGraph.edgeId) de las aristas recalculadas
 * @param formulas    texto de cada recálculo R_ik' = R_ik ∪ (R_ij·R_jj*·R_jk)
 */
public record EliminationStep(String title, String detail, GnfaGraph before, GnfaGraph after,
                              int removedId, List<Integer> updatedEdges, List<String> formulas) {
    public static final int NONE = Integer.MIN_VALUE;
}
