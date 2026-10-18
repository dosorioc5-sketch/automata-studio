package edu.automata.model;

/** Dato de dibujo de una arista (la vista no necesita conocer Transition ni GnfaGraph). */
public record Edge(int id, int from, int to, String label) { }
