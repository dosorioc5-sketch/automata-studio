package edu.automata.controller;

import edu.automata.model.Automaton;
import edu.automata.model.Edge;
import edu.automata.model.State;
import edu.automata.model.Transition;
import edu.automata.view.GraphView;
import edu.automata.view.GraphView.Hit;
import edu.automata.view.GraphView.Kind;
import edu.automata.view.GraphView.MenuAction;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** Controlador del lienzo: traduce gestos del usuario en cambios sobre el modelo {@link Automaton}. */
public class EditorController implements GraphView.Listener {
    public enum Mode { SELECT, ADD_STATE, ADD_TRANSITION }

    private static final String LABEL_HELP =
            "Símbolo(s) del alfabeto separados por coma (ej: a,b), 'eps' para ε,\no una expresión parcial (ej: ab*):";

    private final Automaton automaton;
    private final GraphView view;
    private Mode mode = Mode.SELECT;
    private Integer pendingSource;
    private Hit selection = Hit.NONE;
    private boolean locked;
    private Runnable overlay;
    private final List<Runnable> structureListeners = new ArrayList<>();
    private Consumer<String> status = s -> { };

    public EditorController(Automaton automaton, GraphView view) {
        this.automaton = automaton;
        this.view = view;
        view.setListener(this);
    }

    // ---------------------------------------------------------------- configuración
    public void setStatusSink(Consumer<String> sink) { this.status = sink; }
    public void addStructureListener(Runnable r) { structureListeners.add(r); }
    public Mode getMode() { return mode; }
    public Automaton getAutomaton() { return automaton; }

    /** Código adicional que se ejecuta tras cada redibujado (p. ej. resaltado de la simulación). */
    public void setOverlay(Runnable overlay) { this.overlay = overlay; }

    /** Bloquea la edición mientras otro controlador (conversión) usa el lienzo. */
    public void setLocked(boolean locked) {
        this.locked = locked;
        view.setInteractive(!locked);
        if (!locked) refresh();
    }

    public void setMode(Mode m) {
        this.mode = m;
        this.pendingSource = null;
        view.setMovable(m == Mode.SELECT);
        status.accept(switch (m) {
            case SELECT -> "Modo SELECCIONAR: arrastra estados, doble clic para editar, clic derecho para más opciones.";
            case ADD_STATE -> "Modo ESTADO: haz clic en un espacio vacío del lienzo para crear un estado.";
            case ADD_TRANSITION -> "Modo TRANSICIÓN: clic en el estado origen y luego en el destino (mismo estado = bucle). Esc cancela.";
        });
        refresh();
    }

    // ---------------------------------------------------------------- dibujo
    public void refresh() {
        List<Edge> edges = new ArrayList<>();
        for (Transition t : automaton.getTransitions())
            edges.add(new Edge(t.getId(), t.getFrom(), t.getTo(), t.getLabel()));
        view.render(automaton.getStates(), edges);
        if (pendingSource != null) view.colorState(pendingSource, "#FFF59D");
        if (overlay != null) overlay.run();
        view.select(selection);
    }

    private void structureChanged() {
        for (Runnable r : structureListeners) r.run();
        refresh();
    }

    // ---------------------------------------------------------------- eventos de la vista
    @Override
    public void onMouse(Hit hit, double x, double y, int clicks, boolean popup, MouseEvent e) {
        if (locked) return;
        if (popup) { select(hit); view.showPopup(e, menuFor(hit, x, y)); return; }
        switch (mode) {
            case ADD_STATE -> { if (hit.kind() == Kind.NONE) addState(x, y); else select(hit); }
            case ADD_TRANSITION -> handleTransitionClick(hit);
            case SELECT -> {
                select(hit);
                if (clicks == 2) {
                    if (hit.kind() == Kind.EDGE) editLabel(hit.id());
                    else if (hit.kind() == Kind.STATE) rename(hit.id());
                }
            }
        }
    }

    @Override
    public void onStateMoved(int id, double cx, double cy) {
        State s = automaton.getState(id);
        if (s == null || locked) return;
        s.setPosition(Math.max(40, cx), Math.max(40, cy));
        refresh();
    }

    @Override public void onDeleteKey() { if (!locked) deleteSelection(); }

    @Override
    public void onEscapeKey() {
        if (pendingSource != null) { pendingSource = null; refresh(); status.accept("Transición cancelada."); }
    }

