# Genera Documentacion_Proyecto.pdf.   Uso: python3 build_docs.py   (requiere: pip install reportlab pillow)
from reportlab.lib.pagesizes import letter
from reportlab.lib.styles import ParagraphStyle
from reportlab.lib.units import cm
from reportlab.lib import colors
from reportlab.lib.enums import TA_JUSTIFY, TA_CENTER
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.platypus import (SimpleDocTemplate, Paragraph, Spacer, PageBreak, Image, Table,
                                TableStyle, Preformatted, KeepTogether)
from PIL import Image as PILImage

# ====== EDITA ESTOS DATOS ======
ESTUDIANTE = "Nombre del estudiante"
CURSO = "Lenguajes Formales y Autómatas"
FECHA = "Octubre de 2026"
REPO = "https://github.com/usuario/automata-studio"
# ================================

FD = "/usr/share/fonts/truetype/dejavu/"
pdfmetrics.registerFont(TTFont("DV", FD + "DejaVuSans.ttf"))
pdfmetrics.registerFont(TTFont("DVB", FD + "DejaVuSans-Bold.ttf"))
pdfmetrics.registerFont(TTFont("DVM", FD + "DejaVuSansMono.ttf"))
pdfmetrics.registerFontFamily("DV", normal="DV", bold="DVB", italic="DV", boldItalic="DVB")

BLUE = colors.HexColor("#1F4E79")
body = ParagraphStyle("body", fontName="DV", fontSize=9.6, leading=14, alignment=TA_JUSTIFY, spaceAfter=6)
h1 = ParagraphStyle("h1", fontName="DVB", fontSize=16, leading=20, textColor=BLUE, spaceBefore=6, spaceAfter=10, keepWithNext=1)
h2 = ParagraphStyle("h2", fontName="DVB", fontSize=12, leading=16, textColor=BLUE, spaceBefore=10, spaceAfter=6, keepWithNext=1)
cap = ParagraphStyle("cap", fontName="DV", fontSize=8.5, leading=11, alignment=TA_CENTER, textColor=colors.HexColor("#555555"), spaceAfter=10)
code = ParagraphStyle("code", fontName="DVM", fontSize=8.2, leading=11, backColor=colors.HexColor("#F3F5F7"),
                      borderColor=colors.HexColor("#D0D7DE"), borderWidth=0.6, borderPadding=6, spaceBefore=4, spaceAfter=10)
cell = ParagraphStyle("cell", fontName="DV", fontSize=8.6, leading=11.5)
cellb = ParagraphStyle("cellb", parent=cell, fontName="DVB", textColor=colors.white)
bullet = ParagraphStyle("bullet", parent=body, leftIndent=14, bulletIndent=2, spaceAfter=3, alignment=0)

def P(t, s=body): return Paragraph(t, s)
def B(items): return [Paragraph(i, bullet, bulletText="•") for i in items]
def M(t): return f'<font face="DVM" size="8.6">{t}</font>'
def fig(path, width_cm, caption):
    w, h = PILImage.open(path).size
    img = Image(path, width=width_cm * cm, height=width_cm * cm * h / w)
    return KeepTogether([img, Spacer(1, 3), P(caption, cap)])
def table(rows, widths, header=True):
    data = [[Paragraph(str(c), cellb if (header and r == 0) else cell) for c in row] for r, row in enumerate(rows)]
    t = Table(data, colWidths=[w * cm for w in widths], repeatRows=1 if header else 0)
    st = [("GRID", (0, 0), (-1, -1), 0.5, colors.HexColor("#B8C4D0")), ("VALIGN", (0, 0), (-1, -1), "TOP"),
          ("ROWBACKGROUNDS", (0, 1), (-1, -1), [colors.white, colors.HexColor("#F2F6FA")]),
          ("TOPPADDING", (0, 0), (-1, -1), 4), ("BOTTOMPADDING", (0, 0), (-1, -1), 4)]
    if header: st.append(("BACKGROUND", (0, 0), (-1, 0), BLUE))
    t.setStyle(TableStyle(st))
    return t

def footer(c, d):
    c.saveState(); c.setFont("DV", 8); c.setFillColor(colors.HexColor("#777777"))
    c.drawString(2.2 * cm, 1.3 * cm, "Autómatas Studio — Documentación del proyecto")
    c.drawRightString(letter[0] - 2.2 * cm, 1.3 * cm, f"Página {d.page}")
    c.restoreState()

