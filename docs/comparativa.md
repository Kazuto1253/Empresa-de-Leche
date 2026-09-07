# Comparativa de frameworks multiplataforma

## Objetivo

Comparar Kotlin Multiplatform, Flutter y React Native para justificar la selección tecnológica de Ecoláctea Digital. La comparación considera siete dimensiones y utiliza documentación oficial consultada en septiembre de 2026.

## Comparación

| Dimensión | Kotlin Multiplatform | Flutter | React Native |
|---|---|---|---|
| Lenguaje y aprendizaje | Usa Kotlin. Es conveniente para un equipo que ya trabaja con Android, Ktor y corrutinas, aunque Kotlin/Native y los source sets requieren aprendizaje adicional (JetBrains, s. f.-a). | Usa Dart, por lo que un equipo sin experiencia debe aprender lenguaje y framework. Su modelo reactivo y sus widgets cuentan con documentación integrada (Flutter, 2026a). | Usa JavaScript o TypeScript y React. La entrada es favorable para equipos web, pero exige comprender también particularidades nativas (Meta Platforms, 2026). |
| Estrategia de UI | Permite compartir lógica y decidir si la UI será Compose Multiplatform o nativa por plataforma (JetBrains, s. f.-b). | Comparte la UI mediante su propio framework y motor de renderizado (Flutter, 2026a). | Declara la UI con React y la conecta con componentes y capacidades nativas (Meta Platforms, 2024). |
| Código compartido | El equipo decide cuánto compartir: dominio, datos, red y, si conviene, UI. Admite implementaciones específicas por plataforma (JetBrains, s. f.-a). | Normalmente comparte lógica y UI desde una sola base Dart, usando plugins o código específico cuando es necesario (Flutter, 2026b). | Comparte lógica y UI y permite código específico de Android/iOS (Meta Platforms, 2026). |
| Resultado técnico | Kotlin/JVM se usa en Android y Desktop; Kotlin/Native genera binarios para targets nativos (JetBrains, s. f.-a). | Durante desarrollo usa una VM con hot reload; las versiones de entrega se compilan para el target correspondiente (Flutter, 2026a). | Su arquitectura coordina el runtime de JavaScript, el renderizado y los módulos nativos (Meta Platforms, 2024). |
| Ecosistema y madurez | Respaldado por JetBrains y con soporte para compartir código entre las plataformas declaradas en su documentación oficial (JetBrains, s. f.-a). | Respaldado por Google y con soporte documentado para móvil, web y escritorio (Flutter, 2026b). | Respaldado por Meta y acompañado por el ecosistema de React y herramientas comunitarias (Meta Platforms, 2026). |
| Entorno de desarrollo | Android y Desktop pueden desarrollarse en Windows. La compilación final de iOS requiere macOS y Xcode (JetBrains, s. f.-a). | Android y Windows pueden compilarse en Windows; iOS y macOS requieren macOS (Flutter, 2026b). | Android puede desarrollarse en Windows; la compilación nativa de iOS requiere macOS y Xcode (Meta Platforms, 2026). |
| Adecuación al proyecto | Encaja con Kotlin en cliente y servidor, Clean Architecture, lógica compartida, Android/Desktop e iOS preparado. Facilita mantener reglas de negocio únicas. | Sería adecuado si la prioridad fuera una UI uniforme y un equipo dispuesto a adoptar Dart. Implicaría otro lenguaje para el cliente. | Sería adecuado para un equipo con experiencia fuerte en React/TypeScript y prioridad móvil. Introduciría un segundo ecosistema frente al backend Kotlin. |

## Conclusión del equipo

Flutter es una alternativa sólida cuando se busca compartir una interfaz completa y el equipo acepta adoptar Dart. React Native resulta conveniente para equipos que ya dominan React y TypeScript. Kotlin Multiplatform es la opción más coherente para Ecoláctea Digital porque permite compartir dominio, casos de uso, datos y UI sin abandonar Kotlin, conserva acceso específico a cada plataforma y se integra naturalmente con el backend Ktor.

La elección no significa que KMP sea superior en todos los contextos; responde a las restricciones académicas, experiencia tecnológica, necesidad offline y objetivo de evitar reglas duplicadas entre plataformas.

## Referencias

- Flutter. (2026a). *Flutter architectural overview*. https://docs.flutter.dev/resources/architectural-overview
- Flutter. (2026b). *Build for and integrate with multiple platforms*. https://docs.flutter.dev/platform-integration
- JetBrains. (s. f.-a). *Kotlin Multiplatform*. Recuperado el 7 de septiembre de 2026 de https://kotlinlang.org/docs/multiplatform.html
- JetBrains. (s. f.-b). *Compose Multiplatform*. Recuperado el 7 de septiembre de 2026 de https://kotlinlang.org/docs/multiplatform/compose-multiplatform.html
- Meta Platforms. (2026). *Introduction to React Native*. https://reactnative.dev/docs/getting-started
- Meta Platforms. (2024). *React Native architecture overview*. https://reactnative.dev/architecture/overview

## Declaración de apoyo tecnológico

El equipo utilizó un asistente de inteligencia artificial para organizar la comparación y mejorar la redacción. Los integrantes revisaron el contenido y contrastaron las afirmaciones con la documentación oficial enlazada.
