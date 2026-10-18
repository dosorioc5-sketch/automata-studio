package edu.automata.algorithm;

import java.util.List;

/** Resultado completo del algoritmo: todos los pasos y la regex final (null = ∅). */
public record EliminationResult(List<EliminationStep> steps, String regex) { }
