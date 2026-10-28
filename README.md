# Store Cost

Aplicación móvil para calcular precios de venta, registrar ventas diarias, cerrar la jornada y generar reportes en CSV, con datos almacenados localmente en el dispositivo.

Desarrollada con **Kotlin Multiplatform** y **Compose Multiplatform** siguiendo Clean Architecture, Unidirectional Data Flow (UDF) y TDD en las capas de dominio y presentación.

![Kotlin](https://img.shields.io/badge/Kotlin-2.0.0-7F52FF?logo=kotlin&logoColor=white)
![Compose Multiplatform](https://img.shields.io/badge/Compose%20Multiplatform-1.6.11-4285F4)
![Android](https://img.shields.io/badge/Android-minSdk%2024-3DDC84?logo=android&logoColor=white)
![iOS](https://img.shields.io/badge/iOS-KMP-000000?logo=apple&logoColor=white)

---

## Características

- **Cálculo de precios** según la modalidad de operación, con historial de consultas.
- **Registro de ventas** con importe, cantidad, precio unitario y subtotal.
- **Cierre diario** con total vendido y cantidad de operaciones.
- **Historial de transacciones** completo.
- **Resumen mensual** con total, operaciones y ticket promedio.
- **Exportación mensual a CSV** y envío por correo electrónico.
- **Reportes automáticos** en segundo plano mediante WorkManager.
- **Acceso con contraseña local** (hash SHA-256) y sesión persistente.
- **Interfaz Material 3** compartida entre plataformas.

### Modalidades y recargos

| Modalidad | Recargo | Fórmula |
|---|---:|---|
| Factura A | +21 % | `precioBase × 1,21` |
| Factura B | +10,5 % | `precioBase × 1,105` |
| Lote 100 | +100 % | `precioBase × 2,00` |
| Lote 110 | +110 % | `precioBase × 2,10` |
| Efectivo Negro | +35 % | `precioBase × 1,35` |

> La contraseña inicial es **`storecost`**. Se guarda hasheada y la sesión se mantiene entre aperturas de la app.

---

## Stack tecnológico

| Área | Tecnología |
|---|---|
| Lenguaje | Kotlin 2.0.0 |
| Multiplataforma | Kotlin Multiplatform + Compose Multiplatform 1.6.11 |
| UI | Compose Material 3 |
| Arquitectura | Clean Architecture + UDF (`StateFlow`) |
| Persistencia | Room KMP (`androidx.room` 2.7.0-alpha11) |
| Inyección de dependencias | Koin 4.0.0 + Koin Annotations (KSP) |
| Navegación | Navigation Compose (KMP) |
| Fechas | `kotlinx-datetime` |
| Tareas en segundo plano | WorkManager (Android) |
| Testing | `kotlin.test`, `kotlinx-coroutines-test`, fakes |

---

## Estructura del proyecto

```text
store_cost/
├── androidApp/                      # Aplicación Android
│   └── src/main/java/.../android/
│       ├── MainActivity.kt
│       ├── StoreCostApplication.kt  # Inicialización de Koin y workers
│       ├── notification/            # Notificaciones de reportes
│       ├── report/                  # Envío de CSV por correo
│       └── worker/                  # Reportes y limpieza automática
├── shared/                          # Código compartido KMP
│   └── src/
│       ├── commonMain/kotlin/com/tickets/store_cost/
│       │   ├── domain/              # Modelos, casos de uso y contratos
│       │   ├── data/                # Room, repositorios, mappers y storage
│       │   ├── presentation/        # Pantallas Compose, ViewModels y estados
│       │   ├── di/                  # Módulos de Koin
│       │   └── core/                # Utilidades compartidas
│       ├── androidMain/             # Implementaciones Android
│       ├── iosMain/                 # Implementaciones iOS
│       └── commonTest/              # Pruebas unitarias
├── iosApp/                          # Proyecto Xcode
├── specs/                           # Especificaciones y reglas de arquitectura
├── flujo-de-la-app.md               # Documentación funcional de la app
├── compose-stability.conf           # Configuración de estabilidad de Compose
└── gradle/libs.versions.toml        # Catálogo de dependencias
```

Flujo de dependencias:

```text
Pantalla Compose → ViewModel (StateFlow) → Caso de uso → Repositorio (interfaz)
                                                       → Implementación → Room / KeyValueStorage
```

---

## Requisitos

- **JDK 17** o superior.
- **Android Studio** (Ladybug o posterior) con Android SDK 35.
- **Xcode 15+** y macOS para compilar la app de iOS.
- Gradle se descarga automáticamente mediante el wrapper (8.10.2).

---

## Puesta en marcha

### Android

```bash
# Compilar el APK de debug
./gradlew :androidApp:assembleDebug

# Instalar en un dispositivo o emulador conectado
./gradlew :androidApp:installDebug
```

En Windows:

```bash
gradlew.bat :androidApp:assembleDebug
```

También se puede abrir el proyecto en Android Studio y ejecutar la configuración `androidApp`.

### iOS

1. Abrir `iosApp/iosApp.xcodeproj` en Xcode.
2. Ejecutar el esquema `iosApp` en un simulador o dispositivo.

> La interfaz de iOS todavía no consume las pantallas compartidas; el proyecto Xcode se mantiene como punto de entrada nativo.

### Pruebas

```bash
# Pruebas unitarias de la capa compartida (Android)
./gradlew :shared:testDebugUnitTest

# Todas las pruebas de la capa compartida
./gradlew :shared:allTests
```

Las pruebas cubren casos de uso, repositorios, `Result`, hashing, formateo y ViewModels.

---

## Documentación

| Documento | Contenido |
|---|---|
| [`flujo-de-la-app.md`](flujo-de-la-app.md) | Recorrido funcional completo: login, pantallas, flujos, datos y reglas de negocio. |
| [`specs/architecture.md`](specs/architecture.md) | Reglas de arquitectura KMP, SOLID y DI. |
| [`specs/spec-001-price-module.md`](specs/spec-001-price-module.md) | Especificación del módulo de precios, ventas y cierres. |
| [`specs/spec-002-modulo.md`](specs/spec-002-modulo.md) | Especificación del segundo módulo. |
| [`specs/tdd-scenarios-test.md`](specs/tdd-scenarios-test.md) | Escenarios de prueba bajo TDD. |
| [`specs/agent.md`](specs/agent.md) | Guía de trabajo y estándares para agentes/desarrolladores. |

---

## Reglas de negocio destacadas

- Todos los importes se redondean a dos decimales.
- No se permiten precios ni cantidades menores o iguales a cero.
- Una consulta de precios **no** genera una venta.
- Solo se puede cerrar el día si hay al menos una venta registrada.
- Un cierre por fecha: volver a cerrar el mismo día reemplaza el anterior.
- Las consultas de precios se conservan 24 horas; las ventas y cierres, 15 días.
- Antes de purgar ventas y cierres se genera un respaldo CSV y se notifica al usuario.
- El reporte automático cubre el **mes anterior**; la exportación manual, el **mes en curso**.

---

## Estado del proyecto

- La app Android está completamente funcional.
- La capa `shared` concentra dominio, datos y presentación.
- La app iOS aún no está conectada a las pantallas compartidas.
- No existe todavía una pantalla para cambiar la contraseña desde la interfaz.
- La pestaña **Diario** trabaja siempre con la fecha actual.
