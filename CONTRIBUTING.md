# Cómo contribuir

1. Haz un *fork* y crea una rama (`git checkout -b mi-mejora`).
2. Compila y prueba: `gradle testDebugUnitTest assembleDebug` (o desde Android Studio).
3. Abre un *Pull Request* y completa la lista de verificación.

## Criterios del proyecto
- **La lógica clínica vive en `domain/`**, sin dependencias de interfaz, y debe tener pruebas en `app/src/test`.
- Los cambios en puntajes, umbrales o textos del informe deben justificarse y quedar documentados en el PR.
- `Catalog.kt` y `ReasoningBank.kt` se generaron desde un catálogo original; si los editas a mano, mantén 10 actividades × 8 niveles.
- Las tareas deben registrar **omisiones y comisiones por separado** y medir el tiempo de reacción solo en respuestas correctas.
- Nunca subas datos de pacientes ni claves (ver `SECURITY.md`).