S = []
# ------------------------------------------------------------------ portada
S += [Spacer(1, 5 * cm),
      P("Autómatas Studio", ParagraphStyle("t", fontName="DVB", fontSize=30, leading=36, textColor=BLUE, alignment=TA_CENTER)),
      P("Editor, simulador y conversor de DFA a Expresión Regular<br/>mediante eliminación de estados",
        ParagraphStyle("st", fontName="DV", fontSize=13, leading=19, alignment=TA_CENTER, spaceAfter=40)),
      P("Documentación del proyecto", ParagraphStyle("d", fontName="DVB", fontSize=14, alignment=TA_CENTER, spaceAfter=40)),
      table([["Estudiante", ESTUDIANTE], ["Curso", CURSO], ["Fecha", FECHA], ["Repositorio", REPO],
             ["Tecnología", "Java 17 · Swing · JGraphX 4.2.2 · Maven"]], [4, 11], header=False),
      PageBreak()]

# ------------------------------------------------------------------ 1. resumen
S += [P("1. Resumen del proyecto", h1),
      P("Autómatas Studio es una aplicación de escritorio en Java que permite <b>dibujar un autómata finito determinista (DFA)</b> "
        "sobre un lienzo interactivo, <b>simular</b> cadenas con animación, <b>convertirlo a una expresión regular</b> mediante el "
        "algoritmo de eliminación de estados (con animación paso a paso) y <b>verificar la equivalencia</b> entre el DFA y la regex "
        "usando el motor <font face='DVM'>java.util.regex</font>. Con ello se aplica la equivalencia entre lenguajes regulares, "
        "autómatas finitos y expresiones regulares."),
      P("Funcionalidades principales", h2)]
S += B(["<b>Lienzo interactivo:</b> crear estados con clic, conectar transiciones (incluidos bucles y aristas opuestas), marcar estado inicial y de aceptación, mover, renombrar, editar y eliminar.",
        "<b>Simulación:</b> recorrido animado símbolo a símbolo, resaltando el estado actual y la transición usada; veredicto ACEPTADA / RECHAZADA.",
        "<b>Conversión a regex:</b> normalización (q_start, q_accept), eliminación iterativa con la fórmula R<sub>ik</sub>' = R<sub>ik</sub> ∪ (R<sub>ij</sub>·(R<sub>jj</sub>)*·R<sub>jk</sub>), animación del desvanecimiento de nodos y etiquetas actualizadas, y resultado final.",
        "<b>Equivalencia:</b> prueba de una cadena en ambos modelos y verificación exhaustiva de todas las cadenas hasta una longitud dada."])

# ------------------------------------------------------------------ 2. arquitectura
S += [P("2. Arquitectura del software", h1),
      P("El proyecto sigue el patrón <b>MVC</b> con una separación estricta en cuatro paquetes. La regla de dependencia es: "
        "el <b>modelo</b> y los <b>algoritmos</b> no conocen ninguna clase gráfica (no importan Swing ni JGraphX), "
        "los <b>controladores</b> coordinan y la <b>vista</b> solo dibuja y reporta eventos."),
      table([["Paquete", "Responsabilidad", "Clases principales"],
             ["model", "Representación matemática A = (Q, Σ, δ, q0, F). Sin lógica gráfica.", "State, Transition, Automaton, Edge, Examples"],
             ["algorithm", "Lógica pura: álgebra de regex, grafo GNFA, eliminación de estados, simulación, equivalencia. Totalmente testeable sin GUI.", "RegexOps, GnfaGraph, StateEliminator, EliminationStep, DfaSimulator, EquivalenceChecker"],
             ["controller", "Traducen gestos del usuario en cambios del modelo y orquestan las animaciones (javax.swing.Timer).", "EditorController, SimulationController, EliminationController, EquivalenceController"],
             ["view", "Componentes Swing + JGraphX. Dibujan lo que se les entrega y notifican eventos mediante interfaces.", "MainFrame, GraphView, SimulationPanel, ConversionPanel, EquivalencePanel"]],
            [2.4, 8.2, 6.6]),
      Spacer(1, 8),
      P("Desacoplamiento mediante interfaces", h2),
      P("Los controladores no dependen de paneles concretos: <font face='DVM'>SimulationController.SimulationView</font> y "
        "<font face='DVM'>EliminationController.EliminationView</font> son interfaces implementadas por los paneles, y "
        "<font face='DVM'>GraphView.Listener</font> permite que el lienzo avise de clics, arrastres y teclas sin conocer al "
        "<font face='DVM'>EditorController</font> que los atiende. Los pasos del algoritmo "
        "(<font face='DVM'>EliminationStep</font>) son <i>records</i> con instantáneas del grafo: la animación solo los reproduce."),
      PageBreak(),
      P("Diagrama de clases UML", h2),
      fig("img/uml.png", 16.8, "Figura 1. Diagrama de clases. Rombo lleno = composición, rombo vacío = agregación, "
                               "flecha continua = asociación, flecha punteada = dependencia, triángulo punteado = realización de interfaz."),
      PageBreak()]

