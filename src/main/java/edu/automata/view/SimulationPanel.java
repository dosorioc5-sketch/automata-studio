package edu.automata.view;

import edu.automata.controller.SimulationController;

import javax.swing.*;
import java.awt.*;

/** Pestaña "Simulación": entrada de cadena, animación y veredicto ACEPTADA / RECHAZADA. */
public class SimulationPanel extends JPanel implements SimulationController.SimulationView {
    private final JTextField input = new JTextField(14);
    private final JSlider speed = new JSlider(200, 1500, 700);
    private final JLabel trace = new JLabel(" ");
    private final JLabel result = new JLabel(" ", SwingConstants.CENTER);
    private final JTextArea log = new JTextArea();

    public SimulationPanel(SimulationController controller) {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(new JLabel("Cadena de entrada (símbolos del alfabeto del DFA):"));
        top.add(Box.createVerticalStrut(4));
        input.setFont(new Font(Font.MONOSPACED, Font.BOLD, 18));
        top.add(input);
        top.add(Box.createVerticalStrut(6));

        JPanel buttons = new JPanel(new GridLayout(1, 3, 6, 0));
        JButton play = new JButton("▶ Simular");
        JButton step = new JButton("Paso ⏭");
        JButton reset = new JButton("Reiniciar");
        buttons.add(play); buttons.add(step); buttons.add(reset);
        top.add(buttons);
        top.add(Box.createVerticalStrut(6));

        JPanel sp = new JPanel(new BorderLayout());
        sp.add(new JLabel("Velocidad: "), BorderLayout.WEST);
        speed.setInverted(true);
        sp.add(speed, BorderLayout.CENTER);
        top.add(sp);
        top.add(Box.createVerticalStrut(8));

        trace.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 20));
        trace.setAlignmentX(LEFT_ALIGNMENT);
        top.add(trace);
        top.add(Box.createVerticalStrut(6));
        result.setFont(result.getFont().deriveFont(Font.BOLD, 22f));
        result.setOpaque(true);
        result.setAlignmentX(LEFT_ALIGNMENT);
        top.add(result);
        alignLeft(top);
        add(top, BorderLayout.NORTH);

        log.setEditable(false);
        log.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        JScrollPane scroll = new JScrollPane(log);
        scroll.setBorder(BorderFactory.createTitledBorder("Recorrido"));
        add(scroll, BorderLayout.CENTER);

        play.addActionListener(e -> controller.play(input.getText().trim(), speed.getValue()));
        step.addActionListener(e -> controller.step(input.getText().trim()));
        reset.addActionListener(e -> controller.reset());
        input.addActionListener(e -> controller.play(input.getText().trim(), speed.getValue()));
        controller.setUi(this);
    }

    @Override public void showTrace(String in, int consumed) {
        if (in.isEmpty()) { trace.setText(" "); return; }
        String done = esc(in.substring(0, consumed)), rest = esc(in.substring(consumed));
        trace.setText("<html><span style='color:#2E7D32'><b>" + done + "</b></span>"
                + "<span style='color:#757575'>" + rest + "</span></html>");
    }

    @Override public void appendLog(String line) { log.append(line + "\n"); log.setCaretPosition(log.getDocument().getLength()); }
    @Override public void clearLog() { log.setText(""); }

    @Override public void showResult(Boolean accepted, String message) {
        result.setText(message);
        if (accepted == null) { result.setBackground(null); result.setForeground(Color.DARK_GRAY); }
        else if (accepted) { result.setBackground(new Color(0xC8E6C9)); result.setForeground(new Color(0x1B5E20)); }
        else { result.setBackground(new Color(0xFFCDD2)); result.setForeground(new Color(0xB71C1C)); }
    }

    private static String esc(String s) { return s.replace("&", "&amp;").replace("<", "&lt;"); }

    private static void alignLeft(JPanel p) {
        for (Component c : p.getComponents()) if (c instanceof JComponent jc) jc.setAlignmentX(LEFT_ALIGNMENT);
    }
}
