package edu.automata.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Transición dirigida entre dos estados. La etiqueta puede ser:
 *  - un símbolo del alfabeto ("a"),
 *  - varios símbolos separados por coma ("a,b"),
 *  - epsilon ("eps" o "ε"),
 *  - o una expresión regular parcial ("ab*").
 */
public class Transition {
    private final int id;
    private final int from, to;
    private String label;

    public Transition(int id, int from, int to, String label) {
        this.id = id; this.from = from; this.to = to; this.label = label;
    }

    public int getId() { return id; }
    public int getFrom() { return from; }
    public int getTo() { return to; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    /** Separa la etiqueta por comas: "a, b" -> ["a","b"]. */
    public List<String> symbols() {
        List<String> out = new ArrayList<>();
        for (String s : label.split(",")) {
            String t = s.trim();
            if (!t.isEmpty()) out.add(t);
        }
        return out;
    }

    public static boolean isEpsilon(String token) {
        String t = token.trim();
        return t.equals("ε") || t.equalsIgnoreCase("eps") || t.equalsIgnoreCase("epsilon");
    }
}