# ------------------------------------------------------------------ 3. estructuras de datos
S += [P("3. Estructuras de datos y algoritmo de reducción", h1),
      P("3.1 Representación del autómata (modelo)", h2),
      P("<font face='DVM'>Automaton</font> guarda los estados y las transiciones en dos <font face='DVM'>LinkedHashMap&lt;Integer, …&gt;</font> "
        "indexados por identificador. Se eligió <i>LinkedHashMap</i> porque ofrece acceso, inserción y borrado en O(1) y conserva el "
        "orden de creación (útil para la estrategia «orden de creación» y para mostrar siempre los estados en el mismo orden). "
        "Las transiciones referencian estados por <i>id</i> (no por objeto), lo que simplifica borrar un estado: basta eliminar las "
        "transiciones cuyo origen o destino sea ese id. El alfabeto Σ no se almacena: se <b>infiere</b> de las etiquetas."),
      P("3.2 Grafo generalizado GnfaGraph", h2),
      P("Para el algoritmo se usa un grafo cuyas aristas llevan <b>expresiones regulares</b> en lugar de símbolos. Se implementa como "
        "<b>lista de adyacencia con mapas anidados</b>:"),
      Preformatted("Map<Integer, State>                    nodes   // q_start = -1, q_accept = -2\n"
                   "Map<Integer, Map<Integer, String>>     edges   // edges.get(i).get(k) = R_ik   (sin entrada ⇒ ∅)", code),
      P("Esta estructura da acceso O(1) a R<sub>ik</sub> y permite obtener los predecesores y sucesores de un estado sin recorrer "
        "toda la matriz. Una matriz n×n sería más simple pero desperdiciaría memoria en grafos dispersos (lo habitual en DFAs), y una "
        "lista de aristas haría costosa la consulta de R<sub>ik</sub>. Las aristas paralelas del DFA se fusionan con unión (∪) al construir el grafo."),
      P("3.3 Álgebra de expresiones regulares (RegexOps)", h2),
      P("Las expresiones son cadenas de texto con dos convenciones: <font face='DVM'>null</font> representa ∅ (lenguaje vacío) y "
        "<font face='DVM'>\"ε\"</font> representa la cadena vacía. Cada operación aplica las <b>identidades del álgebra</b> para no "
        "generar expresiones innecesariamente largas:"),
      table([["Operación", "Identidades aplicadas", "Ejemplo"],
             ["union(a, b)", "∅ ∪ R = R ;  R ∪ R = R ;  ε ∪ R = R? (o R* si ya es estrella)", "union(ε, a) = a?"],
             ["concat(a, b)", "∅·R = ∅ ;  ε·R = R ;  paréntesis solo si hay unión de nivel superior", "concat(a|b, c) = (a|b)c"],
             ["star(a)", "∅* = ε ;  ε* = ε ;  (R*)* = R* ;  (R?)* = R*", "star(ab) = (ab)*"]],
            [3, 9.2, 5]),
      P("3.4 Algoritmo de eliminación de estados", h2),
      P("<b>Paso 1 — Normalización.</b> Se añade un estado inicial único <i>q_start</i> con una transición ε hacia q0 y un estado final "
        "único <i>q_accept</i> con transiciones ε desde cada estado de F. Así q_start no tiene aristas entrantes y q_accept no tiene salientes."),
      P("<b>Paso 2 — Reducción iterativa.</b> Para eliminar un estado intermedio j, cada par (i, k) con i ∈ pred(j) y k ∈ succ(j) se recalcula:"),
      Preformatted("R_ik' = R_ik  ∪  ( R_ij · (R_jj)* · R_jk )", ParagraphStyle("f", parent=code, fontSize=10.5, leading=14, alignment=TA_CENTER)),
      P("El término <b>(R<sub>jj</sub>)*</b> es la aplicación del <b>Lema de Arden</b>: la ecuación X = R<sub>jj</sub>·X ∪ S tiene como solución "
        "mínima X = (R<sub>jj</sub>)*·S. Geométricamente, el bucle del estado eliminado se «absorbe» en las aristas que lo atravesaban."),
      P("<b>Paso 3 — Animación.</b> Por cada eliminación, <font face='DVM'>EliminationController</font> reproduce tres fases con un "
        "<font face='DVM'>javax.swing.Timer</font>: (a) resaltar el nodo a eliminar y sus aristas, (b) desvanecerlo reduciendo la opacidad, "
        "(c) dibujar el grafo resultante marcando en verde las aristas recalculadas."),
      P("<b>Paso 4 — Resultado.</b> Cuando solo quedan q_start y q_accept, la etiqueta de la única arista es la expresión regular buscada."),
      Preformatted("ELIMINAR_ESTADOS(DFA):\n"
                   "  G ← GNFA(DFA)                      // R_ik = unión de etiquetas; ∅ si no hay arista\n"
                   "  G.normalizar()                     // q_start -ε-> q0 ;  q ∈ F -ε-> q_accept\n"
                   "  mientras G tenga estados intermedios:\n"
                   "      j    ← elegir(G)               // el de menor |pred(j)| · |succ(j)|\n"
                   "      loop ← star(R_jj)              // Lema de Arden\n"
                   "      para cada i ∈ pred(j), k ∈ succ(j):\n"
                   "          R_ik ← union(R_ik, concat(concat(R_ij, loop), R_jk))\n"
                   "      G.eliminar(j)\n"
                   "  devolver R(q_start, q_accept)       // null = ∅", code),
      P("<b>Orden de eliminación.</b> El resultado es siempre correcto, pero su tamaño depende del orden. La estrategia «automática» elige el "
        "estado con menor producto |pred|·|succ| (heurística clásica que minimiza nuevas aristas); la alternativa recorre los estados en orden de creación. "
        "<b>Complejidad:</b> O(n³) operaciones sobre expresiones; el tamaño de la regex puede crecer exponencialmente en el peor caso, característica conocida del método."),
      P("3.5 Ejemplo trabajado: cantidad par de 0s (Σ = {0,1})", h2),
      P("DFA: q0 (inicial y de aceptación), q1; δ(q0,0)=q1, δ(q0,1)=q0, δ(q1,0)=q0, δ(q1,1)=q1."),
      table([["Paso", "Acción", "Resultado"],
             ["1", "Normalizar", "q_start –ε→ q0 ;  q0 –ε→ q_accept"],
             ["2a", "Eliminar q1 (pred = {q0}, succ = {q0}).  R(q0,q0)' = 1 ∪ (0 · (1)* · 0)", "bucle en q0 con etiqueta  1|01*0"],
             ["2b", "Eliminar q0 (pred = {q_start}, succ = {q_accept}).  R' = ∅ ∪ (ε · (1|01*0)* · ε)", "q_start –(1|01*0)*→ q_accept"],
             ["4", "Resultado", "<b>R = (1|01*0)*</b>"]], [1.4, 9.6, 6.2]),
      Spacer(1, 6),
      P("3.6 Simulación y verificación de equivalencia", h2),
      P("<font face='DVM'>DfaSimulator.run</font> recorre la cadena y devuelve la lista de pasos <i>(from, símbolo, transición, to)</i>; "
        "la animación solo reproduce esa lista, de modo que la lógica no depende de la interfaz. Antes de simular se valida que el autómata sea un DFA "
        "(estado inicial presente, sin ε y sin transiciones repetidas con el mismo símbolo). "
        "<font face='DVM'>EquivalenceChecker</font> compara el veredicto del DFA con "
        "<font face='DVM'>Pattern.compile(regex).matcher(cadena).matches()</font> para una cadena, o para <b>todas</b> las cadenas sobre Σ hasta una longitud "
        "máxima (por niveles, con tope de 200 000 cadenas), devolviendo un contraejemplo si lo hay."),
      PageBreak()]

