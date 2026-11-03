## ¿Qué cambia?

<!-- Describe el cambio en pocas líneas -->

## Lista de verificación

- [ ] Compila y las pruebas pasan (`gradle testDebugUnitTest`)
- [ ] Si toqué `domain/Scoring.kt`, el informe o los umbrales: actualicé las pruebas y lo expliqué en este PR
- [ ] **No incluye datos de pacientes reales** (ni PDF/CSV exportados, ni capturas con nombres o RUT)
- [ ] **No incluye claves** (`GEMINI_API_KEY`, keystores, `local.properties`)
- [ ] Si cambié el esquema de Room: subí `version` y agregué una `Migration` (nunca `fallbackToDestructiveMigration`)
