# Genera el diagrama de clases UML con Graphviz.  Uso: python3 uml.py
import subprocess
COL = {"model": ("#DCEBFA", "#2B6CB0"), "algorithm": ("#DDF2E3", "#2F855A"),
       "controller": ("#FDEBD0", "#C05621"), "view": ("#EBDDF7", "#6B46C1")}

def cls(name, pkg, fields=(), methods=(), stereo=None, disp=None):
    fill, line = COL[pkg]
    def rows(items):
        return "".join(f'<TR><TD ALIGN="LEFT" BALIGN="LEFT"><FONT POINT-SIZE="9">{i}</FONT></TD></TR>' for i in items) or '<TR><TD> </TD></TR>'
    st = f'<TR><TD><FONT POINT-SIZE="8">«{stereo}»</FONT></TD></TR>' if stereo else ""
    return (f'{name} [label=<<TABLE BORDER="1" CELLBORDER="0" CELLSPACING="0" CELLPADDING="3" BGCOLOR="{fill}" COLOR="{line}">'
            f'{st}<TR><TD><B>{(disp or name).replace("_", ".")}</B></TD></TR><HR/>{rows(fields)}<HR/>{rows(methods)}</TABLE>>];')

nodes = {
 "model": [
  cls("State","model",["-id: int","-name: String","-x, y: double","-initial, accepting: boolean"],["+copy(): State"]),
  cls("Transition","model",["-id, from, to: int","-label: String"],["+symbols(): List&lt;String&gt;","+isEpsilon(String): boolean"]),
  cls("EdgeRec","model",["id, from, to: int","label: String"],[],"record","Edge"),
  cls("Automaton","model",["-states: Map&lt;Integer,State&gt;","-transitions: Map&lt;Integer,Transition&gt;"],
      ["+addState(x,y)","+addTransition(from,to,label)","+toggleInitial(id) / toggleAccepting(id)","+getAlphabet(): SortedSet","+validateAsDfa(): List&lt;String&gt;"]),
 ],
 "algorithm": [
  cls("RegexOps","algorithm",[],["+union(a,b) / concat(a,b) / star(a)","+fromLabel(String)","+toJava(String)"],"utility"),
  cls("GnfaGraph","algorithm",["-nodes: Map&lt;Integer,State&gt;","-edges: Map&lt;Integer,Map&lt;Integer,String&gt;&gt;"],
      ["+fromAutomaton(Automaton)","+normalize()","+get(i,k) / put(i,k,r)","+predecessors(j) / successors(j)","+removeNode(j)"]),
  cls("EliminationStep","algorithm",["title, detail: String","before, after: GnfaGraph","removedId: int","formulas: List&lt;String&gt;"],[],"record"),
  cls("EliminationResult","algorithm",["steps: List&lt;EliminationStep&gt;","regex: String"],[],"record"),
  cls("StateEliminator","algorithm",[],["+run(Automaton, Order): EliminationResult","-pick(GnfaGraph, Order): int"],"utility"),
  cls("DfaSimulator","algorithm",[],["+run(Automaton, String): Result"],"utility"),
  cls("EquivalenceChecker","algorithm",[],["+regexAccepts(regex, input)","+exhaustive(Automaton, regex, n)"],"utility"),
 ],
 "controller": [
  cls("EditorController","controller",["-mode: Mode","-selection: Hit","-pendingSource: Integer"],["+setMode(Mode)","+addState(x,y)","+deleteSelection()","+refresh()"]),
  cls("SimulationController","controller",["-timer: Timer","-result: Result"],["+play(input, delay)","+step(input)","+reset()"]),
  cls("EliminationController","controller",["-result: EliminationResult","-index: int"],["+prepare(Order)","+next() / play() / pause()","+skipToEnd() / exit()"]),
  cls("EquivalenceController","controller",[],["+compare(regex, input)","+exhaustive(regex, n)"]),
 ],
 "view": [
  cls("GraphView","view",["-graph: mxGraph","-component: mxGraphComponent"],["+render(states, edges)","+colorState / colorEdge","+setFade(...)"]),
  cls("GraphView_Listener","view",[],["onMouse(...)","onStateMoved(...)","onDeleteKey()"],"interface"),
  cls("MainFrame","view",[],["-buildToolbar()","-buildMenu()"]),
  cls("SimulationPanel","view",[],["+showTrace() / appendLog()","+showResult()"]),
  cls("ConversionPanel","view",[],["+log() / setStepInfo()","+showRegex()"]),
  cls("EquivalencePanel","view",[],["+setRegex()","-evaluate() / exhaustive()"]),
 ],
}
edges = [
 # composición / agregación / asociación / dependencia / realización
 ("Automaton","State","arrowtail=diamond,dir=back,arrowhead=none","1..*"),
 ("Automaton","Transition","arrowtail=diamond,dir=back,arrowhead=none","0..*"),
 ("GnfaGraph","State","arrowtail=odiamond,dir=back,arrowhead=none","nodos"),
 ("EliminationResult","EliminationStep","arrowtail=diamond,dir=back,arrowhead=none","1..*"),
 ("EliminationStep","GnfaGraph","arrowhead=vee",""),
 ("GnfaGraph","EdgeRec","style=dashed,arrowhead=vee",""),
 ("GraphView","EdgeRec","style=dashed,arrowhead=vee,constraint=false",""),
 ("StateEliminator","GnfaGraph","style=dashed,arrowhead=vee",""),
 ("StateEliminator","EliminationResult","style=dashed,arrowhead=vee",""),
 ("GnfaGraph","RegexOps","style=dashed,arrowhead=vee",""),
 ("StateEliminator","RegexOps","style=dashed,arrowhead=vee",""),
 ("DfaSimulator","Automaton","style=dashed,arrowhead=vee",""),
 ("EquivalenceChecker","DfaSimulator","style=dashed,arrowhead=vee",""),
 ("EquivalenceChecker","RegexOps","style=dashed,arrowhead=vee",""),
 ("EditorController","Automaton","arrowhead=vee",""),
 ("EditorController","GraphView","arrowhead=vee",""),
 ("EditorController","GraphView_Listener","style=dashed,arrowhead=empty",""),
 ("GraphView","GraphView_Listener","arrowhead=vee,constraint=false",""),
 ("SimulationController","EditorController","arrowhead=vee",""),
 ("SimulationController","DfaSimulator","style=dashed,arrowhead=vee",""),
 ("EliminationController","EditorController","arrowhead=vee",""),
 ("EliminationController","StateEliminator","style=dashed,arrowhead=vee",""),
 ("EquivalenceController","EquivalenceChecker","style=dashed,arrowhead=vee",""),
 ("EquivalenceController","Automaton","arrowhead=vee",""),
 ("MainFrame","EditorController","arrowtail=diamond,dir=back,arrowhead=none",""),
 ("MainFrame","SimulationPanel","arrowtail=diamond,dir=back,arrowhead=none",""),
 ("MainFrame","ConversionPanel","arrowtail=diamond,dir=back,arrowhead=none",""),
 ("MainFrame","EquivalencePanel","arrowtail=diamond,dir=back,arrowhead=none",""),
 ("SimulationPanel","SimulationController","arrowhead=vee",""),
 ("ConversionPanel","EliminationController","arrowhead=vee",""),
 ("EquivalencePanel","EquivalenceController","arrowhead=vee",""),
]
titles = {"model":"MODELO (edu.automata.model)","algorithm":"ALGORITMOS (edu.automata.algorithm)",
          "controller":"CONTROLADORES (edu.automata.controller)","view":"VISTA (edu.automata.view)"}
dot = ['digraph G { rankdir=TB; nodesep=0.35; ranksep=0.8; splines=true; fontname="DejaVu Sans"; node [shape=plain, fontname="DejaVu Sans"]; edge [fontname="DejaVu Sans", fontsize=8, color="#444444"];']
for pkg, items in nodes.items():
    dot.append(f'subgraph cluster_{pkg} {{ label="{titles[pkg]}"; labeljust=l; fontsize=12; style="rounded,filled"; fillcolor="white"; color="{COL[pkg][1]}"; penwidth=1.5;')
    dot += items
    dot.append('}')
for a, b, attrs, lab in edges:
    dot.append(f'{a} -> {b} [{attrs}{",label=\"" + lab + "\"" if lab else ""}];')
dot.append('}')
open("uml.dot","w").write("\n".join(dot))
subprocess.run(["dot","-Tpng","-Gdpi=170","uml.dot","-o","img/uml.png"],check=True)
print("ok")
