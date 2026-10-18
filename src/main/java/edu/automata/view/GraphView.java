package edu.automata.view;

import com.mxgraph.model.mxGeometry;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxPoint;
import com.mxgraph.view.mxGraph;
import edu.automata.model.Edge;
import edu.automata.model.State;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

/**
 * Lienzo interactivo basado en JGraphX. Es una vista "tonta": dibuja los estados/aristas que
 * se le entregan y avisa de los eventos del usuario a través de {@link Listener}.
 * No conoce el modelo matemático ni los algoritmos.
 */
public class GraphView extends JPanel {
    public static final int SIZE = 56;
    private static final int R = SIZE / 2;
    private static final String FILL = "#E3F2FD";

    public enum Kind { NONE, STATE, EDGE }
    public record Hit(Kind kind, int id) { public static final Hit NONE = new Hit(Kind.NONE, -1); }
    public record MenuAction(String text, Runnable action) { }

    public interface Listener {
        void onMouse(Hit hit, double x, double y, int clickCount, boolean popup, MouseEvent e);
        void onStateMoved(int id, double cx, double cy);
        void onDeleteKey();
        void onEscapeKey();
    }

    private final Set<Object> helpers = new HashSet<>();
    private boolean movable = true;
    private final mxGraph graph = new mxGraph() {
        @Override public boolean isCellSelectable(Object c) { return !helpers.contains(c) && super.isCellSelectable(c); }
        @Override public boolean isCellMovable(Object c) {
            return movable && !helpers.contains(c) && !getModel().isEdge(c) && super.isCellMovable(c);
        }
    };
    private final mxGraphComponent component = new mxGraphComponent(graph);

    private final Map<Integer, Object> stateCells = new HashMap<>();
    private final Map<Integer, Object> edgeCells = new HashMap<>();
    private final Map<Object, Integer> cellToState = new HashMap<>();
    private final Map<Object, Integer> cellToEdge = new HashMap<>();
    private final Map<Object, Integer> helperOwner = new HashMap<>();
    private Listener listener;
    private List<State> lastStates = new ArrayList<>();
    private List<Edge> lastEdges = new ArrayList<>();

