package edu.automata.controller;

import edu.automata.algorithm.*;
import edu.automata.model.Automaton;
import edu.automata.model.Edge;
import edu.automata.view.GraphView;

import javax.swing.Timer;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador del algoritmo de eliminación de estados: ejecuta {@link StateEliminator}
 * y anima en el lienzo cada paso (resaltado → desvanecimiento del nodo → nuevo grafo).
 */
public class EliminationController {
    public interface EliminationView {
        void log(String line);
        void clearLog();
        void setStepInfo(String text);
        void showRegex(String regex);
        void setBusy(boolean busy);
    }

    private static final int TICK_MS = 60, HOLD_TICKS = 14, FADE_TICKS = 8, PAUSE_BETWEEN_STEPS_MS = 900;

    private final Automaton automaton;
    private final EditorController editor;
    private final GraphView view;
    private EliminationView ui;
    private StateEliminator.Order order = StateEliminator.Order.MIN_DEGREE;
    private EliminationResult result;
    private int index = -1;
    private boolean active, animating, playing;
    private Timer timer;

    public EliminationController(Automaton automaton, EditorController editor, GraphView view) {
        this.automaton = automaton;
        this.editor = editor;
        this.view = view;
    }

    public void setUi(EliminationView ui) { this.ui = ui; }
    public boolean isActive() { return active; }
    public String getRegex() { return result == null ? null : result.regex(); }

    /** Ejecuta el algoritmo completo y muestra el primer paso. */
    public boolean prepare(StateEliminator.Order order) {
        stopTimer();
        this.order = order;
        if (automaton.getStates().isEmpty()) { ui.log("⚠ No hay estados en el lienzo."); return false; }
        if (automaton.getInitial() == null) { ui.log("⚠ Marca primero el estado inicial."); return false; }
        result = StateEliminator.run(automaton, order);
        index = -1; animating = false; playing = false; active = true;
        editor.setLocked(true);
        ui.clearLog();
        ui.showRegex(null);
        next();
        return true;
    }

    /** Avanza un paso del algoritmo (con animación si elimina un estado). */
    public void next() {
        if (!active || animating || index + 1 >= result.steps().size()) return;
        index++;
        EliminationStep st = result.steps().get(index);
        ui.setStepInfo("(" + (index + 1) + "/" + result.steps().size() + ")  " + st.title());
        ui.log("▶ " + st.title());
        ui.log("   " + st.detail());
        if (st.before() == null) { draw(st.after()); finishStep(); }
        else animateElimination(st);
    }

    public void play() {
        if (!active) return;
        if (index + 1 >= result.steps().size()) { prepare(order); }
        playing = true;
        ui.setBusy(true);
        next();
    }

    public void pause() { playing = false; stopTimer(); animating = false; ui.setBusy(false); }

    /** Salta directamente al resultado final (sin animar). */
    public void skipToEnd() {
        if (!active) return;
        stopTimer(); animating = false; playing = false;
        index = result.steps().size() - 1;
        EliminationStep last = result.steps().get(index);
        draw(last.after());
        ui.setStepInfo("(" + (index + 1) + "/" + result.steps().size() + ")  " + last.title());
        ui.log("⏭ Se omitió la animación. " + last.detail());
        publishRegex();
        ui.setBusy(false);
    }

    /** Vuelve al editor normal. */
    public void exit() {
        stopTimer();
        animating = false; playing = false; active = false;
        ui.setBusy(false);
        editor.setLocked(false);
    }

    // ------------------------------------------------------------------ animación
    private void animateElimination(EliminationStep st) {
        animating = true;
        ui.setBusy(true);
        int j = st.removedId();
        GnfaGraph before = st.before();
        draw(before);
        List<Integer> incident = new ArrayList<>();
        for (Edge e : before.edgeList()) if (e.from() == j || e.to() == j) incident.add(e.id());
        view.colorState(j, "#EF9A9A");
        for (int id : incident) view.colorEdge(id, "#F57C00");
        ui.log("   Eliminando " + before.nameOf(j) + "…");

        int[] ticks = {0};
        timer = new Timer(TICK_MS, e -> {
            ticks[0]++;
            if (ticks[0] <= HOLD_TICKS) return;
            int f = ticks[0] - HOLD_TICKS;
            if (f <= FADE_TICKS) { view.setFade(List.of(j), incident, 100 - f * 100 / FADE_TICKS); return; }
            stopTimer();
            draw(st.after());
            for (int id : st.updatedEdges()) view.colorEdge(id, "#2E7D32");
            for (String f2 : st.formulas()) ui.log("   " + f2);
            animating = false;
            finishStep();
        });
        timer.start();
    }

    private void finishStep() {
        boolean last = index + 1 >= result.steps().size();
        if (last) { publishRegex(); playing = false; ui.setBusy(false); return; }
        if (playing) {
            timer = new Timer(PAUSE_BETWEEN_STEPS_MS, e -> { stopTimer(); next(); });
            timer.setRepeats(false);
            timer.start();
        } else ui.setBusy(false);
    }

    private void publishRegex() {
        ui.showRegex(RegexOps.show(result.regex()));
        ui.log("✔ Expresión regular final:  " + RegexOps.show(result.regex()));
    }

    private void draw(GnfaGraph g) { view.render(g.nodes(), g.edgeList()); }
    private void stopTimer() { if (timer != null) { timer.stop(); timer = null; } }
}