    private List<MenuAction> menuFor(Hit hit, double x, double y) {
        List<MenuAction> m = new ArrayList<>();
        if (hit.kind() == Kind.STATE) {
            int id = hit.id();
            m.add(new MenuAction("Marcar / desmarcar como inicial", () -> toggleInitial(id)));
            m.add(new MenuAction("Marcar / desmarcar como de aceptación", () -> toggleAccepting(id)));
            m.add(new MenuAction("Renombrar…", () -> rename(id)));
            m.add(new MenuAction("Eliminar estado", () -> deleteSelection()));
        } else if (hit.kind() == Kind.EDGE) {
            int id = hit.id();
            m.add(new MenuAction("Editar etiqueta…", () -> editLabel(id)));
            m.add(new MenuAction("Eliminar transición", () -> deleteSelection()));
        } else {
            m.add(new MenuAction("Agregar estado aquí", () -> addState(x, y)));
        }
        return m;
    }

    // ---------------------------------------------------------------- operaciones sobre el modelo
    public void select(Hit hit) { selection = hit; view.select(hit); }
    public Hit getSelection() { return selection; }

    public void addState(double x, double y) {
        State s = automaton.addState(Math.max(40, x), Math.max(40, y));
        if (automaton.getStates().size() == 1) s.setInitial(true);   // el primero es el inicial por defecto
        selection = new Hit(Kind.STATE, s.getId());
        structureChanged();
        status.accept("Estado " + s.getName() + " creado.");
    }

    private void handleTransitionClick(Hit hit) {
        if (hit.kind() != Kind.STATE) { pendingSource = null; refresh(); return; }
        if (pendingSource == null) {
            pendingSource = hit.id();
            refresh();
            status.accept("Origen: " + automaton.getState(hit.id()).getName() + ". Ahora haz clic en el estado destino.");
            return;
        }
        int from = pendingSource, to = hit.id();
        pendingSource = null;
        String label = view.promptText("Nueva transición", LABEL_HELP, "");
        if (label == null || label.isBlank()) { refresh(); return; }
        Transition t = automaton.addTransition(from, to, label);
        selection = new Hit(Kind.EDGE, t.getId());
        structureChanged();
    }

    public void toggleInitial(int id) { automaton.toggleInitial(id); structureChanged(); }
    public void toggleAccepting(int id) { automaton.toggleAccepting(id); structureChanged(); }

    public void toggleInitialOfSelection() { if (selection.kind() == Kind.STATE) toggleInitial(selection.id()); else notSelected("un estado"); }
    public void toggleAcceptingOfSelection() { if (selection.kind() == Kind.STATE) toggleAccepting(selection.id()); else notSelected("un estado"); }

    public void editSelection() {
        if (selection.kind() == Kind.EDGE) editLabel(selection.id());
        else if (selection.kind() == Kind.STATE) rename(selection.id());
        else notSelected("un estado o una transición");
    }

    private void notSelected(String what) { status.accept("Primero selecciona " + what + " en el lienzo."); }

    private void rename(int id) {
        State s = automaton.getState(id);
        if (s == null) return;
        String name = view.promptText("Renombrar estado", "Nuevo nombre:", s.getName());
        if (name == null || name.isBlank()) return;
        s.setName(name);
        structureChanged();
    }

    private void editLabel(int id) {
        Transition t = automaton.getTransition(id);
        if (t == null) return;
        String label = view.promptText("Editar transición", LABEL_HELP, t.getLabel());
        if (label == null || label.isBlank()) return;
        t.setLabel(label);
        structureChanged();
    }

    public void deleteSelection() {
        if (selection.kind() == Kind.STATE) automaton.removeState(selection.id());
        else if (selection.kind() == Kind.EDGE) automaton.removeTransition(selection.id());
        else { notSelected("un estado o una transición"); return; }
        selection = Hit.NONE;
        structureChanged();
    }

    public void clearAll() {
        if (automaton.getStates().isEmpty() || view.confirm("¿Borrar todo el autómata?")) {
            automaton.clear();
            selection = Hit.NONE; pendingSource = null;
            structureChanged();
        }
    }

    public void load(Consumer<Automaton> builder) {
        builder.accept(automaton);
        selection = Hit.NONE; pendingSource = null;
        structureChanged();
    }
}
