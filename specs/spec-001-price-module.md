# Spec 001: Módulo de Cálculo de Precios, Planilla de Ventas y Cierre de Jornada

## 1. Stack Tecnológico, Librerías, UI y Reglas de Arquitectura
- **Framework & Multiplatform**: Kotlin Multiplatform (KMP) + Compose Multiplatform.
- **Arquitectura & Escalabilidad**:
  - Clean Architecture modularizada (Presentation, Domain, Data) con principios SOLID.
  - Diseño altamente escalable: capas desacopladas mediante interfaces y abstracciones en la capa de Dominio, permitiendo agregar nuevas funcionalidades (ej. gestión de inventario, sincronización en la nube o múltiples sucursales) sin modificar la lógica existente.
- **Persistencia**: Room KMP.
- **Inyección de Dependencias**: Koin.
- **Testing**: Test-Driven Development (TDD) sobre `commonTest` utilizando `kotlin.test` y Mocks/Fakes para la capa de datos.
- **UI & Componentes Visuales (Compose Multiplatform)**:
  - Diseño basado en Material 3 (`org.jetbrains.compose.material3`).
  - Uso obligatorio de layouts eficientes para grandes volúmenes de datos (`LazyColumn` / `LazyRow` para listados de ventas y cierres).
  - Patrón de arquitectura unidireccional (UDF): pantallas *stateless* que consumen estados reactivos del `ViewModel` mediante `StateFlow`.
- **Navegación Multiplataforma**:
  - Navegación tipada (*Type-safe navigation*) mediante **Jetpack Navigation Compose (KMP)** para compartir las rutas entre Android, iOS y Desktop.
- **Configuración Explícita de Gradle**:
  - **`gradle/libs.versions.toml`**:
    - Declarar en el bloque `[versions]`: `kotlin = "..."`, `kotlinx-datetime = "..."`, `coroutines = "..."`.
    - Declarar en el bloque `[libraries]`: 
      - `kotlin-test = { module = "org.jetbrains.kotlin:kotlin-test", version.ref = "kotlin" }`
      - `kotlinx-datetime = { module = "org.jetbrains.kotlinx:kotlinx-datetime", version.ref = "kotlinx-datetime" }`
      - `kotlinx-coroutines-test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "coroutines" }`
  - **`shared/build.gradle.kts`**:
    - Agregar en el bloque `commonTest.dependencies`:
      - `implementation(libs.kotlin.test)`
      - `implementation(libs.kotlinx.datetime)`
      - `implementation(libs.kotlinx.coroutines.test)`

---

## 2. Definición de Tareas de Implementación

### **Tarea 01: Inicialización de Estructura de Clean Architecture y Testing**
- **Objetivo**: Configurar las librerías en Gradle, crear las carpetas de las capas de producción en `commonMain` y el espejo completo de pruebas en `commonTest`.
- **Pasos a ejecutar**:
  1. Configurar las dependencias de testing y fecha en `gradle/libs.versions.toml` y `shared/build.gradle.kts` según las especificaciones de la sección 1.
  2. Crear en `shared/src/commonMain/kotlin/com/tickets/store_cost/`:
     - `domain/model` (incluyendo aquí la clase base **`Result.kt`**, modelos de dominio y enums)
     - `domain/repository`, `domain/usecase`
     - `data/local`, `data/repository`, `data/mapper`
     - `presentation/screen`, `presentation/viewmodel`, `presentation/state`
     - `di`
  3. Crear en `shared/src/commonTest/kotlin/com/tickets/store_cost/` el espejo completo para pruebas:
     - `domain/usecase` (para pruebas unitarias de cálculo de precios, registro de ventas y cierres)
     - `presentation/viewmodel` (para pruebas de estados de UI)
     - `data/repository` (para colocar Fakes/Mocks de repositorios que simulen Room)
