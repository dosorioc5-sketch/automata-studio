package edu.automata.view;

import edu.automata.algorithm.EquivalenceChecker;
import edu.automata.algorithm.RegexOps;
import edu.automata.controller.EquivalenceController;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/** Pestaña "Equivalencia": prueba una cadena en el DFA y en la regex (java.util.regex). */
public class EquivalencePanel extends JPanel {
    private final EquivalenceController controller;
    private final JTextField regexField = new JTextField();
    private final JTextField input = new JTextField(14);
    private final JLabel verdict = new JLabel(" ");
    private final JSpinner maxLen = new JSpinner(new SpinnerNumberModel(10, 1, 14, 1));
    private final JTextArea out = new JTextArea();
    private String regex;
    private boolean hasRegex;

    public EquivalencePanel(EquivalenceController controller) {
        this.controller = controller;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(new JLabel("Expresión regular obtenida (pestaña «Regex»):"));
        regexField.setEditable(false);
        regexField.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));
        top.add(regexField);
        top.add(Box.createVerticalStrut(10));
        top.add(new JLabel("Cadena de prueba:"));
        input.setFont(new Font(Font.MONOSPACED, Font.BOLD, 18));
        top.add(input);
        top.add(Box.createVerticalStrut(6));
        JButton eval = new JButton("Evaluar en DFA y en la regex");
        eval.setAlignmentX(LEFT_ALIGNMENT);
        top.add(eval);
        top.add(Box.createVerticalStrut(6));
        verdict.setFont(verdict.getFont().deriveFont(Font.BOLD, 16f));
        top.add(verdict);
        top.add(Box.createVerticalStrut(10));
        JPanel ex = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        ex.add(new JLabel("Verificar todas las cadenas hasta longitud"));
        ex.add(maxLen);
        top.add(ex);
        JButton all = new JButton("Verificación exhaustiva");
        all.setAlignmentX(LEFT_ALIGNMENT);
        top.add(all);
        alignLeft(top);
        add(top, BorderLayout.NORTH);

        out.setEditable(false);
        out.setLineWrap(true);
        out.setWrapStyleWord(true);
        JScrollPane sp = new JScrollPane(out);
        sp.setBorder(BorderFactory.createTitledBorder("Resultado"));
        add(sp, BorderLayout.CENTER);

        eval.addActionListener(e -> evaluate());
        input.addActionListener(e -> evaluate());
        all.addActionListener(e -> exhaustive());
    }

    /** Llamado cuando termina la conversión (raw = regex en nuestra notación; null/"∅" = lenguaje vacío). */
    public void setRegex(String shown) {
        hasRegex = shown != null;
        regex = (shown == null || RegexOps.EMPTY.equals(shown)) ? null : shown;
        regexField.setText(shown == null ? "" : shown);
        verdict.setText(" ");
        out.setText(hasRegex ? "" : "");
    }

    private boolean ready() {
        if (!hasRegex) { out.setText("Primero obtén la expresión regular en la pestaña «Regex»."); return false; }
        List<String> errors = controller.errors();
        if (!errors.isEmpty()) { out.setText("⚠ " + String.join("\n⚠ ", errors)); return false; }
        return true;
    }

    private void evaluate() {
        if (!ready()) return;
        String s = input.getText().trim();
        var c = controller.compare(regex, s);
        String shown = s.isEmpty() ? "ε (cadena vacía)" : s;
        verdict.setText(c.agree() ? "✔ Coinciden" : "✖ DISCREPAN");
        verdict.setForeground(c.agree() ? new Color(0x1B5E20) : new Color(0xB71C1C));
        out.setText("Cadena: " + shown + "\n\nDFA:    " + (c.dfaAccepts() ? "ACEPTADA" : "RECHAZADA")
                + "\nRegex:  " + (c.regexAccepts() ? "ACEPTADA" : "RECHAZADA")
                + "\n\n(La regex se evalúa con Pattern.matcher(...).matches() de Java.)");
    }

    private void exhaustive() {
        if (!ready()) return;
        EquivalenceChecker.Report r = controller.exhaustive(regex, (Integer) maxLen.getValue());
        if (r.counterexample() == null) {
            verdict.setText("✔ Equivalentes (hasta longitud " + r.maxLength() + ")");
            verdict.setForeground(new Color(0x1B5E20));
            out.setText("Se probaron " + r.tested() + " cadenas sobre el alfabeto del DFA.\n"
                    + "El DFA y la regex coinciden en todas.");
        } else {
            verdict.setText("✖ DISCREPAN");
            verdict.setForeground(new Color(0xB71C1C));
            out.setText("Contraejemplo: '" + r.counterexample() + "'");
        }
    }

    private static void alignLeft(JPanel p) {
        for (Component c : p.getComponents()) if (c instanceof JComponent jc) jc.setAlignmentX(LEFT_ALIGNMENT);
    }
}
