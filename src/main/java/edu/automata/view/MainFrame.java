package edu.automata.view;

import edu.automata.controller.*;
import edu.automata.model.Automaton;
import edu.automata.model.Examples;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.function.Consumer;

/** Ventana principal: ensambla modelo, controladores y vistas (composición de MVC). */
public class MainFrame extends JFrame {
    private final Automaton automaton = new Automaton();
    private final GraphView graphView = new GraphView();
    private final EditorController editor = new EditorController(automaton, graphView);
    private final SimulationController simulation = new SimulationController(automaton, editor, graphView);
    private final EliminationController elimination = new EliminationController(automaton, editor, graphView);
    private final EquivalenceController equivalence = new EquivalenceController(automaton);
    private final JLabel status = new JLabel(" ");

    public MainFrame() {
        super("Autómatas Studio — DFA ⇄ Expresión Regular");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        editor.setStatusSink(status::setText);

        SimulationPanel simPanel = new SimulationPanel(simulation);
        ConversionPanel convPanel = new ConversionPanel(elimination);
        EquivalencePanel eqPanel = new EquivalencePanel(equivalence);
        convPanel.setOnRegex(shown -> eqPanel.setRegex(shown == null ? null : shown));
        editor.addStructureListener(() -> eqPanel.setRegex(null));   // si se edita el DFA, la regex deja de ser válida

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("1. Simulación", simPanel);
        tabs.addTab("2. Regex", convPanel);
        tabs.addTab("3. Equivalencia", eqPanel);
        tabs.setPreferredSize(new Dimension(430, 600));
        tabs.addChangeListener(e -> {
            if (tabs.getSelectedIndex() != 1 && elimination.isActive()) elimination.exit();
            if (tabs.getSelectedIndex() == 1) simulation.reset();
        });

        add(buildToolbar(), BorderLayout.NORTH);
        add(graphView, BorderLayout.CENTER);
        add(tabs, BorderLayout.EAST);
        status.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        add(status, BorderLayout.SOUTH);
        setJMenuBar(buildMenu());

        editor.setMode(EditorController.Mode.SELECT);
        editor.load(Examples.all().values().iterator().next());   // arranca con un ejemplo cargado
        setSize(1280, 780);
        setLocationRelativeTo(null);
    }

    private JToolBar buildToolbar() {
        JToolBar tb = new JToolBar();
        tb.setFloatable(false);
        ButtonGroup g = new ButtonGroup();
        tb.add(modeButton("Seleccionar", EditorController.Mode.SELECT, g, true));
        tb.add(modeButton("Estado (Q)", EditorController.Mode.ADD_STATE, g, false));
        tb.add(modeButton("Transición (δ)", EditorController.Mode.ADD_TRANSITION, g, false));
        tb.addSeparator();
        tb.add(action("Inicial (q0)", editor::toggleInitialOfSelection));
        tb.add(action("Aceptación (F)", editor::toggleAcceptingOfSelection));
        tb.add(action("Editar", editor::editSelection));
        tb.add(action("Eliminar", editor::deleteSelection));
        tb.addSeparator();
        tb.add(action("Limpiar todo", editor::clearAll));
        return tb;
    }

    private JToggleButton modeButton(String text, EditorController.Mode mode, ButtonGroup g, boolean selected) {
        JToggleButton b = new JToggleButton(text, selected);
        b.setFocusable(false);
        b.addActionListener(e -> editor.setMode(mode));
        g.add(b);
        return b;
    }

    private JButton action(String text, Runnable r) {
        JButton b = new JButton(text);
        b.setFocusable(false);
        b.addActionListener(e -> r.run());
        return b;
    }

    private JMenuBar buildMenu() {
        JMenuBar bar = new JMenuBar();
        JMenu file = new JMenu("Archivo");
        JMenu examples = new JMenu("Cargar ejemplo");
        for (Map.Entry<String, Consumer<Automaton>> ex : Examples.all().entrySet()) {
            JMenuItem item = new JMenuItem(ex.getKey());
            item.addActionListener(e -> editor.load(ex.getValue()));
            examples.add(item);
        }
        file.add(examples);
        JMenuItem exit = new JMenuItem("Salir");
        exit.addActionListener(e -> dispose());
        file.add(exit);
        bar.add(file);

        JMenu help = new JMenu("Ayuda");
        JMenuItem guide = new JMenuItem("Guía rápida");
        guide.addActionListener(e -> JOptionPane.showMessageDialog(this, """
                1. Modo «Estado»: clic en el lienzo para crear estados (el primero es el inicial).
                2. Modo «Transición»: clic en origen y luego en destino; escribe el símbolo (a, o a,b; 'eps' = ε).
                3. Modo «Seleccionar»: arrastra estados; doble clic para renombrar/editar; clic derecho = menú;
                   Supr elimina lo seleccionado. «Inicial» y «Aceptación» cambian las propiedades del estado.
                4. Pestaña Simulación: escribe una cadena y pulsa Simular (o Paso).
                5. Pestaña Regex: Iniciar → Siguiente paso / Animar todo para ver la eliminación de estados.
                6. Pestaña Equivalencia: compara la cadena en el DFA y en la regex resultante.""",
                "Guía rápida", JOptionPane.INFORMATION_MESSAGE));
        help.add(guide);
        bar.add(help);
        return bar;
    }
}
