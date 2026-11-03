# Seguridad y privacidad

Esta aplicación maneja **datos de salud**, que son sensibles.

## Si encuentras una vulnerabilidad
No abras un issue público. Usa *Security ▸ Report a vulnerability* de este repositorio (informe privado de GitHub)
o contacta al mantenedor directamente.

## Reglas para quien contribuye o publica una copia
- **Nunca** subas datos de pacientes reales: exportaciones PDF/CSV, bases de datos (`*.db`), capturas con nombres o RUT.
- **Nunca** subas claves de API ni keystores. La clave opcional de Gemini va solo en `local.properties` (ignorado por Git).
  Si una clave llegó a publicarse, **revócala de inmediato** y genera otra; borrarla del historial no basta.
- Una clave incluida en el APK puede extraerse. Para distribuir la app, usa un servidor intermediario.

## Limitaciones conocidas
- La base de datos local (Room) no está cifrada; usa el almacenamiento privado de la app.
- No hay bloqueo por PIN ni biometría.