    public GraphView() {
        super(new BorderLayout());
        graph.setCellsEditable(false);
        graph.setCellsResizable(false);
        graph.setCellsCloneable(false);
        graph.setCellsDisconnectable(false);
        graph.setAllowDanglingEdges(false);
        graph.setDisconnectOnMove(false);
        graph.setEdgeLabelsMovable(false);
        graph.setDropEnabled(false);
        graph.setKeepEdgesInBackground(true);
        component.setConnectable(false);
        component.setToolTips(false);
        component.getGraphHandler().setCloneEnabled(false);
        component.getViewport().setOpaque(true);
        component.getViewport().setBackground(Color.WHITE);
        component.setBorder(BorderFactory.createEmptyBorder());
        add(component, BorderLayout.CENTER);

        component.getGraphControl().addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) fire(e, false);
            }
            @Override public void mousePressed(MouseEvent e) { if (e.isPopupTrigger()) fire(e, true); }
            @Override public void mouseReleased(MouseEvent e) { if (e.isPopupTrigger()) fire(e, true); }
        });

        graph.addListener(mxEvent.CELLS_MOVED, (sender, evt) -> {
            Object[] cells = (Object[]) evt.getProperty("cells");
            List<int[]> moved = new ArrayList<>();
            List<double[]> pos = new ArrayList<>();
            for (Object c : cells) {
                Integer id = cellToState.get(c);
                if (id == null) continue;
                mxGeometry g = graph.getModel().getGeometry(c);
                moved.add(new int[]{id});
                pos.add(new double[]{g.getX() + R, g.getY() + R});
            }
            SwingUtilities.invokeLater(() -> {
                for (int i = 0; i < moved.size(); i++)
                    if (listener != null) listener.onStateMoved(moved.get(i)[0], pos.get(i)[0], pos.get(i)[1]);
            });
        });

        bindKey(KeyStroke.getKeyStroke("DELETE"), () -> { if (listener != null) listener.onDeleteKey(); }, true);
        bindKey(KeyStroke.getKeyStroke("BACK_SPACE"), () -> { if (listener != null) listener.onDeleteKey(); }, true);
        bindKey(KeyStroke.getKeyStroke("ESCAPE"), () -> { if (listener != null) listener.onEscapeKey(); }, false);
    }

    private void bindKey(KeyStroke ks, Runnable r, boolean ignoreWhenTyping) {
        String name = "key-" + ks;
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(ks, name);
        getActionMap().put(name, new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                Component f = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
                if (ignoreWhenTyping && f instanceof JTextComponent) return;
                r.run();
            }
        });
    }

    public void setListener(Listener l) { this.listener = l; }

    // ------------------------------------------------------------------ eventos
    private void fire(MouseEvent e, boolean popup) {
        if (listener == null || !graph.isEnabled()) return;
        double sc = graph.getView().getScale();
        mxPoint tr = graph.getView().getTranslate();
        double gx = e.getX() / sc - tr.getX(), gy = e.getY() / sc - tr.getY();
        listener.onMouse(hit(e), gx, gy, e.getClickCount(), popup, e);
    }

    private Hit hit(MouseEvent e) {
        Object cell = component.getCellAt(e.getX(), e.getY());
        if (cell == null) return Hit.NONE;
        if (cellToState.containsKey(cell)) return new Hit(Kind.STATE, cellToState.get(cell));
        if (helperOwner.containsKey(cell)) return new Hit(Kind.STATE, helperOwner.get(cell));
        if (cellToEdge.containsKey(cell)) return new Hit(Kind.EDGE, cellToEdge.get(cell));
        return Hit.NONE;
    }

    // ------------------------------------------------------------------ dibujo
    public void render(Collection<State> states, Collection<Edge> edges) {
        lastStates = new ArrayList<>(states);
        lastEdges = new ArrayList<>(edges);
        Object parent = graph.getDefaultParent();
        graph.getModel().beginUpdate();
        try {
            graph.removeCells(graph.getChildCells(parent, true, true));
            stateCells.clear(); edgeCells.clear(); cellToState.clear(); cellToEdge.clear();
            helpers.clear(); helperOwner.clear();

            for (State s : lastStates) {
                int fs = s.getName().length() > 5 ? 9 : (s.getName().length() > 3 ? 11 : 14);
                String style = "shape=" + (s.isAccepting() ? "doubleEllipse" : "ellipse")
                        + ";perimeter=ellipsePerimeter;fillColor=" + FILL + ";strokeColor=#37474F;strokeWidth=2;"
                        + "fontColor=#212121;fontStyle=1;fontSize=" + fs + ";";
                Object v = graph.insertVertex(parent, null, s.getName(), s.getX() - R, s.getY() - R, SIZE, SIZE, style);
                stateCells.put(s.getId(), v);
                cellToState.put(v, s.getId());
                if (s.isInitial()) {
                    Object h = graph.insertVertex(parent, null, "", s.getX() - R - 55, s.getY(), 1, 1,
                            "fillColor=none;strokeColor=none;");
                    Object arrow = graph.insertEdge(parent, null, "", h, v,
                            "endArrow=classic;strokeColor=#37474F;strokeWidth=2;");
                    helpers.add(h); helpers.add(arrow);
                    helperOwner.put(h, s.getId()); helperOwner.put(arrow, s.getId());
                }
            }
            Map<Integer, State> byId = new HashMap<>();
            for (State s : lastStates) byId.put(s.getId(), s);
            // Agrupa aristas entre el mismo par de estados (sin importar el sentido) para separarlas en arcos
            Map<String, List<Edge>> groups = new LinkedHashMap<>();
            Map<Integer, Integer> loopsPerState = new HashMap<>();
            for (Edge e : lastEdges)
                if (e.from() != e.to())
                    groups.computeIfAbsent(Math.min(e.from(), e.to()) + "_" + Math.max(e.from(), e.to()), k -> new ArrayList<>()).add(e);

            for (Edge e : lastEdges) {
                Object src = stateCells.get(e.from()), tgt = stateCells.get(e.to());
                State a = byId.get(e.from()), b = byId.get(e.to());
                if (src == null || tgt == null) continue;
                List<mxPoint> pts = new ArrayList<>();
                if (e.from() == e.to()) {                                   // bucle: arco grande sobre el nodo
                    int n = loopsPerState.merge(e.from(), 1, Integer::sum) - 1;
                    // un único punto de control sobre el nodo: el estilo Loop de JGraphX dibuja el lazo hacia arriba
                    pts.add(new mxPoint(a.getX() + 24, a.getY() - R - 38 - n * 30));
                } else {
                    List<Edge> grp = groups.get(Math.min(e.from(), e.to()) + "_" + Math.max(e.from(), e.to()));
                    if (grp.size() > 1) {                                   // aristas paralelas/opuestas: arcos a ambos lados
                        State lo = byId.get(Math.min(e.from(), e.to())), hi = byId.get(Math.max(e.from(), e.to()));
                        double dx = hi.getX() - lo.getX(), dy = hi.getY() - lo.getY(), len = Math.max(1, Math.hypot(dx, dy));
                        double off = (grp.indexOf(e) - (grp.size() - 1) / 2.0) * 90;
                        pts.add(new mxPoint((lo.getX() + hi.getX()) / 2 - dy / len * off,
                                            (lo.getY() + hi.getY()) / 2 + dx / len * off));
                    } else if (crossesAnotherState(a, b)) {                 // evita atravesar nodos intermedios: arco por debajo
                        pts.add(new mxPoint((a.getX() + b.getX()) / 2, Math.max(a.getY(), b.getY()) + 105));
                    }
                }
                String style = (pts.isEmpty() ? "" : (e.from() == e.to() ? "rounded=1;" : "shape=curve;"))
                        + "endArrow=classic;strokeColor=#37474F;strokeWidth=2;fontSize=13;fontColor=#1B1B1B;"
                        + "labelBackgroundColor=#FFFFFF;";
                Object c = graph.insertEdge(parent, null, e.label(), src, tgt, style);
                if (!pts.isEmpty()) {
                    mxGeometry geo = new mxGeometry();
                    geo.setRelative(true);
                    geo.setPoints(pts);
                    graph.getModel().setGeometry(c, geo);
                }
                edgeCells.put(e.id(), c);
                cellToEdge.put(c, e.id());
            }
        } finally {
            graph.getModel().endUpdate();
        }
        graph.refresh();
    }

    private boolean crossesAnotherState(State a, State b) {
        for (State s : lastStates) {
            if (s.getId() == a.getId() || s.getId() == b.getId()) continue;
            double dx = b.getX() - a.getX(), dy = b.getY() - a.getY();
            double t = ((s.getX() - a.getX()) * dx + (s.getY() - a.getY()) * dy) / Math.max(1, dx * dx + dy * dy);
            if (t <= 0 || t >= 1) continue;
            double px = a.getX() + t * dx, py = a.getY() + t * dy;
            if (Math.hypot(s.getX() - px, s.getY() - py) < R + 8) return true;
        }
        return false;
    }

    /** Vuelve a dibujar con los últimos datos (limpia colores/resaltados). */
    public void refresh() { render(lastStates, lastEdges); }

    public void colorState(int id, String fill) {
        Object c = stateCells.get(id);
        if (c != null) graph.setCellStyles(mxConstants.STYLE_FILLCOLOR, fill, new Object[]{c});
    }

    public void colorEdge(int id, String color) {
        Object c = edgeCells.get(id);
        if (c == null) return;
        graph.setCellStyles(mxConstants.STYLE_STROKECOLOR, color, new Object[]{c});
        graph.setCellStyles(mxConstants.STYLE_FONTCOLOR, color, new Object[]{c});
        graph.setCellStyles(mxConstants.STYLE_STROKEWIDTH, "4", new Object[]{c});
    }

    /** Opacidad (0-100) de un estado y de aristas: sirve para la animación de "desaparecer". */
    public void setFade(Collection<Integer> stateIds, Collection<Integer> edgeIds, int percent) {
        List<Object> cells = new ArrayList<>();
        for (int id : stateIds) if (stateCells.containsKey(id)) cells.add(stateCells.get(id));
        for (int id : edgeIds) if (edgeCells.containsKey(id)) cells.add(edgeCells.get(id));
        Object[] arr = cells.toArray();
        graph.setCellStyles(mxConstants.STYLE_OPACITY, String.valueOf(percent), arr);
        graph.setCellStyles(mxConstants.STYLE_TEXT_OPACITY, String.valueOf(percent), arr);
    }

    public void select(Hit hit) {
        Object c = hit.kind() == Kind.STATE ? stateCells.get(hit.id())
                : hit.kind() == Kind.EDGE ? edgeCells.get(hit.id()) : null;
        if (c == null) graph.clearSelection(); else graph.setSelectionCell(c);
    }

    public void setInteractive(boolean on) { graph.setEnabled(on); }
    public void setMovable(boolean on) { this.movable = on; }

    // ------------------------------------------------------------------ diálogos
    public String promptText(String title, String message, String initial) {
        Object r = JOptionPane.showInputDialog(this, message, title, JOptionPane.QUESTION_MESSAGE, null, null, initial);
        return r == null ? null : r.toString().trim();
    }

    public boolean confirm(String message) {
        return JOptionPane.showConfirmDialog(this, message, "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    public void showPopup(MouseEvent e, List<MenuAction> actions) {
        JPopupMenu menu = new JPopupMenu();
        for (MenuAction a : actions) {
            JMenuItem item = new JMenuItem(a.text());
            item.addActionListener(ev -> a.action().run());
            menu.add(item);
        }
        menu.show(e.getComponent(), e.getX(), e.getY());
    }
}