# ------------------------------------------------------------------ 4. manual
S += [P("4. Manual de usuario", h1),
      P("4.1 Requisitos y ejecución", h2),
      P("Se necesita <b>JDK 17 o superior</b> y <b>Apache Maven 3.8+</b>. Desde la carpeta del proyecto:"),
      Preformatted("mvn clean package          # compila, ejecuta las pruebas y genera target/automata-studio-1.0.0.jar\n"
                   "java -jar target/automata-studio-1.0.0.jar\n\n"
                   "# alternativa sin generar el jar:\n"
                   "mvn compile exec:java", code),
      P("4.2 Pantalla principal", h2),
      P("La ventana tiene una <b>barra de herramientas</b> (modos y acciones), el <b>lienzo</b> al centro y, a la derecha, tres pestañas: "
        "<b>1. Simulación</b>, <b>2. Regex</b> y <b>3. Equivalencia</b>. La barra inferior muestra ayuda contextual. Al abrir la aplicación se carga un ejemplo "
        "(cantidad par de 0s); también puede elegirse otro en <i>Archivo → Cargar ejemplo</i>."),
      fig("img/01_editor.png", 16.5, "Figura 2. Ventana principal con el ejemplo «cantidad par de 0s» cargado. La flecha entrante marca el estado inicial; el doble círculo, el de aceptación."),
      P("4.3 Construir un autómata", h2)]
