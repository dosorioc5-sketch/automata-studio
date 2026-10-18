package edu.automata.model;

/** Estado (nodo) de un autómata. Las coordenadas son el CENTRO del nodo en el lienzo. */
public class State {
    private final int id;
    private String name;
    private double x, y;
    private boolean initial, accepting;

    public State(int id, String name, double x, double y) {
        this.id = id; this.name = name; this.x = x; this.y = y;
    }

    public State copy() {
        State s = new State(id, name, x, y);
        s.initial = initial; s.accepting = accepting;
        return s;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getX() { return x; }
    public double getY() { return y; }
    public void setPosition(double x, double y) { this.x = x; this.y = y; }
    public boolean isInitial() { return initial; }
    public void setInitial(boolean initial) { this.initial = initial; }
    public boolean isAccepting() { return accepting; }
    public void setAccepting(boolean accepting) { this.accepting = accepting; }
}
