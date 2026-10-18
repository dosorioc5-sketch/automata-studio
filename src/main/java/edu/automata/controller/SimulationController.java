package edu.automata.controller;

import edu.automata.algorithm.DfaSimulator;
import edu.automata.model.Automaton;
import edu.automata.view.GraphView;

import javax.swing.Timer;
import java.util.List;

/** Controla la simulación animada de una cadena sobre el DFA dibujado. */
public class SimulationController {
    /** Lo que el panel de simulación debe saber mostrar. */
    public interface SimulationView {
        void showTrace(String input, int consumed);
        void appendLog(String line);
        void clearLog();
        /** accepted == null: aún sin veredicto (o error de validación). */
        void showResult(Boolean accepted, String message);
    }

    private static final String CURRENT = "#FFD54F", OK = "#81C784", BAD = "#E57373", EDGE = "#D32F2F";

    private final Automaton automaton;
    private final EditorController editor;
    private final GraphView view;
    private SimulationView ui;
    private Timer timer;
    private DfaSimulator.Result result;
    private String input = "";
    private int shown;

    public SimulationController(Automaton automaton, EditorController editor, GraphView view) {
        this.automaton = automaton;
        this.editor = editor;
        this.view = view;
        editor.addStructureListener(this::reset);   // si el usuario edita el autómata, se cancela la simulación
    }

    public void setUi(SimulationView ui) { this.ui = ui; }

    /** Valida y calcula todo el recorrido; devuelve false si el autómata no es un DFA válido. */
    private boolean prepare(String in) {
        stopTimer();
        List<String> errors = automaton.validateAsDfa();
        if (!errors.isEmpty()) {
            ui.showResult(null, "<html>⚠ " + String.join("<br>⚠ ", errors) + "</html>");
            return false;
        }
        input = in;
        result = DfaSimulator.run(automaton, in);
        shown = 0;
        ui.clearLog();
        ui.showResult(null, " ");
        display();
        return true;
    }

    /** Reproduce la animación automática (un símbolo cada delayMs milisegundos). */
    public void play(String in, int delayMs) {
        if (!prepare(in)) return;
        if (result.steps().isEmpty()) return;
        timer = new Timer(delayMs, e -> {
            shown++;
            display();
            if (shown >= result.steps().size()) stopTimer();
        });
        timer.start();
    }

    /** Avanza un único símbolo (modo paso a paso). */
    public void step(String in) {
        if (result == null || !in.equals(input) || shown >= result.steps().size()) {
            if (!prepare(in)) return;
            if (result.steps().isEmpty()) return;
        }
        stopTimer();
        shown++;
        display();
    }

    public void reset() {
        stopTimer();
        result = null; shown = 0;
        editor.setOverlay(null);
        if (ui != null) { ui.clearLog(); ui.showTrace("", 0); ui.showResult(null, " "); }
        editor.refresh();
    }

    private void stopTimer() { if (timer != null) { timer.stop(); timer = null; } }

    /** Dibuja el instante 'shown' (nº de símbolos ya consumidos). */
    private void display() {
        List<DfaSimulator.Step> steps = result.steps();
        int cur = shown == 0 ? automaton.getInitial().getId() : steps.get(shown - 1).to();
        boolean finished = shown >= steps.size();
        String color = !finished ? CURRENT : (result.accepted() ? OK : BAD);
        DfaSimulator.Step last = shown > 0 ? steps.get(shown - 1) : null;

        editor.setOverlay(() -> {
            view.colorState(cur, color);
            if (last != null) view.colorEdge(last.transitionId(), EDGE);
        });
        editor.refresh();

        ui.showTrace(input, shown);
        if (last != null)
            ui.appendLog("δ(" + automaton.getState(last.from()).getName() + ", " + last.symbol() + ") = "
                    + automaton.getState(last.to()).getName());
        else ui.appendLog("Inicio en " + automaton.getState(cur).getName());

        if (finished) {
            if (result.stuck()) ui.appendLog("✖ " + result.message());
            ui.showResult(result.accepted(), result.accepted() ? "ACEPTADA" : "RECHAZADA");
        }
    }
}
