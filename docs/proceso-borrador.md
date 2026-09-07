# Modelo de proceso del proyecto

## Elección

El Equipo Nexo Lácteo adopta un proceso **incremental y ágil**, organizado por entregas pequeñas integradas mediante ramas personales y Pull Requests.

## Justificación

Ecoláctea Digital depende de información de una organización real y todavía conserva 21 incertidumbres abiertas. Un proceso en cascada exigiría cerrar demasiado pronto precios, rangos de calidad, tolerancias, fórmulas y procedimientos que la planta aún no ha confirmado. Un enfoque puramente improvisado tampoco sería adecuado porque existen 36 RF, dependencias entre cuatro responsables y obligaciones de trazabilidad.

El proceso incremental permite mantener una visión V2 completa y construir por bloques demostrables. Cada incremento selecciona requisitos relacionados, confirma contratos, implementa una parte vertical, ejecuta pruebas y registra evidencia. Las observaciones de la planta, del docente y del equipo alimentan los incrementos siguientes sin perder el historial de decisiones.

## Forma de trabajo

1. Seleccionar RF y criterios de aceptación del incremento.
2. Revisar dependencias e incertidumbres antes de programar.
3. Acordar contratos compartidos entre responsables.
4. Desarrollar en la rama personal correspondiente.
5. Ejecutar pruebas y actualizar trazabilidad.
6. Abrir Pull Request y recibir revisión de otro integrante.
7. Integrar en `main` y demostrar el incremento.
8. Registrar cambios relevantes y actualizar la línea base cuando corresponda.

## Riesgos y limitaciones

### Cambios frecuentes de alcance

La validación pendiente puede modificar requisitos ya planificados. Se mitiga manteniendo incertidumbres visibles, evitando números inventados y aplicando control de cambios.

### Conflictos de integración

Cuatro personas trabajan sobre módulos compartidos. Se mitiga acordando puertos e interfaces, integrando con frecuencia y evitando cambios generales sin coordinación.

### Deuda técnica por presión de entrega

La velocidad puede llevar a omitir pruebas o duplicar lógica. Toda postergación debe registrarse en `docs/deuda-tecnica` con responsable y fecha de revisión.

### Alcance amplio

Los 36 RF representan la visión completa aprobada por el equipo, no la obligación de declarar todo terminado simultáneamente. Cada entrega comunicará con precisión qué RF están diseñados, implementados, probados o pendientes.