S += B(["<b>Estado (Q):</b> activa el modo <i>Estado (Q)</i> y haz clic en un espacio vacío. El primer estado creado es el inicial por defecto.",
        "<b>Transición (δ):</b> activa <i>Transición (δ)</i>, haz clic en el estado de origen y luego en el de destino (mismo estado = bucle). Escribe el símbolo en el cuadro que aparece. Esc cancela.",
        "<b>Propiedades:</b> selecciona un estado y usa <i>Inicial (q0)</i> o <i>Aceptación (F)</i> (o el menú contextual con clic derecho).",
        "<b>Editar:</b> en modo <i>Seleccionar</i>, arrastra los estados para moverlos; doble clic en un estado lo renombra y en una arista edita su etiqueta; <i>Supr</i> o <i>Eliminar</i> borra lo seleccionado.",
        "<b>Etiquetas:</b> un símbolo (<font face='DVM'>a</font>), varios (<font face='DVM'>a,b</font>), epsilon (<font face='DVM'>eps</font> o <font face='DVM'>ε</font>) o una expresión parcial (<font face='DVM'>ab*</font>)."])
S += [Spacer(1, 4),
      fig("img/12_drawn.png", 16.5, "Figura 3. Autómata dibujado desde cero con el ratón: estados, transiciones, un bucle (a,b), una arista ε y un estado de aceptación."),
      P("4.4 Simulación de cadenas (pestaña 1)", h2),
      P("Escribe una cadena y pulsa <b>▶ Simular</b> (animación automática, velocidad regulable) o <b>Paso ⏭</b> (un símbolo por clic). El estado actual se pinta de amarillo, "
        "la transición usada en rojo y la parte consumida de la cadena en verde. Al terminar se muestra <b>ACEPTADA</b> (verde) o <b>RECHAZADA</b> (rojo) y el estado final se colorea igual. "
        "Si el autómata no es un DFA válido, se indica el motivo (por ejemplo «falta el estado inicial» o «no determinista»)."),
      fig("img/02_sim_mid.png", 16.5, "Figura 4. Simulación en curso con la cadena 10010: dos símbolos consumidos, estado actual q1."),
      fig("img/03b_sim_rejected.png", 16.5, "Figura 5. Fin de la simulación: la cadena 10010 tiene tres ceros, por lo que es RECHAZADA."),
      P("4.5 Conversión a expresión regular (pestaña 2)", h2),
      P("Elige el orden de eliminación y pulsa <b>Iniciar</b>; el lienzo pasa a mostrar el grafo del algoritmo y se bloquea la edición. Avanza con <b>Siguiente paso</b>, "
        "reproduce todo con <b>▶ Animar</b> (<b>⏸ Pausa</b> la detiene), salta con <b>⏭ Final</b> o regresa con <b>↩ Volver al editor</b>. "
        "El cuadro «Pasos del algoritmo» registra cada fórmula R<sub>ik</sub>' aplicada."),
      fig("img/05_norm.png", 16.5, "Figura 6. Paso 1 — Normalización: se añaden q_start (con ε hacia q0) y q_accept (con ε desde cada estado de F)."),
      fig("img/06_elim_highlight.png", 16.5, "Figura 7. Paso 2 — Se resalta q1 (rojo) y las aristas que pasan por él (naranja) antes de desvanecerlo."),
      fig("img/08_elim_done.png", 16.5, "Figura 8. Tras eliminar q1: el bucle de q0 queda etiquetado 1|01*0 (verde = arista recalculada)."),
      fig("img/09_final.png", 16.5, "Figura 9. Resultado final: una sola arista q_start → q_accept con la expresión (1|01*0)*, copiable con el botón «Copiar»."),
      P("4.6 Verificación de equivalencia (pestaña 3)", h2),
      P("Escribe una cadena de prueba y pulsa <b>Evaluar en DFA y en la regex</b>: se muestra el veredicto de cada modelo y si coinciden. "
        "<b>Verificación exhaustiva</b> prueba todas las cadenas hasta la longitud elegida y reporta equivalencia o un contraejemplo. "
        "Si se edita el autómata, la regex deja de ser válida y debe regenerarse."),
      fig("img/10_equiv.png", 16.5, "Figura 10. Evaluación de la cadena 10010: ambos modelos la rechazan (✔ Coinciden)."),
      fig("img/11_equiv_all.png", 16.5, "Figura 11. Verificación exhaustiva: 2047 cadenas (hasta longitud 10) sin ningún contraejemplo."),
      P("4.7 Problemas frecuentes", h2),
      table([["Mensaje / síntoma", "Causa y solución"],
             ["«Falta marcar el estado inicial»", "Selecciona un estado y pulsa «Inicial (q0)»."],
             ["«No determinista: q0 tiene más de una transición con 'a'»", "Un DFA permite solo una transición por símbolo y estado; corrige la etiqueta."],
             ["«La etiqueta … no es un símbolo único»", "Las expresiones parciales (ab*) sirven para la conversión, pero la simulación exige símbolos de 1 carácter."],
             ["La regex muestra ∅", "El autómata no tiene estados de aceptación alcanzables: el lenguaje es vacío."],
             ["Los botones de edición no responden", "Estás en la pestaña Regex con el algoritmo activo; pulsa «Volver al editor»."]],
            [6.2, 11]),
      PageBreak()]