- **Criterio de Aceptación**: Dependencias sincronizadas y directorios creados correctamente sin errores de compilación en Gradle.
- **REPORTE OBLIGATORIO DE FINALIZACIÓN**: Al terminar esta tarea, el agente DEBE mostrar obligatoriamente el siguiente checklist completado con `[x]` y detallar los archivos creados/modificados:
  - [ ] Dependencias de testing y datetime sincronizadas en `gradle/libs.versions.toml` y `build.gradle.kts`.
  - [ ] Estructura completa de carpetas de Clean Architecture creada en `commonMain`.
  - [ ] Espejo completo de pruebas unitarias creado en `commonTest`.
  - *Archivos Creados/Modificados*: (Detallar la lista de carpetas y archivos inicializados).

---

### **Tarea 02: Modelos de Dominio, Base de Datos Room KMP, Koin y Lógica de Negocio (Flujo TDD)**
- **Objetivo**: Diseñar la clase sellada `Result.kt`, entidades, DAOs, base de datos Room KMP, módulo Koin y casos de uso del dominio (cálculo de precios, registro de ventas y cierres) aplicando un flujo estricto de **Test-Driven Development (TDD)**.

- **Pasos a ejecutar (Flujo TDD y Configuración)**:
  1. **Pre-flight Check y Koin**:
     - Centralizar versiones de Koin, KSP, Room KMP y `kotlinx-datetime` en `libs.versions.toml` y registrar plugins KSP/Room en `build.gradle.kts`.
     - Crear módulo Koin (`di/`) con anotaciones `@Module`, `@ComponentScan`, `@Single` y `@Factory`.
  2. **Capa Data & Persistencia (Room KMP)**:
     - Implementar `sealed class Result<T>` (`Loading`, `Success<T>`, `Error`).
     - Definir `SaleEntity`, `SaleDao` y `AppDatabase`.
  3. **Fase Roja TDD (Tests Unitarios)**:
     - Crear archivos de prueba en `commonTest/.../domain/usecase/`:
       - `CalculatePriceUseCaseTest.kt`
       - `RegisterSaleUseCaseTest.kt`
       - `DailyClosingUseCaseTest.kt`
     - Definir casos de prueba según los porcentajes de recargo obligatorios. Confirmar fallo inicial.
  4. **Fase Verde TDD (Implementación)**:
     - Crear modelos de dominio (`Sale`, `OperationType`, `PriceCalculationResult`).
     - Crear contrato `ISaleRepository` e implementación `SaleRepositoryImpl`.
     - Implementar Casos de Uso (`CalculateFinalPriceUseCase`, `RegisterSaleUseCase`, `GetSalesSummaryUseCase`) hasta que todas las pruebas pasen en verde.
  5. **Fase Refactor**:
     - Limpiar el código asegurando que la capa de dominio sea 100% pura y desacoplada.

- **Tabla de Recargos y Modalidades de Pago (Base = $1,000.00)**:
  | Tipo de Operación | Regla / Porcentaje | Resultado Esperado ($) |
  | :--- | :--- | :--- |
  | **`FACTURA_A`** | Recargo IVA +21% | **$1,210.00** |
  | **`FACTURA_B`** | Recargo IVA +10.5% | **$1,105.00** |
  | **`LOTE_100`** | Margen Lote +100% | **$2,000.00** |
  | **`LOTE_110`** | Margen Lote +110% | **$2,100.00** |
  | **`EFECTIVO_NEGRO`** | Ajuste Efectivo +35% | **$1,350.00** |

