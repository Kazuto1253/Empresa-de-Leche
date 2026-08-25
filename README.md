# Ecolactea Digital — Modelo de dominio

Proyecto Kotlin Multiplatform mínimo para el modelado del dominio de Ecolactea Digital.

## Requisitos

- JDK 17
- IntelliJ IDEA
- Gradle Wrapper

## Ejecutar pruebas

En Windows:

```powershell
.\gradlew.bat allTests
```

También puede ejecutarse:

```powershell
.\gradlew.bat jvmTest
```

Las pruebas se encuentran en:

```text
src/commonTest/kotlin/org/ecolactea/domain/ModeloDominioTest.kt
```

El modelo está en:

```text
src/commonMain/kotlin/org/ecolactea/domain/
```

La documentación está en:

```text
docs/modelo-dominio.md
```