# ------------------------------------------------------------------ 5. pruebas
S += [P("5. Pruebas realizadas", h1),
      P("<b>Pruebas unitarias (JUnit 5, 8 casos):</b> simplificaciones de RegexOps, interpretación de etiquetas, regex conocida del ejemplo de ceros pares (<font face='DVM'>(1|01*0)*</font>), "
        "normalización, lenguaje vacío, simulación (aceptar/rechazar/ε/símbolo fuera del alfabeto) y detección de no determinismo."),
      P("<b>Equivalencia exhaustiva:</b> para los tres autómatas de ejemplo y las dos estrategias de orden, el DFA y la regex obtenida coinciden en las 8191 cadenas sobre Σ = {0,1} (o {a,b}) de longitud ≤ 12, sin contraejemplos."),
      P("<b>Pruebas de interfaz:</b> se ejecutó un guion con eventos reales de ratón sobre el lienzo (crear estados, conectar transiciones con diálogos, marcar aceptación, simular, convertir y verificar) "
        "y se revisaron las capturas de cada etapa."),
      table([["Ejemplo (Σ)", "Regex obtenida (orden automático)"],
             ["Cantidad par de 0s ({0,1})", "(1|01*0)*"],
             ["Terminan en «ab» ({a,b})", "b*aa*b((a|bb*a)a*b)*"],
             ["Binarios múltiplos de 3 ({0,1})", "(0|1(01*0)*1)*"]], [7.5, 9.7])]

doc = SimpleDocTemplate("Documentacion_Proyecto.pdf", pagesize=letter, leftMargin=2.2 * cm, rightMargin=2.2 * cm,
                        topMargin=2 * cm, bottomMargin=2 * cm, title="Autómatas Studio — Documentación", author=ESTUDIANTE)
doc.build(S, onFirstPage=lambda c, d: None, onLaterPages=footer)
print("PDF generado")