- **Criterio de Aceptación**: 100% de los tests unitarios ejecutados y aprobados con éxito (`gradlew test`). Grafo de Koin y esquemas de Room KMP compilando sin errores.
- **REPORTE OBLIGATORIO DE FINALIZACIÓN**: Al terminar esta tarea, el agente DEBE mostrar obligatoriamente el siguiente checklist completado con `[x]` y detallar los archivos creados/modificados:
  - [ ] Clase sellada `Result<T>` definida en `domain/model/`.
  - [ ] Esquema de persistencia Room KMP (`SaleEntity`, `SaleDao`, `AppDatabase`) implementado y compilando con KSP.
  - [ ] Contrato `ISaleRepository` e implementación `SaleRepositoryImpl` creados.
  - [ ] Grafo de Inyección de Dependencias Koin configurado en `di/`.
  - [ ] Casos de uso (`CalculateFinalPriceUseCase`, `RegisterSaleUseCase`, `GetSalesSummaryUseCase`) creados mediante flujo TDD.
  - [ ] 100% de las pruebas unitarias aprobadas en verde (`gradlew test`).
  - *Archivos Creados/Modificados*: (Detallar la lista de clases, DAOs, repositorios y tests unitarios desarrollados).

---

### **Tarea 03: Conexión de Capa de Presentación, ViewModels, StateFlow e Integración con DI**
- **Objetivo**: Implementar la capa de presentación (`Presentation`) creando los ViewModels reactivos que transformen los flujos fríos (`Flow`) en estados cálidos (`StateFlow`), conectando los Casos de Uso con la base de datos Room y configurando la inyección de dependencias (Koin) de punta a punta.

- **Pasos a ejecutar**:
  1. **Configuración del Módulo de Koin en Presentación (`di/`)**:
     - Registrar ViewModels mediante `@KoinViewModel` inyectando los casos de uso correspondientes.
  2. **Diseño de Estados de UI Inmutables (`presentation/state/`)**:
     - Crear clases de datos inmutables `SalesUiState` y `CalculationUiState` para guardar centralizadamente los datos de pantalla y los estados envueltos en `Result`.
  3. **Implementación de ViewModels (`presentation/viewmodel/`)**:
     - Crear el `SalesViewModel` consumiendo los `Flow` provenientes de los casos de uso y transformándolos mediante `stateIn` en `StateFlow` cálidos.
  4. **Conexión con Persistencia y Repositorios**:
     - Conectar la acción de registrar ventas y recuperar reportes con Room a través de los casos de uso.
  5. **Pruebas Unitarias de ViewModels (`commonTest/presentation/viewmodel/`)**:
     - Escribir pruebas unitarias para `SalesViewModel` validando la reacción ante las emisiones del `StateFlow`.

- **Criterio de Aceptación**: Compilación exitosa con KSP, grafo de Koin validado sin dependencias faltantes, y pruebas de ViewModel en verde (`gradlew test`).
- **REPORTE OBLIGATORIO DE FINALIZACIÓN**: Al terminar esta tarea, el agente DEBE mostrar obligatoriamente el siguiente checklist completado con `[x]` y detallar los archivos creados/modificados:
  - [ ] Clases de estado de UI inmutables (`SalesUiState` y `CalculationUiState`) definidas.
  - [ ] `SalesViewModel` implementado utilizando `StateFlow` y anotado con `@KoinViewModel`.
  - [ ] Grafo de Koin actualizado con las dependencias de presentación, casos de uso y repositorios.
  - [ ] Pruebas unitarias de ViewModel ejecutadas y aprobadas en verde (`commonTest`).
  - *Archivos Creados/Modificados*: (Detallar la lista de ViewModels, UiStates, módulos Koin y pruebas unitarias asociadas).

---

### **Tarea 04: Menú de Navegación, Vistas en Jetpack Compose Multiplatform y Consumo Asincrónico de Estados (UiState)**
- **Objetivo**: Implementar la inicialización nativa de Koin (`StoreCostApplication`), el sistema de navegación, la actividad principal de Android (`MainActivity`), y las pantallas visuales utilizando **Jetpack Compose (Compose Multiplatform)** (`GestionVentasScreen` y `CierresJornadaScreen`), conectando el menú desplegable y enlazando las vistas mediante la recolección **asincrónica** de los `StateFlow` y estados inmutables (`SalesUiState` y `CalculationUiState`).

