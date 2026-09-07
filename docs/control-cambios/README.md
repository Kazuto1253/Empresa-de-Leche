# Procedimiento de control de cambios

## Cuándo aplica

Toda modificación de un requerimiento, regla, ADR, interfaz compartida, esquema de datos, prueba o línea base debe quedar registrada cuando cambie lo aprobado por el equipo.

Corregir un defecto que incumple una regla vigente no crea un requisito nuevo, pero igualmente debe ser trazable mediante issue o Pull Request.

## Flujo

1. Registrar la solicitud con ID `SC-###`.
2. Identificar solicitante, fecha, motivo y situación deseada.
3. Clasificar: defecto, cambio de regla, requisito nuevo o mejora.
4. Analizar impacto en alcance, código, datos, arquitectura, pruebas, documentación, cronograma y riesgo.
5. Registrar qué ocurre si el cambio no se realiza.
6. Someterlo al equipo/CCB: aprobar, aprobar con condición, aplazar, rechazar o pedir información.
7. Implementar únicamente después de la decisión, en una rama personal.
8. Verificar mediante pruebas y revisión de otro integrante.
9. Fusionar mediante Pull Request y actualizar los elementos afectados.
10. Cerrar la solicitud indicando versión, responsable y evidencia.

## Comité del proyecto

Los cuatro integrantes forman el CCB. Para cada revisión deben cubrir presidencia/coordinación, voz técnica, voz de calidad y voz del usuario. Quien proponga o implemente el cambio no debe ser su único revisor.

## Formato mínimo de una solicitud

```text
ID:
Título:
Solicitante y fecha:
Tipo:
Descripción:
Elementos afectados:
Impacto en pruebas:
Esfuerzo estimado:
Riesgo de implementar:
Riesgo de no implementar:
Decisión y fundamento:
Responsable y versión:
```

Las solicitudes reales se guardarán en este directorio sin copiar los casos simulados de SIREL.
