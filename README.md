# NeuroCognitiva Pro — Android (Kotlin + Jetpack Compose)

![Android CI](https://github.com/TU_USUARIO/TU_REPOSITORIO/actions/workflows/android.yml/badge.svg)

Versión nativa de la aplicación web *NeuroCognitiva Pro: estimulación y evaluación cognitiva*:
las **10 actividades × 8 niveles**, el informe clínico, y funciones nuevas pensadas para uso real
(historial persistente, varios pacientes, evaluación guiada adaptativa, PDF, etc.).

> **Importante:** este proyecto se generó sin acceso a Android SDK ni a un compilador de Kotlin, por lo que
> **no pude compilarlo ni ejecutarlo**. Se verificó con revisiones estáticas (balance de llaves, imports) y se
> incluyen pruebas unitarias, pero es normal que al primer *Sync/Build* aparezca algún error menor de
> compilación. Pégame el mensaje y lo corrijo.

## Requisitos
- Android Studio **Koala (2024.1.1) o superior** (incluye JDK 17).
- Android SDK 34 (Studio lo descarga al sincronizar). `minSdk 26` (Android 8.0+).

## Cómo abrirlo
1. Descomprime y abre la carpeta `NeuroCognitivaPro` con *File ▸ Open*.
2. Espera el *Gradle Sync* (descarga Gradle 8.9 y dependencias). El `gradle-wrapper.jar` binario no se incluye;
   Android Studio usa `gradle/wrapper/gradle-wrapper.properties`. Si quieres `./gradlew`, ejecuta una vez
   `gradle wrapper --gradle-version 8.9`. La integración continua de GitHub (`.github/workflows/android.yml`) no lo necesita.
3. Ejecuta ▶ en un emulador o dispositivo. Pruebas: *Run ▸ app/src/test* o `./gradlew test`.

### IA opcional (Gemini)
Copia `local.properties.example` a `local.properties` y agrega `GEMINI_API_KEY=...` (y opcionalmente `GEMINI_MODEL`).
Luego activa el consentimiento en **Ajustes**. Sin clave, el informe usa solo la síntesis local.
> Una clave dentro del APK puede extraerse. Para distribuir la app usa un backend propio como intermediario.

## Estructura
```
app/src/main/java/com/neurocognitiva/pro/
├─ data/      Room: pacientes y sesiones, repositorio, preferencias
├─ domain/    Lógica pura (sin Android UI): catálogo, scoring, informe, batería, generadores
├─ game/      GameTracker + games/ (los 10 juegos en Compose)
├─ report/    PdfExporter, CsvExporter, ShareUtils, AiClient
└─ ui/        Navegación, pantallas, gráficos (radar/línea con Canvas), tema
```
`domain/Catalog.kt` y `ReasoningBank.kt` se generaron automáticamente desde tu HTML (sin transcripción manual).

## Mapa HTML → Android
| Web | Android |
|---|---|
| Ficha del evaluado (1 paciente, en memoria) | **Pacientes** (varios, guardados en Room) + ficha con validación |
| Actividades y niveles N1–N8 | **Actividades** con nivel recomendado por paciente |
| 10 juegos | `game/games/*Game.kt` |
| Informe (I–V) + imprimir | **Informe** en pantalla + **PDF** compartible + copiar texto |
| Gemini en el cliente | `AiClient` con consentimiento y datos anonimizados |

## Mejoras agregadas
**Flujo clínico**
- Varios pacientes con historial persistente (el HTML perdía todo al recargar).
- **Evaluación guiada**: recorre las 10 áreas con dificultad adaptativa; parte en el nivel sugerido del ámbito y
  respeta el máximo del protocolo (Tamizaje N1-3 / Estándar N1-6 / Profunda N1-8), algo que antes eran solo etiquetas.
- Pantalla de instrucciones y cuenta regresiva antes de cada tarea; la pantalla no se apaga durante el juego.
- Recomendación adaptativa de nivel al terminar cada sesión (sube con ≥85 %, baja con <55 %).
- Pestaña **Evolución** con gráfico de rendimiento por sesión y tendencia.
- Validación de RUT chileno (módulo 11, aviso no bloqueante), edad automática desde la fecha de nacimiento y aviso de ámbito vs. edad.

**Validez psicométrica**
- Cada sesión se marca como válida / azar / respuestas anticipatorias / incompleta; las dudosas no entran al índice.
- Registro separado de **omisiones y comisiones** y **d′** (teoría de detección de señal) en N-Back y CPT.
- Tiempo de reacción solo de respuestas correctas; ítems sin responder cuentan como omisión.
- Índice por dominio ponderado por nivel y normalizado contra el **techo esperable del ámbito** (reemplaza la bonificación fija de +6);
  referencias de velocidad propias de cada tarea (antes una sola escala para todas).
- Aviso de "techo no explorado" cuando el paciente domina el máximo nivel jugado.

**Mejoras de diseño de las tareas**
- Razonamiento: se baraja el orden de preguntas y alternativas (en el HTML la correcta quedaba siempre en la misma posición).
- CPT: proporción Go/No-Go exacta 65/35, estímulo breve y **un solo botón GO** (la inhibición real es *no* pulsar).
- Rotación mental: todas las opciones tienen igual número de celdas (antes se delataba por conteo) y se añaden distractores por ángulo erróneo y espejo.
- Torre de Hanoi: la precisión mide **eficiencia** (óptimo/movimientos); antes 40 movimientos en una torre de 7 daban 100 %.
- Cálculo mental: se corrigió que algunas restas pudieran dar resultado negativo; el nivel 1 incluye restas como indica su descripción.
- Símbolo-Dígito: símbolos dibujados con Canvas (idénticos en cualquier dispositivo).

**Informe y privacidad**
- PDF A4 nativo (sin librerías), CSV de sesiones **anonimizado**, copiar texto.
- Datos de demostración marcados como **simulados**, con aviso en el informe y botón para borrarlos.
- La IA solo recibe edad, escolaridad, lateralidad, ocupación, ámbito y métricas; **nunca nombre, documento ni notas**, y requiere consentimiento.
- `allowBackup=false`, borrado de paciente/historial, limpieza de archivos exportados. Sin servidores: todo queda en el dispositivo.

## Cambios metodológicos (transparencia)
Estos cambios alteran los puntajes respecto del HTML; están documentados en `domain/Scoring.kt`:
1. Índice = 0.45·precisión ponderada + 0.40·nivel dominado/techo del ámbito + 0.15·velocidad (antes 0.65·precisión + bonos).
2. "Nivel dominado" = mayor nivel con precisión ≥ 70 % (antes bastaba con haberlo jugado).
3. Precisión de tareas con tiempo límite incluye los ítems no respondidos como omisiones.

## Límites que debes conocer
- Los umbrales (88/75/60/45) y las referencias de velocidad son **heurísticos**, no baremos poblacionales validados.
  El informe lo declara: **no constituye diagnóstico**. Si se usará con fines clínicos, planifica una baremación por edad y escolaridad.
- Si la app se comercializa como herramienta de evaluación, revisa la regulación local de software de salud y la política de privacidad/Data Safety de la tienda.
- Los datos de Room no están cifrados (están en el almacenamiento privado de la app). Para mayor protección considera SQLCipher y un bloqueo por PIN/biometría.

## Ideas siguientes
Bloqueo biométrico, base cifrada, lectura de instrucciones por voz (TextToSpeech), layouts de tablet, baremos normativos,
respaldo cifrado exportable, proxy propio para la IA, y exportar el informe a Word.

## Licencia y contribuciones
Código bajo licencia **MIT** (ver `LICENSE`; reemplaza `[TU NOMBRE U ORGANIZACIÓN]` por el titular de los derechos).
Antes de contribuir o publicar una copia lee `CONTRIBUTING.md` y `SECURITY.md`: **nunca subas datos de pacientes ni claves**.