- **Pasos a ejecutar**:
  1. **Configuración de Dependencias Visuales y de Navegación (RECORDATORIO OBLIGATORIO)**:
     - Declarar las dependencias de Compose Multiplatform, Material 3 y Navigation Compose en `gradle/libs.versions.toml` y `build.gradle.kts` (`shared` y `app`).
  2. **Inicialización de Koin y Clase `StoreCostApplication` (Android Entry Point)**:
     - Crear la clase `StoreCostApplication : Application()` en el módulo `app` ejecutando `startKoin { androidContext(this@StoreCostApplication); modules(AppModule, DatabaseModule) }` para resolver las dependencias de Room y los ViewModels.
     - Registrar la clase en el archivo `AndroidManifest.xml` mediante el atributo `android:name=".StoreCostApplication"`.
  3. **Creación de la Actividad Principal (`MainActivity`)**:
     - Implementar la clase `MainActivity` en el módulo `app` extendiendo de `ComponentActivity`, configurando `setContent` con Material 3 para renderizar el contenedor raíz de navegación compartida (dejando la actividad limpia solo con la UI).
  4. **Creación del Menú de Navegación (`presentation/screen/components/`)**:
     - Desarrollar el componente de menú desplegable (Drawer / TopAppBar) para alternar entre **Gestión de Ventas** y **Cierres de Jornada**.
  5. **Diseño de la Pantalla de Ventas (`GestionVentasScreen`)**:
     - `OutlinedTextField` para Precio Base ($) y Cantidad.
     - Grupo de `RadioButton` para opciones: `FACTURA_A` (+21%), `FACTURA_B` (+10.5%), `LOTE_100` (+100%), `LOTE_110` (+110%) y `EFECTIVO_NEGRO` (+35%).
     - Tarjeta `Card` con "Resumen en Tiempo Real" (Precio Unitario y Subtotal) recolectando estado con `collectAsStateWithLifecycle()`.
     - Botón "CONFIRMAR Y REGISTRAR VENTA".
  6. **Diseño de la Pantalla de Cierres y Reportes (`CierresJornadaScreen`)**:
     - Pestañas `TabRow` (*Diario | Semanal | Mensual*) y selector de rango de fecha.
     - Tarjeta `Card` con "Métrica Global" (Total Facturado y Desglose).
     - Lista scrolleable `LazyColumn` para el "Historial de Transacciones".
  7. **Wiring de Navegación y ViewModels**:
     - Enlazar las pantallas al enrutador compartiendo las instancias de los ViewModels de Koin.

- **Criterio de Aceptación**: `StoreCostApplication` arrancando Koin con el contexto de Android, `AndroidManifest` actualizado, `MainActivity` configurada limpiamente con Compose, navegación por menú desplegable funcional, recolección asincrónica de estados validada y sincronización con la base de datos.

- **REPORTE OBLIGATORIO DE FINALIZACIÓN**: Al terminar esta tarea, el agente DEBE mostrar obligatoriamente el siguiente checklist completado con `[x]` y detallar los archivos creados/modificados:
  - [ ] Dependencias de Compose y Navigation añadidas en `libs.versions.toml` y `build.gradle.kts`.
  - [ ] Clase `StoreCostApplication` creada con `startKoin` (`androidContext` y módulos) y registrada en `AndroidManifest.xml`.
  - [ ] `MainActivity` implementada y configurada exclusivamente con Material 3 y `setContent`.
  - [ ] Componente de menú de navegación (Drawer / TopAppBar) creado.
  - [ ] `GestionVentasScreen` maquetada con `OutlinedTextField`, `RadioButton` y cálculo en tiempo real.
  - [ ] `CierresJornadaScreen` maquetada con pestañas (*Diario | Semanal | Mensual*), métricas globales y `LazyColumn`.
  - [ ] Enrutador principal y wiring con los ViewModels de Koin completados.
  - *Archivos Creados/Modificados*: (Detallar la lista de aplicaciones, actividades, pantallas y componentes UI creados).
---

