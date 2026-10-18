package edu.automata.algorithm;

import edu.automata.model.Transition;

/**
 * Álgebra de expresiones regulares sobre cadenas de texto.
 * Convenciones: null = ∅ (lenguaje vacío), "ε" = cadena vacía.
 * Cada operación simplifica (∅, ε, paréntesis redundantes) para que el resultado final sea legible.
 */
public final class RegexOps {
    public static final String EPS = "ε";
    public static final String EMPTY = "∅";
    private static final String META = "\\.[]{}()*+?^$|";

    private RegexOps() {}

    /** Convierte la etiqueta de una arista ("a,b", "eps", "ab*") en una expresión regular. */
    public static String fromLabel(String label) {
        if (label == null) return null;
        String result = null;
        for (String tok : label.split(",")) {
            String t = tok.trim();
            if (t.isEmpty()) continue;
            String piece;
            if (Transition.isEpsilon(t)) piece = EPS;
            else if (t.length() == 1 && META.indexOf(t.charAt(0)) >= 0) piece = "\\" + t;
            else piece = t;
            result = union(result, piece);
        }
        return result;
    }

    /** R1 ∪ R2 */
    public static String union(String a, String b) {
        if (a == null) return b;
        if (b == null) return a;
        if (a.equals(b)) return a;
        if (EPS.equals(a)) return optional(b);
        if (EPS.equals(b)) return optional(a);
        return a + "|" + b;
    }

    /** R1 · R2 */
    public static String concat(String a, String b) {
        if (a == null || b == null) return null;
        if (EPS.equals(a)) return b;
        if (EPS.equals(b)) return a;
        return wrapUnion(a) + wrapUnion(b);
    }

    /** R* (∅* = ε, ε* = ε). */
    public static String star(String a) {
        if (a == null || EPS.equals(a)) return EPS;
        if (a.endsWith("*") && isAtom(a.substring(0, a.length() - 1))) return a;
        if (a.endsWith("?") && isAtom(a.substring(0, a.length() - 1)))
            return star(a.substring(0, a.length() - 1));
        return isAtom(a) ? a + "*" : "(" + a + ")*";
    }

    /** Muestra legible: null -> ∅. */
    public static String show(String r) { return r == null ? EMPTY : r; }

    /** Convierte nuestra notación a sintaxis java.util.regex. */
    public static String toJava(String r) {
        if (r == null) return "(?!)";       // nunca coincide
        if (EPS.equals(r)) return "";
        return r;
    }

    // ---------- auxiliares ----------
    private static String optional(String x) {
        if (x.endsWith("*") && isAtom(x.substring(0, x.length() - 1))) return x;
        if (x.endsWith("?") && isAtom(x.substring(0, x.length() - 1))) return x;
        return isAtom(x) ? x + "?" : "(" + x + ")?";
    }

    private static String wrapUnion(String s) { return hasTopLevelUnion(s) ? "(" + s + ")" : s; }

    static boolean hasTopLevelUnion(String s) {
        int depth = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\') { i++; continue; }
            if (c == '(') depth++;
            else if (c == ')') depth--;
            else if (c == '|' && depth == 0) return true;
        }
        return false;
    }

    /** ¿Es un símbolo, un símbolo escapado o un grupo (...) completo? */
    static boolean isAtom(String s) {
        if (s.length() == 1) return true;
        if (s.length() == 2 && s.charAt(0) == '\\') return true;
        if (s.charAt(0) != '(') return false;
        int depth = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\') { i++; continue; }
            if (c == '(') depth++;
            else if (c == ')') { depth--; if (depth == 0 && i < s.length() - 1) return false; }
        }
        return depth == 0;
    }
}
