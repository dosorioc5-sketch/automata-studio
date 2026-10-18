# Autómatas Studio

Editor gráfico de DFA, simulador animado y conversor **DFA → Expresión Regular** por eliminación de estados
(Java 17 · Swing · JGraphX · Maven).

## Ejecutar
```bash
mvn clean package                         # compila, corre las pruebas y genera el jar
java -jar target/automata-studio-1.0.0.jar
# o bien:
mvn compile exec:java
```

## Estructura (MVC)
| Paquete | Contenido |
|---|---|
| `edu.automata.model` | `Automaton`, `State`, `Transition`, `Edge`, `Examples` |
| `edu.automata.algorithm` | `RegexOps`, `GnfaGraph`, `StateEliminator`, `DfaSimulator`, `EquivalenceChecker` |
| `edu.automata.controller` | `EditorController`, `SimulationController`, `EliminationController`, `EquivalenceController` |
| `edu.automata.view` | `MainFrame`, `GraphView` (JGraphX), paneles |

Documentación (UML, estructuras de datos, manual con capturas): `docs/Documentacion_Proyecto.pdf`.
Para regenerar: edita tus datos en `docs/build_docs.py` y ejecuta `python3 build_docs.py`.
