package edu.automata.view;

import edu.automata.algorithm.StateEliminator;
import edu.automata.controller.EliminationController;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.function.Consumer;

/** Pestaña "Regex": controles del algoritmo de eliminación de estados y registro de pasos. */
public class ConversionPanel extends JPanel implements EliminationController.EliminationView {
    private final JComboBox<String> order = new JComboBox<>(new String[]{
            "Automático (menor grado primero)", "Orden de creación (q0, q1, …)"});
    private final JLabel stepInfo = new JLabel("Pulsa «Iniciar» para normalizar el autómata.");
    private final JTextArea log = new JTextArea();
    private final JTextField regex = new JTextField();
    private final JButton start = new JButton("Iniciar");
    private final JButton next = new JButton("Siguiente paso");
    private final JButton play = new JButton("▶ Animar");
    private final JButton pause = new JButton("⏸ Pausa");
    private final JButton end = new JButton("⏭ Final");
    private final JButton back = new JButton("↩ Volver al editor");
    private Consumer<String> onRegex = s -> { };

    public ConversionPanel(EliminationController c) {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(new JLabel("Orden de eliminación de estados:"));
        top.add(order);
        top.add(Box.createVerticalStrut(6));
        JPanel row1 = new JPanel(new GridLayout(1, 2, 6, 0));
        row1.add(start); row1.add(next);
        JPanel row2 = new JPanel(new GridLayout(1, 3, 6, 0));
        row2.add(play); row2.add(pause); row2.add(end);
        top.add(row1); top.add(Box.createVerticalStrut(4)); top.add(row2);
        top.add(Box.createVerticalStrut(4)); top.add(back);
        top.add(Box.createVerticalStrut(8));
        stepInfo.setFont(stepInfo.getFont().deriveFont(Font.BOLD));
        top.add(stepInfo);
        alignLeft(top);
        add(top, BorderLayout.NORTH);

        log.setEditable(false);
        log.setLineWrap(true);
        log.setWrapStyleWord(true);
        log.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scroll = new JScrollPane(log);
        scroll.setBorder(BorderFactory.createTitledBorder("Pasos del algoritmo"));
        add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(6, 0));
        bottom.setBorder(BorderFactory.createTitledBorder("Expresión regular resultante"));
        regex.setEditable(false);
        regex.setFont(new Font(Font.MONOSPACED, Font.BOLD, 15));
        JButton copy = new JButton("Copiar");
        copy.addActionListener(e -> Toolkit.getDefaultToolkit().getSystemClipboard()
                .setContents(new StringSelection(regex.getText()), null));
        bottom.add(regex, BorderLayout.CENTER);
        bottom.add(copy, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);

        start.addActionListener(e -> c.prepare(order.getSelectedIndex() == 0
                ? StateEliminator.Order.MIN_DEGREE : StateEliminator.Order.CREATION));
        next.addActionListener(e -> c.next());
        play.addActionListener(e -> c.play());
        pause.addActionListener(e -> c.pause());
        end.addActionListener(e -> c.skipToEnd());
        back.addActionListener(e -> { c.exit(); stepInfo.setText("Pulsa «Iniciar» para normalizar el autómata."); });
        c.setUi(this);
    }

    public void setOnRegex(Consumer<String> onRegex) { this.onRegex = onRegex; }

    @Override public void log(String line) { log.append(line + "\n"); log.setCaretPosition(log.getDocument().getLength()); }
    @Override public void clearLog() { log.setText(""); }
    @Override public void setStepInfo(String text) { stepInfo.setText(text); }
    @Override public void setBusy(boolean busy) { start.setEnabled(!busy); next.setEnabled(!busy); play.setEnabled(!busy); }

    @Override public void showRegex(String r) {
        regex.setText(r == null ? "" : r);
        onRegex.accept(r);
    }

    private static void alignLeft(JPanel p) {
        for (Component c : p.getComponents()) if (c instanceof JComponent jc) jc.setAlignmentX(LEFT_ALIGNMENT);
    }
}
