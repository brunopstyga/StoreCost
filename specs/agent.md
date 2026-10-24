# Agent Instructions & Guidelines (`agent.md`)
**Proyecto**: Módulo de Cálculo de Precios, Planilla de Ventas y Cierre de Jornada (`store_cost`)
**Especificación de Referencia**: `spec-001`

---

## 1. Contexto del Proyecto y Stack Tecnológico
Este repositorio alberga una aplicación móvil/multiplataforma desarrollada con **Kotlin Multiplatform (KMP)** y **Compose Multiplatform**, siguiendo los principios estrictos de **Clean Architecture**, **Unidirectional Data Flow (UDF)** y **Test-Driven Development (TDD)**.

### Stack Principal:
- **Lenguaje & Core**: Kotlin, Coroutines, `StateFlow`, `kotlinx-datetime`.
- **UI Framework**: Compose Multiplatform con **Material 3** (`org.jetbrains.compose.material3`).
- **Navegación**: Type-safe Navigation Compose (KMP) con menú desplegable (Drawer / Navigation Rail).
- **Persistencia**: Room KMP (`RoomDatabase`, `SaleEntity`, `SaleDao`).
- **Inyección de Dependencias**: Koin KMP + KSP (`@Module`, `@ComponentScan`, `@Single`, `@Factory`, `@KoinViewModel`).
- **Pruebas**: `kotlin.test`, `kotlinx-coroutines-test`, Fakes/Mocks en `commonTest`.
- **Automatización**: Android `WorkManager` para tareas programadas en segundo plano.

---

## 2. Reglas Arquitectónicas y Estándares de Código

### A. Capa de Dominio (`domain/`) — *100% Kotlin Puro*
1. **Sin Dependencias de Frameworks**: La capa de dominio debe ser totalmente agnóstica a Room, Compose, Android, iOS o Koin.
2. **Result Pattern**: Todas las operaciones asincrónicas, repositorios y casos de uso deben devolver la clase sellada `Result<T>` (`Loading`, `Success<T>`, `Error`).
3. **Casos de Uso**: Cada UseCase tiene una sola responsabilidad (`CalculateFinalPriceUseCase`, `RegisterSaleUseCase`, `GetSalesSummaryUseCase`, `GenerateMonthlyReportUseCase`).
4. **Puridad e Inmutabilidad**: Priorizar valores inmutables (`val`), funciones puras, manejo explícito de nulos (`null-safety`) y modelos de dominio desacoplados (`Sale`, `PriceCalculationResult`).

### B. Capa de Datos (`data/`)
1. **Mapeo Claro**: Usar mappers para convertir entre `SaleEntity` (Room) y `Sale` (Dominio).
2. **Abstracción de Repositorios**: La implementación `SaleRepositoryImpl` reside en `data/` pero implementa la interfaz `ISaleRepository` definida en `domain/`.
3. **Flujos Fríos**: Exponer `Flow<T>` desde los DAOs y Repositorios para lecturas reactivas.

### C. Capa de Presentación (`presentation/`)
1. **Unidirectional Data Flow (UDF)**:
   - Los ViewModels transforman los `Flow` fríos en **`StateFlow`** cálidos mediante `stateIn`.
   - Los estados de UI (`SalesUiState`, `CalculationUiState`) son data classes inmutables y representan la única fuente de verdad.
2. **Jetpack Compose Multiplatform**:
   - Componentes visuales *stateless*.
   - Renderizado eficiente para listas mediante **`LazyColumn`** / **`LazyRow`**.
   - **Consumo Asincrónico**: La recolección de estados en las pantallas de Compose debe hacerse mediante `collectAsStateWithLifecycle()` o `collectAsState()`.
3. **Punto de Entrada en Android**: `MainActivity` (módulo `app`) aloja la navegación y `setContent`. `MainApplication` inicializa Koin con `androidContext`.

---

## 3. Protocolo de Trabajo en TDD (Test-Driven Development)

Para las capas de **Dominio** y **ViewModels**, debes aplicar estrictamente el ciclo TDD en `commonTest`:

1. **Fase Roja**: Crear/actualizar la prueba unitaria en `commonTest/kotlin/com/tickets/store_cost/...` (`CalculatePriceUseCaseTest`, `RegisterSaleUseCaseTest`, `DailyClosingUseCaseTest`, `SalesViewModelTest`). Verificar que la prueba falle por falta de código.
2. **Fase Verde**: Escribir la implementación mínima necesaria en `commonMain` para que la prueba pase.
3. **Fase Refactor**: Limpiar el código manteniendo la suite de pruebas en verde (`gradlew test`).

---

## 4. Recordatorio Obligatorio de Gradle y Manejo de Dependencias

**CADA VEZ** que se requiera incluir o modificar una librería, componente visual, extensión o motor de persistencia:
1. Declarar la versión y el módulo en **`gradle/libs.versions.toml`** (bloques `[versions]` y `[libraries]`).
2. Sincronizar e implementar la dependencia en el bloque `build.gradle.kts` correspondiente (`shared/build.gradle.kts` para `commonMain`/`commonTest` o `app/build.gradle.kts`).
3. Verificar la compatibilidad de KSP y anotaciones con `gradlew clean build`.

---

## 5. Hoja de Ruta y Tareas Especificadas (`spec-001`)

Sigue estrictamente las tareas planificadas en el documento de especificación:

- **Tarea 01**: Inicialización de Estructura Clean Architecture y Testing (`commonMain` y `commonTest`).
- **Tarea 02**: Modelos de Dominio, `Result.kt`, Lógica de Precios, Registro de Ventas, Cierres y Persistencia con Room KMP + KSP + Koin.
  - *Tabla de Recargos*: `FACTURA_A` (+21%), `FACTURA_B` (+10.5%), `LOTE_100` (+100%), `LOTE_110` (+110%), `EFECTIVO_NEGRO` (+35%).
- **Tarea 03**: ViewModels, `SalesUiState`, `CalculationUiState`, `StateFlow` y Grafo de Dependencias en Koin.
- **Tarea 04**: Menú de Navegación (Drawer/TopBar), `MainActivity`, Pantallas Compose (`GestionVentasScreen`, `CierresJornadaScreen`) y consumo asincrónico de `UiState`.
- **Tarea 05**: Generación de Reportes Periódicos (`GenerateMonthlyReportUseCase`), `WorkManager` para los primeros días del mes y envío asistido vía `Intent` de correo electrónico.

---

## 6. Formato de Comunicación y Commits

- Mantén las respuestas técnicas, concisas y enfocadas en el código KMP.
- Cuando sugieras cambios en la terminal, provee mensajes basados en *Conventional Commits* (ej. `feat(domain): implement CalculateFinalPriceUseCase with TDD`).