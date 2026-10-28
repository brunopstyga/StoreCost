# Spec 002: Refactorización del Módulo de Gestión de Precios e Historial de Consultas

## 1. Stack Tecnológico, Librerías, UI y Reglas de Arquitectura
- **Framework & Multiplatform**: Kotlin Multiplatform (KMP) + Compose Multiplatform.
- **Arquitectura & Escalabilidad**:
  - Clean Architecture modularizada (Presentation, Domain, Data) con principios SOLID.
  - Diseño altamente escalable: capas desacopladas mediante interfaces y abstracciones en la capa de Dominio.
- **Persistencia**: Room KMP.
- **Inyección de Dependencias**: Koin.
- **Testing**: Test-Driven Development (TDD) sobre `commonTest` utilizando `kotlin.test`.
- **UI & Componentes Visuales (Compose Multiplatform)**:
  - Diseño basado en Material 3 (`org.jetbrains.compose.material3`).
  - Uso de `LazyColumn` para listados eficientes y patrones UDF (*Unidirectional Data Flow*).

---

## 2. Definición de Tareas de Implementación

### **Tarea 01.A: Enfoque TDD - Fase Roja (Pruebas Unitarias del Dominio, Casos de Uso y ViewModel)**
- **Objetivo**: Escribir los tests unitarios en `commonTest` para la lógica de negocio de consultas de precios, validando el comportamiento de los Casos de Uso y del `ConsultingViewModel` antes de escribir el código de producción, asegurando que fallen inicialmente.

- **Pasos a ejecutar**:
  1. **Creación de Fakes y Mocks para el Repositorio de Consultas (`commonTest/.../data/repository/`)**:
     - Implementar un `FakeConsultingRepository` que simule el almacenamiento en memoria y la emisión de flujos de consultas.
  2. **Creación de Pruebas Unitarias para Casos de Uso (`commonTest/.../domain/usecase/`)**:
     - `SavePriceConsultationUseCaseTest.kt`: Validar que al invocar el caso de uso se persista correctamente la consulta a través del repositorio.
     - `GetPriceConsultationsUseCaseTest.kt`: Validar la correcta emisión del `Flow` de historial de consultas.
  3. **Creación de Pruebas Unitarias para el ViewModel (`commonTest/.../presentation/viewmodel/`)**:
     - `ConsultingViewModelTest.kt`: Validar que las interacciones de la UI actualicen el `ConsultingUiState` y disparen los estados de carga, éxito o error de forma reactiva.
- **Criterio de Aceptación**: Pruebas unitarias creadas y ejecutadas con fallo inicial esperado (Fase Roja de TDD) en el entorno `commonTest`.
- **REPORTE OBLIGATORIO DE FINALIZACIÓN**: Al terminar esta tarea, el agente DEBE mostrar obligatoriamente el siguiente checklist completado con `[x]` y detallar los archivos de prueba creados:
  - [ ] `FakeConsultingRepository` implementado para testing.
  - [ ] Tests unitarios de casos de uso (`SavePriceConsultationUseCaseTest`, `GetPriceConsultationsUseCaseTest`) escritos.
  - [ ] Test unitario del ViewModel (`ConsultingViewModelTest`) escrito.
  - [ ] Fallo verificado en la ejecución de pruebas (`gradlew test`).
  - *Archivos Creados/Modificados*: (Detallar la lista de clases Fake y archivos de pruebas unitarias creados).
  
  ### **Tarea 01.B: Refactorización de la Pantalla de Gestión de Precios, Creación de Nueva Entidad y Persistencia**
  
- **Objetivo**: Refactorizar la pantalla de "Gestión de Precios" integrando los campos de entrada, el Spinner con las modalidades, la lista de precios calculados y conectando el botón **"CONSULTAR PRECIOS"** para que guarde un registro en una nueva tabla de la base de datos Room, administrando los estados reactivos con Clean Architecture.

- **Pasos a ejecutar**:
  1. **Creación de la Nueva Entidad de Base de Datos (`PriceConsultationEntity`)**:
     - Definir la entidad Room en `data/local/` con los campos: `id` (Primary Key autoincremental), `basePrice` (Double), `quantity` (Int), `selectedModality` (String), `calculatedUnitPrice` (Double), `calculatedSubtotal` (Double) y `timestamp` (Long).
  2. **Creación del DAO (`PriceConsultationDao`)**:
     - Implementar los métodos de consulta e inserción:
       - `suspend fun insertConsultation(consultation: PriceConsultationEntity)`
       - `fun getAllConsultations(): Flow<List<PriceConsultationEntity>>` (flujo frío)
  3. **Actualización de la Base de Datos (`AppDatabase`)**:
     - Registrar `PriceConsultationEntity` y su respectivo DAO en la configuración de Room KMP.
  4. **Capa de Dominio y Repositorio**:
     - Crear el modelo de dominio puro `PriceConsultation` en `domain/model/`.
     - Crear el contrato de interfaz `IConsultingRepository` en `domain/repository/` declarando los métodos para insertar y obtener las consultas encapsulados en `Result<T>` y `Flow`.
     - Crear la clase de implementación `ConsultingRepositoryImpl` en `data/repository/` extendiendo el contrato de interfaz, mapeando las entidades de Room a modelos de dominio y consumiendo `PriceConsultationDao`.
  5. **Casos de Uso (`domain/usecase/`)**:
     - Crear `SavePriceConsultationUseCase` para persistir la consulta en Room.
     - Crear `GetPriceConsultationsUseCase` para exponer el `Flow` frío con el historial de consultas de precios.
  6. **Capa de Presentación, UiState y ViewModel**:
     - Crear la clase **`ConsultingUiState`** en el paquete `presentation/state/` para encapsular de forma inmutable los valores de la pantalla, el listado de precios calculados y los estados de `Result` (`Loading`, `Success`, `Error`).
     - Crear `ConsultingViewModel` en `presentation/viewmodel/` anotado con `@KoinViewModel`. Consumir los `Flow` fríos provenientes de los Casos de Uso y transformarlos mediante `stateIn` en **`StateFlow`** cálidos de tipo `ConsultingUiState`.
  7. **Refactorización de la Interfaz Visual (`GestionPreciosScreen`)**:
     - Consumir el `StateFlow` del `ConsultingViewModel` de forma asincrónica (`collectAsStateWithLifecycle()`).
     - Maquetar la UI según el diseño especificado:
       - Campo de texto para Precio Base (`OutlinedTextField`).
       - Campo de texto para Cantidad de Unidades (`OutlinedTextField`).
       - Spinner desplegable (`ExposedDropdownMenuBox`) para seleccionar la modalidad (`FACTURA_A` +21%, `FACTURA_B` +10.5%, `LOTE_100` +100%, `LOTE_110` +110%, `EFECTIVO_NEGRO` +35%).
       - Listado de precios calculados (`LazyColumn`).
       - Botón inferior **"CONSULTAR PRECIOS"**.
     - Vincular el evento del botón con la acción del ViewModel para calcular la lista y persistir el registro en la base de datos de manera limpia.

- **Criterio de Aceptación**:
  - Nueva entidad, DAO y `AppDatabase` compilando correctamente con KSP en Room KMP.
  - Implementación de `ConsultingRepositoryImpl` desacoplada en la capa de datos.
  - `ConsultingUiState` expuesto mediante `StateFlow` desde el ViewModel.
  - Interfaz `GestionPreciosScreen` maquetada y consumiendo el estado sin llamadas directas a persistencia.
  - Persistencia comprobada en la base de datos local al pulsar "CONSULTAR PRECIOS".

- **REPORTE OBLIGATORIO DE FINALIZACIÓN**: Al terminar esta tarea, el agente DEBE mostrar obligatoriamente el siguiente checklist completado con `[x]` y detallar los archivos creados/modificados:
  - [ ] Entidad `PriceConsultationEntity` y `PriceConsultationDao` creados y añadidos a `AppDatabase`.
  - [ ] Contrato `IConsultingRepository` e implementación `ConsultingRepositoryImpl` creados.
  - [ ] Casos de uso `SavePriceConsultationUseCase` y `GetPriceConsultationsUseCase` implementados.
  - [ ] Clase `ConsultingUiState` creada en el paquete `presentation/state/`.
  - [ ] `ConsultingViewModel` implementado convirtiendo `Flow` fríos a `StateFlow` cálidos.
  - [ ] `GestionPreciosScreen` refactorizada consumiendo `ConsultingUiState` y conectada al evento de consulta.
  - *Archivos Creados/Modificados*: (Detallar la lista de entidades, DAOs, repositorios, UseCases, UiStates, ViewModels y Compose Screens).
┌────────────────────────────────────────┐
│  Gestión de Precios                    │
├────────────────────────────────────────┤
│                                        │
│  [Precio Base ($)]                     │
│  > 1000.00                             │
│                                        │
│  [Cantidad de Unidades]                │
│  > 2                                   │
│                                        │
│  Seleccionar Modalidad:                │
│  [ v ] FACTURA_A                   ▼   │
│                                        │
│  Lista de Precios Calculados:          │
│  ┌──────────────────────────────────┐  │
│  │ • FACTURA_A: $1,210.00 (Sub: ... │  │
│  │ • FACTURA_B: $1,105.00 (Sub: ... │  │
│  └──────────────────────────────────┘  │
│                                        │
│  [         CONSULTAR PRECIOS        ]  │
└────────────────────────────────────────┘

### **Tarea 02: Refactorización de la Pantalla de Ventas, Cierre de Jornada y Registro Directo de Transacciones**

- **Objetivo**: Refactorizar la pantalla de "Cierres de Jornada y Reportes" para convertirla en la pantalla de **"Ventas y Cierre"**,en el menu en lugar de cierres de jornada que diga Ventas y Cierre, removiendo la tarjeta de métrica global,
 ajustando el formulario para registrar ventas directamente con los inputs de *Importe Vendido ($)* y *Cantidad de Unidades*, reusando la infraestructura existente de persistencia (`SaleEntity`, `SaleDao` y `ISaleRepository`) 
 y manteniendo la lista scrolleable del historial de transacciones. Además, agregar un botón de **Cierre Diario** que calcule y persista el importe total vendido del día, y lo muestre en pantalla.

- **Pasos a ejecutar**:
  1. **Reutilización y Ajuste de la Capa de Datos y Dominio**:
     - Consumir la entidad `SaleEntity` y los métodos existentes en `SaleDao` (`insertSale` y `getSalesByDateRange`).
     - Asegurar el mapeo correcto con el modelo de dominio `Sale` e `ISaleRepository` / `SaleRepositoryImpl` sin alterar las pruebas unitarias existentes.
  2. **Casos de Uso de Registro e Historial (`domain/usecase/`)**:
     - Utilizar `RegisterSaleUseCase` para procesar y persistir el registro del *Importe Vendido ($)* y *Cantidad de Unidades*.
     - Utilizar `GetSalesSummaryUseCase` para exponer el `Flow` frío con la lista cronológica de transacciones filtrada por rango de fecha y tipo de reporte (Diario, Semanal, Mensual).
  3. **Capa de Presentación, UiState y ViewModel**:
     - Actualizar `SalesUiState` en `presentation/state/` para representar el formulario de registro de venta directa (inputs de importe, cantidad y selección de fecha/pestaña) y el listado inmutable del historial.
     - Ajustar `SalesViewModel` en `presentation/viewmodel/` transformando los `Flow` fríos en `StateFlow` cálidos mediante `stateIn`.
  4. **Refactorización de la Interfaz Visual (`VentasScreen` / `CierresJornadaScreen`)**:
     - Actualizar el título de la pantalla a **"Ventas"** (o **"Ventas y Cierre"**).
     - Mantener el selector de pestañas superiores (*Diario | Semanal | Mensual*) y el picker de fecha/rango. *(La pestaña *Semanal* se reemplaza por *Historial* en la Tarea 02A.)*
     - **Formulario de Registro**: Maquetar la sección "Registrar Venta" con:
       - Campo de texto para *Importe Vendido ($)* (`OutlinedTextField`).
       - Campo de texto para *Cantidad de Unidades* (`OutlinedTextField`).
       - Botón **"REGISTRAR VENTA"**.
     - **Eliminar Métrica Global**: Remover la tarjeta de totales y desgloses por operación.
     - **Historial de Transacciones**: Renderizar el listado mediante una **`LazyColumn`** optimizada mostrando el detalle de cada transacción registrada.
 5. **Actualización del Menú de Navegación**:
     - Modificar el componente de navegación (Drawer / TopBar) para **esconder** el acceso directo a la función de gestión de ventas según corresponda en el flujo de la app.
  6. **Cierre Diario (NUEVO)**:
     - Agregar en la pantalla **Ventas y Cierre** el botón **"REALIZAR CIERRE DEL DÍA"**.
     - Al pulsarlo, calcular el importe total vendido y la cantidad de operaciones del día (rango local con `kotlinx-datetime`) y mostrarlos en un **diálogo de confirmación**; al confirmar, persistirlos en Room y reflejarlos en pantalla.
     - **Un único cierre por día** (`closingDate` con índice único). Si se vuelve a cerrar el mismo día, se **actualiza** el registro existente con el nuevo total y cantidad.
     - **Día sin ventas**: el botón permanece deshabilitado y no se guarda ningún cierre.
     - **Diálogo de confirmación (`AlertDialog`)**: muestra el total del día y la cantidad de operaciones, con botones **Cancelar** y **Confirmar**. El cierre se guarda recién al confirmar.
     - **Datos a persistir**: `closingDate`, `totalAmount`, `salesCount` y `closedAt` (sin desglose por modalidad).
     - **Capas a crear**:
       - `DailyClosingEntity` y `DailyClosingDao` en `data/local/` (`insertClosing` con `OnConflictStrategy.REPLACE`, `getClosingByDate`).
       - `DailyClosing` en `domain/model/`, `IClosingRepository` en `domain/repository/`, `CloseDayUseCase` y `GetDailyClosingUseCase` en `domain/usecase/`, `ClosingRepositoryImpl` en `data/repository/`.
       - `AppDatabase` **versión 3** registrando la nueva entidad y DAO; se mantiene `fallbackToDestructiveMigration(dropAllTables = true)` (sin migración real).
       - `SalesUiState` expone `closingState: Result<DailyClosing>?`; `SalesViewModel` agrega `onCloseDay()`; la UI muestra el cierre guardado (total, cantidad y fecha/hora).
     - **Tests**: `CloseDayUseCaseTest` y ampliación de `SalesViewModelTest` (éxito, error, día sin ventas y actualización del mismo día).
  7. **Pruebas Unitarias (`commonTest/`)**:
     - Verificar que los tests unitarios de `SalesViewModelTest` y `RegisterSaleUseCaseTest` pasen en verde validando las emisiones reactivas.
   	  

- **Criterio de Aceptación**:
  - Pantalla refactorizada según la nueva interfaz visual sin métrica global.
  - Formulario conectado exitosamente con `SaleDao` registrando ventas en la base de datos Room KMP.
  - Historial en `LazyColumn` actualizándose en tiempo real mediante recolección asincrónica de `StateFlow`.
  - Botón de **Cierre Diario** funcional: guarda o actualiza en Room el total y la cantidad de ventas del día, y muestra el resultado.
  - El cierre se confirma mediante un **diálogo previo** (Cancelar / Confirmar) antes de persistir.
  - No se genera cierre para días sin ventas.
  - Pruebas unitarias aprobadas en verde (`gradlew test`).

- **REPORTE OBLIGATORIO DE FINALIZACIÓN**: Al terminar esta tarea, el agente DEBE mostrar obligatoriamente el siguiente checklist completado con `[x]` y detallar los archivos creados/modificados:
  - [ ] Interfaz visual refactorizada a "Ventas" eliminando la métrica global y agregando el formulario directo de venta.
  - [ ] Conexión del formulario de *Importe Vendido ($)* y *Cantidad* con `RegisterSaleUseCase` y `SaleDao`.
  - [ ] Consumo asincrónico del `StateFlow` en `VentasScreen` mediante `collectAsStateWithLifecycle()`.
  - [ ] Visualización del historial de transacciones en `LazyColumn` validada.
  - [ ] Botón **"REALIZAR CIERRE DEL DÍA"** agregado y conectado al ViewModel.
  - [ ] Entidad, DAO, repositorio y casos de uso del Cierre Diario creados (un registro por día, actualizable).
  - [ ] `AppDatabase` actualizada a versión 3 manteniendo el borrado destructivo.
  - [ ] Cierre Diario validado: total/cantidad del día persistidos y mostrados; sin cierre en días sin ventas.
  - [ ] Diálogo de confirmación (Cancelar / Confirmar) mostrado antes de persistir el cierre.
  - [ ] Pruebas unitarias ejecutadas y aprobadas en verde (`commonTest`).
  - *Archivos Creados/Modificados*: (Detallar la lista de pantallas, UiStates, ViewModels, UseCases y tests unitarios ajustados).
  
┌────────────────────────────────────────┐
│  Ventas y Cierre                       │
├────────────────────────────────────────┤
│  [Pestañas: Diario | Semanal | Mensual]│
│                                        │
│  Rango: [ 21/10/2026 📅 ]              │
│                                        │
│  Registrar Venta:                      │
│           │
│  [ Importe Vendido ($) ]               │
│  [ Cantidad de Unidades ]              │
│  [         REGISTRAR VENTA          ]  │
│                                        │
│  [     REALIZAR CIERRE DEL DÍA      ]  │
│  ✅ Cierre: $2,420.00 · 2 ventas        │
│                                        │
│  ┌─ Confirmar cierre ───────────────┐  │
│  │ Total del día: $2,420.00         │  │
│  │ Operaciones: 2                   │  │
│  │ [Cancelar]      [Confirmar]      │  │
│  └──────────────────────────────────┘  │
│                                        │
│  Historial de Transacciones:           │
│  ┌──────────────────────────────────┐  │
│  │ 🔄 LazyColumn (Scrollable)       │  │
│  │ • $2,420.00                      │  │
│  │    2 u. x $1,210.00              │  │
│  │                                  │  │
│  │                                  │  │
│  └──────────────────────────────────┘  │
│                                        │
│                                        │
└────────────────────────────────────────┘

> **Nota:** la **Tarea 02A** reemplaza el comportamiento de la pestaña *Semanal* descrito en la Tarea 02 y mueve el historial de transacciones a una pestaña propia.

### **Tarea 02A: Refactor de Pestañas — Reemplazo de "Semanal" por "Historial de Transacciones"**

- **Objetivo**: Eliminar la pestaña **"Semanal"** de la pantalla **Ventas y Cierre** y reemplazarla por la pestaña **"Historial"**, donde se muestra la pantalla de historial de transacciones (listado completo de ventas, sin filtro por rango de fechas). El historial deja de renderizarse debajo del formulario en el resto de las pestañas; el formulario de registro y el Cierre Diario permanecen operativos en **Diario** y **Mensual**.

- **Pasos a ejecutar**:
  1. **Estado de Presentación (`presentation/state/SalesUiState.kt`)**:
     - Reemplazar la entrada `WEEKLY` del enum `ClosingPeriod` por `HISTORY`. El orden final debe ser `DAILY`, `HISTORY`, `MONTHLY` para conservar la posición visual que hoy ocupa "Semanal" (segunda pestaña).
     - Agregar propiedades derivadas: `isHistory` (`period == ClosingPeriod.HISTORY`), `showSaleForm` (`period != ClosingPeriod.HISTORY`) y `hasSales` (`sales.isNotEmpty()`).
     - Documentar que `HISTORY` no aplica filtro de fecha: representa el historial completo de transacciones.
  2. **ViewModel (`presentation/viewmodel/SalesViewModel.kt`)**:
     - En `rangeFor(period)`, reemplazar la rama `WEEKLY` por `HISTORY` devolviendo el rango completo (`Long.MIN_VALUE` a `Long.MAX_VALUE`), de modo que `getSalesByDateRange` devuelva todas las ventas.
     - Eliminar los imports que queden sin uso (`DatePeriod`, `minus`, `plus`, `dayOfWeek`, etc. según corresponda).
     - `onDateRangeSelected(...)` debe ignorar el cambio cuando `period == HISTORY` (el selector de fecha no está visible en esa pestaña).
     - Mantener intactos `onRegisterSale()`, `onRequestCloseDay()`, `onConfirmCloseDay()` y `loadTodayClosing()`.
  3. **Refactorización de la UI (`presentation/screen/VentasScreen.kt`)**:
     - En `TabRow`, la pestaña "Semanal" pasa a llamarse **"Historial"** (`periodLabel` → `HISTORY -> "Historial"`).
     - Ocultar el bloque `Período: ... / Cambiar fecha` y el `SalesDatePicker` cuando `state.isHistory`.
     - Ocultar el formulario "Registrar Venta" (`OutlinedTextField` de importe y cantidad), el botón **"REGISTRAR VENTA"**, el botón **"REALIZAR CIERRE DEL DÍA"**, la tarjeta del cierre guardado y sus mensajes de error/éxito cuando `state.isHistory`.
     - En la pestaña `HISTORY`, renderizar una columna con el título **"Historial de Transacciones"**, el contador de operaciones (`state.sales.size`) y el listado ocupando todo el alto disponible.
     - Extraer el listado a un composable reutilizable **`TransactionHistorySection`** en `presentation/screen/components/TransactionHistorySection.kt`, que reciba `sales`, `isLoading` y `errorMessage`, y renderice:
       - `LazyColumn` con `SaleHistoryItem` (clave por `sale.id`).
       - Estado de carga (`LinearProgressIndicator` o ítem de carga).
       - Estado vacío: "Todavía no hay transacciones registradas."
     - Eliminar la `LazyColumn` del cuerpo compartido de la pantalla: el historial ya no se muestra en Diario ni en Mensual.
  4. **Limpieza de Código Muerto**:
     - Eliminar `getWeeklyClosing(startOfWeek, endOfWeek)` de `GetSalesSummaryUseCase`, al quedar sin consumidores.
     - Verificar que no queden referencias a `WEEKLY`, "Semanal" ni al método eliminado en `shared` ni en `commonTest`.
     - No modificar `CloseDayUseCase`, `GetDailyClosingUseCase`, `RegisterSaleUseCase`, `GenerateMonthlyReportUseCase` ni la persistencia Room.
  5. **Pruebas Unitarias (`commonTest/`)**:
     - `SalesViewModelTest.kt`: ajustar cualquier referencia a `WEEKLY` y agregar:
       - `seleccionar la pestaña Historial expone todas las ventas sin filtro de fecha` (verificar `period == HISTORY`, `isHistory == true` y que el listado contenga las ventas de todos los rangos).
       - `seleccionar Diario restaura el formulario y el cierre` (`showSaleForm == true`).
       - `registrar una venta desde Diario actualiza el historial` (el listado refleja la nueva venta).
     - `GetSalesSummaryUseCaseTest.kt`: reemplazar la prueba de `getWeeklyClosing` por una de rango completo (`Long.MIN_VALUE..Long.MAX_VALUE`) que devuelva la totalidad de las ventas mockeadas.
     - Ejecutar `gradlew test` y verificar que todo quede en verde.

- **Criterio de Aceptación**:
  - La pestaña "Semanal" ya no existe; en su lugar aparece **"Historial"** y al seleccionarla se visualiza el historial de transacciones.
  - El historial no se renderiza en las pestañas Diario ni Mensual.
  - El listado muestra todas las transacciones sin importar la fecha, con estados de carga y vacío.
  - El formulario de registro y el Cierre Diario siguen funcionando en Diario. *(La Tarea 02B los restringe definitivamente a esa pestaña.)*
  - `WEEKLY` y `getWeeklyClosing` eliminados sin referencias residuales.
  - Pruebas unitarias aprobadas en verde (`gradlew test`).

- **REPORTE OBLIGATORIO DE FINALIZACIÓN**: Al terminar esta tarea, el agente DEBE mostrar obligatoriamente el siguiente checklist completado con `[x]` y detallar los archivos creados/modificados:
  - [ ] Pestaña "Semanal" reemplazada por "Historial" en el `TabRow`.
  - [ ] `ClosingPeriod.WEEKLY` reemplazado por `ClosingPeriod.HISTORY` sin referencias residuales.
  - [ ] `rangeFor(HISTORY)` devuelve el rango completo y el historial muestra todas las transacciones.
  - [ ] Historial extraído a `TransactionHistorySection` con `LazyColumn`, estado vacío y estado de carga.
  - [ ] Historial removido de las pestañas Diario y Mensual.
  - [ ] Formulario de venta y Cierre Diario operativos en Diario.
  - [ ] `getWeeklyClosing` eliminado de `GetSalesSummaryUseCase`.
  - [ ] Tests actualizados (`SalesViewModelTest`, `GetSalesSummaryUseCaseTest`) y `gradlew test` en verde.
  - *Archivos Creados/Modificados*: (Detallar la lista de estados, ViewModels, composables y tests unitarios ajustados).

```
┌────────────────────────────────────────┐
│  Ventas y Cierre                       │
├────────────────────────────────────────┤
│  [ Diario | Historial | Mensual ]      │
│                                        │
│  Historial de Transacciones            │
│  Operaciones: 2                        │
│  ┌──────────────────────────────────┐  │
│  │ 🔄 LazyColumn (Scrollable)       │  │
│  │ • $2,420.00                      │  │
│  │    2 u. x $1,210.00              │  │
│  │    21/10/2026 14:32              │  │
│  │                                  │  │
│  │ • $1,350.00                      │  │
│  │    1 u. x $1,350.00              │  │
│  └──────────────────────────────────┘  │
│                                        │
└────────────────────────────────────────┘
```

### **Tarea 02B: Resumen Mensual de Ventas y Restricción de Controles a la Pestaña Diario**

> **Nota:** esta tarea ajusta lo definido en la Tarea 02A: la pestaña *Mensual* deja de mostrar el formulario, el selector de fecha y el Cierre Diario, y pasa a ser una vista de resumen.

- **Objetivo**: Convertir la pestaña **Mensual** en una vista de solo lectura con el resumen de las ventas del mes en curso (totales + desglose por modalidad), y restringir el formulario de registro, el selector de período/fecha y el Cierre Diario exclusivamente a la pestaña **Diario**. La exportación del reporte mensual se mantiene dentro de la pestaña Mensual.

- **Pasos a ejecutar**:
  1. **Estado de Presentación (`presentation/state/SalesUiState.kt`)**:
     - Agregar las propiedades derivadas `isDaily` (`period == ClosingPeriod.DAILY`) e `isMonthly` (`period == ClosingPeriod.MONTHLY`).
     - Redefinir `showSaleForm` para que sea `period == ClosingPeriod.DAILY` (antes era `period != HISTORY`).
  2. **ViewModel (`presentation/viewmodel/SalesViewModel.kt`)**:
     - Sin consultas nuevas: `rangeFor(MONTHLY)` ya abarca desde el día 1 hasta el último día del mes en curso y `summary` contiene las ventas del mes.
     - `onDateRangeSelected(...)` debe aplicarse solo cuando `period == DAILY`; en Historial y Mensual se ignora (Historial usa rango completo y Mensual siempre el mes en curso).
  3. **Refactorización de la UI (`presentation/screen/VentasScreen.kt`)**:
     - Reemplazar el `if (isHistory) ... else { ... }` por un `when (state.period)` con tres ramas:
       - `DAILY`: extraer el contenido a `DailySalesContent` (período, "Cambiar fecha", formulario, **REGISTRAR VENTA**, **REALIZAR CIERRE DEL DÍA**, tarjeta del cierre y mensajes).
       - `HISTORY`: `TransactionHistorySection` (sin cambios).
       - `MONTHLY`: `MonthlySummarySection` (solo resumen y exportación).
     - El `SalesDatePicker` se renderiza únicamente cuando `state.isDaily`.
     - Eliminar de la vista mensual el formulario, los botones de venta/cierre, el selector de período y la tarjeta de cierre.
  4. **Nuevo `components/MonthlySummarySection.kt`**:
     - Recibe `startTime`, `summary: Result<SalesSummary>`, `reportState` y el callback de exportación.
     - Título **"Resumen de [Mes Año]"** (ej. "Resumen de Octubre 2026").
     - Tarjeta de totales: **Total vendido**, **Operaciones** y **Ticket promedio** (`totalBilled / salesCount`).
     - **Por modalidad**: listado del `breakdown` ordenado por importe descendente, usando el nombre de `OperationType` sin guiones bajos.
     - Botón **"EXPORTAR Y ENVIAR REPORTE MENSUAL (.CSV)"** con estado `Loading`/`Error` del `ReportUiState`.
     - Estados de carga, error y mes sin ventas ("No hay ventas registradas en este mes.").
     - Contenido con `verticalScroll` para pantallas pequeñas.
  5. **Utilidades de Formato (`presentation/util/Format.kt`)**:
     - Agregar `formatMonthYear(timestamp)` que devuelve el mes en español y el año ("Octubre 2026").
  6. **Pruebas Unitarias (`commonTest/`)**:
     - `SalesViewModelTest`:
       - `seleccionar Mensual expone el resumen del mes y oculta el formulario` (`isMonthly == true`, `isDaily == false`, `showSaleForm == false`, total y operaciones correctos).
       - `cambiar el rango de fechas se ignora fuera de Diario`.
       - `cambiar el rango de fechas se aplica en Diario`.
     - `FormatTest`: validar `formatMonthYear` con una fecha conocida.
     - Ejecutar `gradlew test` y verificar verde.

- **Criterio de Aceptación**:
  - La pestaña **Mensual** muestra únicamente el resumen del mes y la exportación CSV.
  - El formulario, "Cambiar fecha", **REGISTRAR VENTA** y **REALIZAR CIERRE DEL DÍA** solo se ven en **Diario**.
  - El resumen mensual muestra total vendido, operaciones, ticket promedio y desglose por modalidad.
  - El rango mensual no puede alterarse desde el selector de fecha.
  - Pruebas unitarias aprobadas en verde (`gradlew test`).

- **REPORTE OBLIGATORIO DE FINALIZACIÓN**: Al terminar esta tarea, el agente DEBE mostrar obligatoriamente el siguiente checklist completado con `[x]` y detallar los archivos creados/modificados:
  - [ ] `isDaily`, `isMonthly` y `showSaleForm` ajustados en `SalesUiState`.
  - [ ] `onDateRangeSelected` restringido a la pestaña Diario.
  - [ ] `VentasScreen` reorganizada con `when (period)` y `DailySalesContent`.
  - [ ] `MonthlySummarySection` creado con totales, ticket promedio y desglose por modalidad.
  - [ ] Formulario, selector de fecha y Cierre Diario ocultos en Mensual.
  - [ ] Botón de exportación mensual conservado en `MonthlySummarySection`.
  - [ ] `formatMonthYear` agregado a `Format.kt`.
  - [ ] Tests de ViewModel y formato actualizados y `gradlew test` en verde.
  - *Archivos Creados/Modificados*: (Detallar la lista de estados, ViewModels, composables, utilidades y tests ajustados).

```
┌────────────────────────────────────────┐
│  Ventas y Cierre                       │
├────────────────────────────────────────┤
│  [ Diario | Historial | Mensual ]      │
│                                        │
│  Resumen de Octubre 2026               │
│  ┌──────────────────────────────────┐  │
│  │ Total vendido                    │  │
│  │ $2.560,00                        │  │
│  │ Operaciones: 2                   │  │
│  │ Ticket promedio: $1.280,00       │  │
│  └──────────────────────────────────┘  │
│                                        │
│  Por modalidad                         │
│  ┌──────────────────────────────────┐  │
│  │ FACTURA A             $2.560,00  │  │
│  └──────────────────────────────────┘  │
│                                        │
│  [ EXPORTAR Y ENVIAR REPORTE (.CSV) ]  │
└────────────────────────────────────────┘
```

### **Tarea 03: Generación de Reportes Periódicos (CSV/PDF) y Automatización de Envío Mensual**

- **Objetivo**: Implementar la generación y exportación del informe mensual en formato CSV, agregando un botón de exportación manual en la pantalla de **Ventas y Cierre** (pestaña *Mensual*), integrando la ejecución automatizada en segundo plano mediante `WorkManager` (Android) y disponiendo el flujo de envío asistido vía `Intent` de correo electrónico con el archivo adjunto.

- **Pasos a ejecutar**:
  1. **Ubicación en la UI e Integración en la Pantalla (`VentasScreen` / `CierresJornadaScreen`)**:
     - En la pantalla de **Ventas**, dentro de la vista correspondiente a la pestaña **Mensual**, agregar un botón de acción en el encabezado o pie de pantalla denominado **"EXPORTAR Y ENVIAR REPORTE MENSUAL (.CSV)"**.
  2. **Configuración de Dependencias de Archivos y WorkManager (RECORDATORIO OBLIGATORIO)**:
     - Declarar la dependencia de `androidx.work:work-runtime-ktx` y librerías de archivos en `gradle/libs.versions.toml` y sincronizar en `shared/build.gradle.kts` y `app/build.gradle.kts`.
  3. **Caso de Uso de Exportación (`GenerateMonthlyReportUseCase`)**:
     - Crear la clase `GenerateMonthlyReportUseCase` en `domain/usecase/` que consuma `ISaleRepository`, extraiga las ventas del mes consultado en Room y genere una cadena/archivo en formato `.csv` (con columnas: `ID`, `Fecha`, `Modalidad`, `PrecioUnitario`, `Cantidad`, `Subtotal`).
  4. **ViewModel y Estado de Presentación (`ReportViewModel` y `ReportUiState`)**:
     - Crear la clase de estado `ReportUiState` en `presentation/state/` (`Idle`, `Loading`, `ExportSuccess(filePath)`, `Error(message)`).
     - Crear `ReportViewModel` en `presentation/viewmodel/` anotado con `@KoinViewModel` que inyecte `GenerateMonthlyReportUseCase`. Exponer un `StateFlow<ReportUiState>` cálido mediante `stateIn`.
     - La pantalla `VentasScreen` consumirá `ReportViewModel` mediante `collectAsStateWithLifecycle()` para reaccionar al estado de generación del archivo y disparar la acción del botón.
  5. **Worker en Segundo Plano (`MonthlyReportWorker`)**:
     - Crear la clase `MonthlyReportWorker` extendiendo de `CoroutineWorker` en la capa nativa de Android (`app/.../worker/`).
     - Configurar `PeriodicWorkRequestBuilder` en `StoreCostApplication` para ejecutar una verificación al inicio de cada mes, ejecutar el Caso de Uso y emitiendo una notificación local de Android al finalizar.
  6. **Mecanismo de Envío Asistido vía Intent (`EmailReportLauncher`)**:
     - Al pulsar el botón de exportación en la UI o la notificación local, invocar un helper de plataforma (`EmailReportLauncher`) que cree un `Intent(Intent.ACTION_SEND)` con MIME type `text/csv`, adjuntando el archivo mediante `FileProvider` (`content://...`) y precargando el asunto: *"Reporte Mensual de Ventas - Store Cost"*.
  7. **Pruebas Unitarias del Generador de Reportes (`commonTest/domain/usecase/`)**:
     - Crear `GenerateMonthlyReportUseCaseTest.kt` validando que la estructura del texto CSV generado coincida exactamente con los registros mockeados del repositorio.

- **Criterio de Aceptación**: Botón de exportación funcional en la pestaña *Mensual* de `VentasScreen`, `ReportViewModel` expuesto con `StateFlow`, archivo `.csv` formateado correctamente desde Room, worker de Android programado y flujo de `Intent` de correo disparándose con el archivo adjunto.

- **REPORTE OBLIGATORIO DE FINALIZACIÓN**: Al terminar esta tarea, el agente DEBE mostrar obligatoriamente el siguiente checklist completado con `[x]` y detallar los archivos creados/modificados:
  - [ ] Dependencias de `WorkManager` agregadas y sincronizadas en Gradle.
  - [ ] Caso de uso `GenerateMonthlyReportUseCase` implementado en dominio.
  - [ ] `ReportUiState` y `ReportViewModel` creados e integrados en Koin.
  - [ ] Botón **"EXPORTAR Y ENVIAR REPORTE MENSUAL (.CSV)"** maquetado en la pestaña *Mensual* de `VentasScreen`.
  - [ ] `MonthlyReportWorker` y notificación local implementados en Android.
  - [ ] Helper `EmailReportLauncher` configurado con `FileProvider` e `Intent.ACTION_SEND`.
  - [ ] Pruebas unitarias `GenerateMonthlyReportUseCaseTest` aprobadas en verde (`commonTest`).
  - *Archivos Creados/Modificados*: (Detallar la lista de UseCases, ViewModels, UiStates, Workers, Helpers e interfaces UI desarrolladas).
  
  # Spec 003: Tarea 04 - Login Básico por Contraseña y Refactorización de Clases Base

## 1. Stack Tecnológico, Librerías, UI y Reglas de Arquitectura
- **Framework & Multiplatform**: Kotlin Multiplatform (KMP) + Compose Multiplatform.
- **Arquitectura & Escalabilidad**:
  - Clean Architecture modularizada (Presentation, Domain, Data) con principios SOLID.
  - Inyección de dependencias con Koin.
  - Persistencia de sesión y credenciales mediante abstracción de clave-valor / Settings local.
- **Testing**: Test-Driven Development (TDD) sobre `commonTest` utilizando `kotlin.test`.
- **UI & Componentes Visuales (Compose Multiplatform)**:
  - Diseño basado en Material 3 (`org.jetbrains.compose.material3`).
  - Campo de contraseña enmascarado (`PasswordVisualTransformation`).
  - Navegación condicional basada en estado reactivo de sesión.

---

## 2. Definición de Tareas de Implementación

### **Tarea 04.A: Enfoque TDD - Fase Roja (Pruebas Unitarias de Autenticación, Casos de Uso y LoginViewModel)**
- **Objetivo**: Escribir las pruebas unitarias en `commonTest` para la lógica de autenticación, validando el comportamiento de la verificación de contraseña y la sesión persistida antes de escribir el código de producción.

- **Pasos a ejecutar**:
  1. **Creación de Fake Repository para Autenticación (`commonTest/.../data/repository/`)**:
     - Crear `FakeAuthRepository` implementando `IAuthRepository` con simulación en memoria de la contraseña válida y el estado de sesión actual.
  2. **Creación de Pruebas Unitarias para Casos de Uso (`commonTest/.../domain/usecase/`)**:
     - `LoginWithPasswordUseCaseTest.kt`: Validar retorno exitoso con contraseña correcta, fallo con contraseña incorrecta y validación de campo vacío.
     - `CheckAuthStatusUseCaseTest.kt`: Validar la emisión reactiva del flujo `Flow<Boolean>` indicando si existe una sesión activa.
     - `LogoutUseCaseTest.kt`: Validar la limpieza de la sesión en el repositorio.
  3. **Creación de Pruebas Unitarias para ViewModel (`commonTest/.../presentation/viewmodel/`)**:
     - `LoginViewModelTest.kt`: Validar la actualización de `LoginUiState` en respuesta a los eventos del usuario (ingreso de contraseña, click en Ingresar, error de autenticación y transición a éxito).
- **Criterio de Aceptación**: Pruebas unitarias creadas y ejecutadas con fallo inicial esperado (Fase Roja) en `commonTest`.
- **REPORTE OBLIGATORIO DE FINALIZACIÓN**:
  - [ ] `FakeAuthRepository` implementado para testing.
  - [ ] Tests unitarios de Casos de Uso (`LoginWithPasswordUseCaseTest`, `CheckAuthStatusUseCaseTest`, `LogoutUseCaseTest`) escritos.
  - [ ] Test unitario del ViewModel (`LoginViewModelTest`) escrito.
  - [ ] Fallo verificado en la ejecución de pruebas (`gradlew test`).
  - *Archivos Creados/Modificados*: (Detallar lista de Fakes y archivos de tests unitarios creados).

---

### **Tarea 04.B: Implementación de la Capa de Dominio, Datos, Persistencia de Sesión y ViewModel**

- **Objetivo**: Implementar la lógica de negocio, persistencia de contraseña/sesión local y el `LoginViewModel` encargado de gestionar los estados de la pantalla de Login.

- **Pasos a ejecutar**:
  1. **Capa de Dominio (`domain/`)**:
     - Crear el modelo de dominio `UserSession` en `domain/model/`.
     - Crear el contrato de interfaz `IAuthRepository` en `domain/repository/` declarando:
       - `suspend fun login(password: String): Result<Boolean>`
       - `fun observeIsLoggedIn(): Flow<Boolean>`
       - `suspend fun logout(): Result<Unit>`
     - Crear los Casos de Uso en `domain/usecase/`:
       - `LoginWithPasswordUseCase`
       - `CheckAuthStatusUseCase`
       - `LogoutUseCase`
  2. **Capa de Datos y Persistencia (`data/`)**:
     - Crear la fuente de datos local `AuthLocalDataSource` (utilizando `KVStorage`, `Settings` o Room KMP para guardar el flag de sesión iniciada y el hash de la contraseña).
     - Crear `AuthRepositoryImpl` en `data/repository/` implementando `IAuthRepository`.
  3. **Capa de Presentación y Estado (`presentation/`)**:
     - Crear `LoginUiState` en `presentation/state/`:
       - Propiedades: `password: String = ""`, `isLoading: Boolean = false`, `errorMessage: String? = null`, `isAuthenticated: Boolean = false`, `isPasswordVisible: Boolean = false`.
     - Crear `LoginViewModel` en `presentation/viewmodel/` anotado con `@KoinViewModel`:
       - Expone `StateFlow<LoginUiState>` mediante `stateIn`.
       - Métodos: `onPasswordChanged(newPassword: String)`, `onTogglePasswordVisibility()`, `onLoginClicked()`, `onLogoutClicked()`.
  4. **Inyección de Dependencias (`di/AppModule.kt`)**:
     - Registrar `AuthLocalDataSource`, `IAuthRepository` (`AuthRepositoryImpl`), Casos de Uso y `LoginViewModel` en el módulo de Koin.

- **Criterio de Aceptación**:
  - `LoginViewModel` y Casos de Uso compilando y pasando los tests creados en la Tarea 04.A en verde.
  - Inyección de dependencias configurada correctamente en Koin.

- **REPORTE OBLIGATORIO DE FINALIZACIÓN**:
  - [ ] Contrato `IAuthRepository` e implementación `AuthRepositoryImpl` creados.
  - [ ] Casos de Uso de autenticación implementados en la capa de dominio.
  - [ ] `LoginUiState` y `LoginViewModel` implementados.
  - [ ] Módulo Koin actualizado con los nuevos componentes de Auth.
  - [ ] Tests en verde (`gradlew test`).
  - *Archivos Creados/Modificados*: (Detallar lista de archivos de Dominio, Datos, Presentación y DI).

---

# Spec 003: Tarea 04 - Login Básico por Contraseña y Refactorización de Clases Base

## 1. Stack Tecnológico, Librerías, UI y Reglas de Arquitectura
- **Framework & Multiplatform**: Kotlin Multiplatform (KMP) + Compose Multiplatform.
- **Arquitectura & Escalabilidad**:
  - Clean Architecture modularizada (Presentation, Domain, Data) con principios SOLID.
  - Inyección de dependencias con Koin.
  - Persistencia segura de credenciales (contraseña protegida mediante hashing o almacenamiento cifrado) gestionada en la capa de datos.
- **Testing**: Test-Driven Development (TDD) sobre `commonTest` utilizando `kotlin.test`.
- **UI & Componentes Visuales (Compose Multiplatform)**:
  - Diseño basado en Material 3 (`org.jetbrains.compose.material3`).
  - Campo de contraseña enmascarado (`PasswordVisualTransformation`).
  - Navegación condicional basada en estado reactivo de sesión.

---

## 2. Definición de Tareas de Implementación

### **Tarea 04.A: Enfoque TDD - Fase Roja (Pruebas Unitarias de Autenticación, Casos de Uso y LoginViewModel)**
- **Objetivo**: Escribir las pruebas unitarias en `commonTest` para la lógica de autenticación, validando el comportamiento de la verificación de contraseña y la sesión persistida antes de escribir el código de producción.

- **Pasos a ejecutar**:
  1. **Creación de Fake Repository para Autenticación (`commonTest/.../data/repository/`)**:
     - Crear `FakeAuthRepository` implementando `IAuthRepository` con simulación en memoria de la contraseña válida y el estado de sesión actual.
  2. **Creación de Pruebas Unitarias para Casos de Uso (`commonTest/.../domain/usecase/`)**:
     - `LoginWithPasswordUseCaseTest.kt`: Validar retorno exitoso con contraseña correcta, fallo con contraseña incorrecta y validación de campo vacío.
     - `CheckAuthStatusUseCaseTest.kt`: Validar la emisión reactiva del flujo `Flow<Boolean>` indicando si existe una sesión activa.
     - `LogoutUseCaseTest.kt`: Validar la limpieza de la sesión en el repositorio.
  3. **Creación de Pruebas Unitarias para ViewModel (`commonTest/.../presentation/viewmodel/`)**:
     - `LoginViewModelTest.kt`: Validar la actualización de `LoginUiState` en respuesta a los eventos del usuario (ingreso de contraseña, click en Ingresar, error de autenticación y transición a éxito).
- **Criterio de Aceptación**: Pruebas unitarias creadas y ejecutadas con fallo inicial esperado (Fase Roja) en `commonTest`.
- **REPORTE OBLIGATORIO DE FINALIZACIÓN**:
  - [ ] `FakeAuthRepository` implementado para testing.
  - [ ] Tests unitarios de Casos de Uso (`LoginWithPasswordUseCaseTest`, `CheckAuthStatusUseCaseTest`, `LogoutUseCaseTest`) escritos.
  - [ ] Test unitario del ViewModel (`LoginViewModelTest`) escrito.
  - [ ] Fallo verificado en la ejecución de pruebas (`gradlew test`).
  - *Archivos Creados/Modificados*: (Detallar lista de Fakes y archivos de tests unitarios creados).

---

### **Tarea 04.B: Implementación de la Capa de Dominio, Datos, Persistencia Cifrada y ViewModel**

- **Objetivo**: Implementar la lógica de negocio, la persistencia local de la sesión y el almacenamiento cifrado/hasheado de la contraseña para proteger las credenciales del sistema, declarando las dependencias necesarias en `libs.versions.toml`.

- **Pasos a ejecutar**:
  1. **Declaración de Dependencias (si aplica)**:
     - Registrar cualquier librería adicional requerida para almacenamiento seguro o cifrado en `gradle/libs.versions.toml` y sincronizar en los archivos `build.gradle.kts` correspondientes.
  2. **Capa de Dominio (`domain/`)**:
     - Crear el modelo de dominio `UserSession` en `domain/model/`.
     - Crear el contrato de interfaz `IAuthRepository` en `domain/repository/` declarando:
       - `suspend fun login(password: String): Result<Boolean>`
       - `fun observeIsLoggedIn(): Flow<Boolean>`
       - `suspend fun logout(): Result<Unit>`
       - `suspend fun saveInitialPassword(password: String): Result<Unit>`
     - Crear los Casos de Uso en `domain/usecase/`:
       - `LoginWithPasswordUseCase`
       - `CheckAuthStatusUseCase`
       - `LogoutUseCase`
       - `SavePasswordUseCase`
  3. **Capa de Datos y Cifrado (`data/`)**:
     - Crear la fuente de datos local `AuthLocalDataSource` integrando un mecanismo de almacenamiento cifrado o hashing seguro (ej. SHA-256 o cifrado nativo) para que la contraseña nunca se guarde en texto plano.
     - Crear `AuthRepositoryImpl` en `data/repository/` implementando `IAuthRepository`.
  4. **Capa de Presentación y Estado (`presentation/`)**:
     - Crear `LoginUiState` en `presentation/state/`:
       - Propiedades: `password: String = ""`, `isLoading: Boolean = false`, `errorMessage: String? = null`, `isAuthenticated: Boolean = false`, `isPasswordVisible: Boolean = false`.
     - Crear `LoginViewModel` en `presentation/viewmodel/` anotado con `@KoinViewModel`:
       - Expone `StateFlow<LoginUiState>` mediante `stateIn`.
       - Métodos: `onPasswordChanged(newPassword: String)`, `onTogglePasswordVisibility()`, `onLoginClicked()`, `onLogoutClicked()`.
  5. **Inyección de Dependencias (`di/AppModule.kt`)**:
     - Registrar `AuthLocalDataSource`, `IAuthRepository` (`AuthRepositoryImpl`), Casos de Uso y `LoginViewModel` en el módulo de Koin.

- **Criterio de Aceptación**:
  - Librerías necesarias declaradas en `libs.versions.toml`.
  - Contraseña protegida mediante hashing o cifrado local.
  - `LoginViewModel` y Casos de Uso compilando y pasando las pruebas unitarias en verde.
  - Inyección de dependencias configurada correctamente en Koin.

- **REPORTE OBLIGATORIO DE FINALIZACIÓN**:
  - [ ] Dependencias registradas en `libs.versions.toml`.
  - [ ] Contrato `IAuthRepository` e implementación `AuthRepositoryImpl` creados.
  - [ ] Casos de Uso de autenticación implementados en la capa de dominio.
  - [ ] `LoginUiState` y `LoginViewModel` implementados.
  - [ ] Módulo Koin actualizado con los nuevos componentes de Auth.
  - [ ] Tests en verde (`gradlew test`).
  - *Archivos Creados/Modificados*: (Detallar lista de archivos de Dominio, Datos, Presentación y DI).

---

# Spec 003: Tarea 04 - Login Básico por Contraseña y Refactorización de Clases Base

## 1. Stack Tecnológico, Librerías, UI y Reglas de Arquitectura
- **Framework & Multiplatform**: Kotlin Multiplatform (KMP) + Compose Multiplatform.
- **Arquitectura & Escalabilidad**:
  - Clean Architecture modularizada (Presentation, Domain, Data) con principios SOLID.
  - Inyección de dependencias con Koin.
  - Persistencia segura de credenciales (contraseña protegida mediante hashing o almacenamiento cifrado) gestionada en la capa de datos.
- **Testing**: Test-Driven Development (TDD) sobre `commonTest` utilizando `kotlin.test`.
- **UI & Componentes Visuales (Compose Multiplatform)**:
  - Diseño basado en Material 3 (`org.jetbrains.compose.material3`).
  - Campo de contraseña enmascarado (`PasswordVisualTransformation`).
  - Navegación condicional basada en estado reactivo de sesión.

---

## 2. Definición de Tareas de Implementación

### **Tarea 04.A: Enfoque TDD - Fase Roja (Pruebas Unitarias de Autenticación, Casos de Uso y LoginViewModel)**
- **Objetivo**: Escribir las pruebas unitarias en `commonTest` para la lógica de autenticación, validando el comportamiento de la verificación de contraseña y la sesión persistida antes de escribir el código de producción.

- **Pasos a ejecutar**:
  1. **Creación de Fake Repository para Autenticación (`commonTest/.../data/repository/`)**:
     - Crear `FakeAuthRepository` implementando `IAuthRepository` con simulación en memoria de la contraseña válida y el estado de sesión actual.
  2. **Creación de Pruebas Unitarias para Casos de Uso (`commonTest/.../domain/usecase/`)**:
     - `LoginWithPasswordUseCaseTest.kt`: Validar retorno exitoso con contraseña correcta, fallo con contraseña incorrecta y validación de campo vacío.
     - `CheckAuthStatusUseCaseTest.kt`: Validar la emisión reactiva del flujo `Flow<Boolean>` indicando si existe una sesión activa.
     - `LogoutUseCaseTest.kt`: Validar la limpieza de la sesión en el repositorio.
  3. **Creación de Pruebas Unitarias para ViewModel (`commonTest/.../presentation/viewmodel/`)**:
     - `LoginViewModelTest.kt`: Validar la actualización de `LoginUiState` en respuesta a los eventos del usuario (ingreso de contraseña, click en Ingresar, error de autenticación y transición a éxito).
- **Criterio de Aceptación**: Pruebas unitarias creadas y ejecutadas con fallo inicial esperado (Fase Roja) en `commonTest`.
- **REPORTE OBLIGATORIO DE FINALIZACIÓN**:
  - [ ] `FakeAuthRepository` implementado para testing.
  - [ ] Tests unitarios de Casos de Uso (`LoginWithPasswordUseCaseTest`, `CheckAuthStatusUseCaseTest`, `LogoutUseCaseTest`) escritos.
  - [ ] Test unitario del ViewModel (`LoginViewModelTest`) escrito.
  - [ ] Fallo verificado en la ejecución de pruebas (`gradlew test`).
  - *Archivos Creados/Modificados*: (Detallar lista de Fakes y archivos de tests unitarios creados).

---

### **Tarea 04.B: Implementación de la Capa de Dominio, Datos, Persistencia Cifrada y ViewModel**

- **Objetivo**: Implementar la lógica de negocio, la persistencia local de la sesión y el almacenamiento cifrado/hasheado de la contraseña para proteger las credenciales del sistema, declarando las dependencias necesarias en `libs.versions.toml`.

- **Pasos a ejecutar**:
  1. **Declaración de Dependencias (si aplica)**:
     - Registrar cualquier librería adicional requerida para almacenamiento seguro o cifrado en `gradle/libs.versions.toml` y sincronizar en los archivos `build.gradle.kts` correspondientes.
  2. **Capa de Dominio (`domain/`)**:
     - Crear el modelo de dominio `UserSession` en `domain/model/`.
     - Crear el contrato de interfaz `IAuthRepository` en `domain/repository/` declarando:
       - `suspend fun login(password: String): Result<Boolean>`
       - `fun observeIsLoggedIn(): Flow<Boolean>`
       - `suspend fun logout(): Result<Unit>`
       - `suspend fun saveInitialPassword(password: String): Result<Unit>`
     - Crear los Casos de Uso en `domain/usecase/`:
       - `LoginWithPasswordUseCase`
       - `CheckAuthStatusUseCase`
       - `LogoutUseCase`
       - `SavePasswordUseCase`
  3. **Capa de Datos y Cifrado (`data/`)**:
     - Crear la fuente de datos local `AuthLocalDataSource` integrando un mecanismo de almacenamiento cifrado o hashing seguro (ej. SHA-256 o cifrado nativo) para que la contraseña nunca se guarde en texto plano.
     - Crear `AuthRepositoryImpl` en `data/repository/` implementando `IAuthRepository`.
  4. **Capa de Presentación y Estado (`presentation/`)**:
     - Crear `LoginUiState` en `presentation/state/`:
       - Propiedades: `password: String = ""`, `isLoading: Boolean = false`, `errorMessage: String? = null`, `isAuthenticated: Boolean = false`, `isPasswordVisible: Boolean = false`.
     - Crear `LoginViewModel` en `presentation/viewmodel/` anotado con `@KoinViewModel`:
       - Expone `StateFlow<LoginUiState>` mediante `stateIn`.
       - Métodos: `onPasswordChanged(newPassword: String)`, `onTogglePasswordVisibility()`, `onLoginClicked()`, `onLogoutClicked()`.
  5. **Inyección de Dependencias (`di/AppModule.kt`)**:
     - Registrar `AuthLocalDataSource`, `IAuthRepository` (`AuthRepositoryImpl`), Casos de Uso y `LoginViewModel` en el módulo de Koin.

- **Criterio de Aceptación**:
  - Librerías necesarias declaradas en `libs.versions.toml`.
  - Contraseña protegida mediante hashing o cifrado local.
  - `LoginViewModel` y Casos de Uso compilando y pasando las pruebas unitarias en verde.
  - Inyección de dependencias configurada correctamente en Koin.

- **REPORTE OBLIGATORIO DE FINALIZACIÓN**:
  - [ ] Dependencias registradas en `libs.versions.toml`.
  - [ ] Contrato `IAuthRepository` e implementación `AuthRepositoryImpl` creados.
  - [ ] Casos de Uso de autenticación implementados en la capa de dominio.
  - [ ] `LoginUiState` y `LoginViewModel` implementados.
  - [ ] Módulo Koin actualizado con los nuevos componentes de Auth.
  - [ ] Tests en verde (`gradlew test`).
  - *Archivos Creados/Modificados*: (Detallar lista de archivos de Dominio, Datos, Presentación y DI).

---

Tarea 04 - Login Básico por Contraseña y Refactorización de Clases Base

## 1. Stack Tecnológico, Librerías, UI y Reglas de Arquitectura
- **Framework & Multiplatform**: Kotlin Multiplatform (KMP) + Compose Multiplatform.
- **Arquitectura & Escalabilidad**:
  - Clean Architecture modularizada (Presentation, Domain, Data) con principios SOLID.
  - Inyección de dependencias con Koin.
  - Persistencia segura de credenciales (contraseña protegida mediante hashing o almacenamiento cifrado) gestionada en la capa de datos.
- **Testing**: Test-Driven Development (TDD) sobre `commonTest` utilizando `kotlin.test`.
- **UI & Componentes Visuales (Compose Multiplatform)**:
  - Diseño basado en Material 3 (`org.jetbrains.compose.material3`).
  - Campo de contraseña enmascarado (`PasswordVisualTransformation`).
  - Navegación condicional basada en estado reactivo de sesión.

---

## 2. Definición de Tareas de Implementación

### **Tarea 04.A: Enfoque TDD - Fase Roja (Pruebas Unitarias de Autenticación, Casos de Uso y LoginViewModel)**
- **Objetivo**: Escribir las pruebas unitarias en `commonTest` para la lógica de autenticación, validando el comportamiento de la verificación de contraseña y la sesión persistida antes de escribir el código de producción.

- **Pasos a ejecutar**:
  1. **Creación de Fake Repository para Autenticación (`commonTest/.../data/repository/`)**:
     - Crear `FakeAuthRepository` implementando `IAuthRepository` con simulación en memoria de la contraseña válida y el estado de sesión actual.
  2. **Creación de Pruebas Unitarias para Casos de Uso (`commonTest/.../domain/usecase/`)**:
     - `LoginWithPasswordUseCaseTest.kt`: Validar retorno exitoso con contraseña correcta, fallo con contraseña incorrecta y validación de campo vacío.
     - `CheckAuthStatusUseCaseTest.kt`: Validar la emisión reactiva del flujo `Flow<Boolean>` indicando si existe una sesión activa.
     - `LogoutUseCaseTest.kt`: Validar la limpieza de la sesión en el repositorio.
  3. **Creación de Pruebas Unitarias para ViewModel (`commonTest/.../presentation/viewmodel/`)**:
     - `LoginViewModelTest.kt`: Validar la actualización de `LoginUiState` en respuesta a los eventos del usuario (ingreso de contraseña, click en Ingresar, error de autenticación y transición a éxito).
- **Criterio de Aceptación**: Pruebas unitarias creadas y ejecutadas con fallo inicial esperado (Fase Roja) en `commonTest`.
- **REPORTE OBLIGATORIO DE FINALIZACIÓN**:
  - [ ] `FakeAuthRepository` implementado para testing.
  - [ ] Tests unitarios de Casos de Uso (`LoginWithPasswordUseCaseTest`, `CheckAuthStatusUseCaseTest`, `LogoutUseCaseTest`) escritos.
  - [ ] Test unitario del ViewModel (`LoginViewModelTest`) escrito.
  - [ ] Fallo verificado en la ejecución de pruebas (`gradlew test`).
  - *Archivos Creados/Modificados*: (Detallar lista de Fakes y archivos de tests unitarios creados).

---

### **Tarea 04.B: Implementación de la Capa de Dominio, Datos, Persistencia Cifrada y ViewModel**

- **Objetivo**: Implementar la lógica de negocio, la persistencia local de la sesión y el almacenamiento cifrado/hasheado de la contraseña para proteger las credenciales del sistema, declarando las dependencias necesarias en `libs.versions.toml`.

- **Pasos a ejecutar**:
  1. **Declaración de Dependencias (si aplica)**:
     - Registrar cualquier librería adicional requerida para almacenamiento seguro o cifrado en `gradle/libs.versions.toml` y sincronizar en los archivos `build.gradle.kts` correspondientes.
  2. **Capa de Dominio (`domain/`)**:
     - Crear el modelo de dominio `UserSession` en `domain/model/`.
     - Crear el contrato de interfaz `IAuthRepository` en `domain/repository/` declarando:
       - `suspend fun login(password: String): Result<Boolean>`
       - `fun observeIsLoggedIn(): Flow<Boolean>`
       - `suspend fun logout(): Result<Unit>`
       - `suspend fun saveInitialPassword(password: String): Result<Unit>`
     - Crear los Casos de Uso en `domain/usecase/`:
       - `LoginWithPasswordUseCase`
       - `CheckAuthStatusUseCase`
       - `LogoutUseCase`
       - `SavePasswordUseCase`
  3. **Capa de Datos y Cifrado (`data/`)**:
     - Crear la fuente de datos local `AuthLocalDataSource` integrando un mecanismo de almacenamiento cifrado o hashing seguro (ej. SHA-256 o cifrado nativo) para que la contraseña nunca se guarde en texto plano.
     - Crear `AuthRepositoryImpl` en `data/repository/` implementando `IAuthRepository`.
  4. **Capa de Presentación y Estado (`presentation/`)**:
     - Crear `LoginUiState` en `presentation/state/`:
       - Propiedades: `password: String = ""`, `isLoading: Boolean = false`, `errorMessage: String? = null`, `isAuthenticated: Boolean = false`, `isPasswordVisible: Boolean = false`.
     - Crear `LoginViewModel` en `presentation/viewmodel/` anotado con `@KoinViewModel`:
       - Expone `StateFlow<LoginUiState>` mediante `stateIn`.
       - Métodos: `onPasswordChanged(newPassword: String)`, `onTogglePasswordVisibility()`, `onLoginClicked()`, `onLogoutClicked()`.
  5. **Inyección de Dependencias (`di/AppModule.kt`)**:
     - Registrar `AuthLocalDataSource`, `IAuthRepository` (`AuthRepositoryImpl`), Casos de Uso y `LoginViewModel` en el módulo de Koin.

- **Criterio de Aceptación**:
  - Librerías necesarias declaradas en `libs.versions.toml`.
  - Contraseña protegida mediante hashing o cifrado local.
  - `LoginViewModel` y Casos de Uso compilando y pasando las pruebas unitarias en verde.
  - Inyección de dependencias configurada correctamente en Koin.

- **REPORTE OBLIGATORIO DE FINALIZACIÓN**:
  - [ ] Dependencias registradas en `libs.versions.toml`.
  - [ ] Contrato `IAuthRepository` e implementación `AuthRepositoryImpl` creados.
  - [ ] Casos de Uso de autenticación implementados en la capa de dominio.
  - [ ] `LoginUiState` y `LoginViewModel` implementados.
  - [ ] Módulo Koin actualizado con los nuevos componentes de Auth.
  - [ ] Tests en verde (`gradlew test`).
  - *Archivos Creados/Modificados*: (Detallar lista de archivos de Dominio, Datos, Presentación y DI).

---

Tarea 05 - Login Básico por Contraseña y Refactorización de Clases Base

## 1. Stack Tecnológico, Librerías, UI y Reglas de Arquitectura
- **Framework & Multiplatform**: Kotlin Multiplatform (KMP) + Compose Multiplatform.
- **Arquitectura & Escalabilidad**:
  - Clean Architecture modularizada (Presentation, Domain, Data) con principios SOLID.
  - Inyección de dependencias con Koin.
  - Persistencia segura de credenciales (contraseña protegida mediante hashing o almacenamiento cifrado) gestionada en la capa de datos.
- **Testing**: Test-Driven Development (TDD) sobre `commonTest` utilizando `kotlin.test`.
- **UI & Componentes Visuales (Compose Multiplatform)**:
  - Diseño basado en Material 3 (`org.jetbrains.compose.material3`).
  - Campo de contraseña enmascarado (`PasswordVisualTransformation`).
  - Navegación condicional basada en estado reactivo de sesión.

---

## 2. Definición de Tareas de Implementación

### **Tarea 05.A: Enfoque TDD - Fase Roja (Pruebas Unitarias de Autenticación, Casos de Uso y LoginViewModel)**
- **Objetivo**: Escribir las pruebas unitarias en `commonTest` para la lógica de autenticación, validando el comportamiento de la verificación de contraseña y la sesión persistida antes de escribir el código de producción.

- **Pasos a ejecutar**:
  1. **Creación de Fake Repository para Autenticación (`commonTest/.../data/repository/`)**:
     - Crear `FakeAuthRepository` implementando `IAuthRepository` con simulación en memoria de la contraseña válida y el estado de sesión actual.
  2. **Creación de Pruebas Unitarias para Casos de Uso (`commonTest/.../domain/usecase/`)**:
     - `LoginWithPasswordUseCaseTest.kt`: Validar retorno exitoso con contraseña correcta, fallo con contraseña incorrecta y validación de campo vacío.
     - `CheckAuthStatusUseCaseTest.kt`: Validar la emisión reactiva del flujo `Flow<Boolean>` indicando si existe una sesión activa.
     - `LogoutUseCaseTest.kt`: Validar la limpieza de la sesión en el repositorio.
  3. **Creación de Pruebas Unitarias para ViewModel (`commonTest/.../presentation/viewmodel/`)**:
     - `LoginViewModelTest.kt`: Validar la actualización de `LoginUiState` en respuesta a los eventos del usuario (ingreso de contraseña, click en Ingresar, error de autenticación y transición a éxito).
- **Criterio de Aceptación**: Pruebas unitarias creadas y ejecutadas con fallo inicial esperado (Fase Roja) en `commonTest`.
- **REPORTE OBLIGATORIO DE FINALIZACIÓN**:
  - [ ] `FakeAuthRepository` implementado para testing.
  - [ ] Tests unitarios de Casos de Uso (`LoginWithPasswordUseCaseTest`, `CheckAuthStatusUseCaseTest`, `LogoutUseCaseTest`) escritos.
  - [ ] Test unitario del ViewModel (`LoginViewModelTest`) escrito.
  - [ ] Fallo verificado en la ejecución de pruebas (`gradlew test`).
  - *Archivos Creados/Modificados*: (Detallar lista de Fakes y archivos de tests unitarios creados).
  
  ### **Tarea 05.B: Implementación de la Capa de Dominio, Datos, Persistencia Cifrada y ViewModel**

- **Objetivo**: Implementar la lógica de negocio, la persistencia local de la sesión y el almacenamiento cifrado/hasheado de la contraseña para proteger las credenciales del sistema, declarando las dependencias necesarias en `libs.versions.toml`.

- **Pasos a ejecutar**:
  1. **Declaración de Dependencias (si aplica)**:
     - Registrar cualquier librería adicional requerida para almacenamiento seguro o cifrado en `gradle/libs.versions.toml` y sincronizar en los archivos `build.gradle.kts` correspondientes.
  2. **Capa de Dominio (`domain/`)**:
     - Crear el modelo de dominio `UserSession` en `domain/model/`.
     - Crear el contrato de interfaz `IAuthRepository` en `domain/repository/` declarando:
       - `suspend fun login(password: String): Result<Boolean>`
       - `fun observeIsLoggedIn(): Flow<Boolean>`
       - `suspend fun logout(): Result<Unit>`
       - `suspend fun saveInitialPassword(password: String): Result<Unit>`
     - Crear los Casos de Uso en `domain/usecase/`:
       - `LoginWithPasswordUseCase`
       - `CheckAuthStatusUseCase`
       - `LogoutUseCase`
       - `SavePasswordUseCase`
  3. **Capa de Datos y Cifrado (`data/`)**:
     - Crear la fuente de datos local `AuthLocalDataSource` integrando un mecanismo de almacenamiento cifrado o hashing seguro (ej. SHA-256 o cifrado nativo) para que la contraseña nunca se guarde en texto plano.
     - Crear `AuthRepositoryImpl` en `data/repository/` implementando `IAuthRepository`.
  4. **Capa de Presentación y Estado (`presentation/`)**:
     - Crear `LoginUiState` en `presentation/state/`:
       - Propiedades: `password: String = ""`, `isLoading: Boolean = false`, `errorMessage: String? = null`, `isAuthenticated: Boolean = false`, `isPasswordVisible: Boolean = false`.
     - Crear `LoginViewModel` en `presentation/viewmodel/` anotado con `@KoinViewModel`:
       - Expone `StateFlow<LoginUiState>` mediante `stateIn`.
       - Métodos: `onPasswordChanged(newPassword: String)`, `onTogglePasswordVisibility()`, `onLoginClicked()`, `onLogoutClicked()`.
  5. **Inyección de Dependencias (`di/AppModule.kt`)**:
     - Registrar `AuthLocalDataSource`, `IAuthRepository` (`AuthRepositoryImpl`), Casos de Uso y `LoginViewModel` en el módulo de Koin.

- **Criterio de Aceptación**:
  - Librerías necesarias declaradas en `libs.versions.toml`.
  - Contraseña protegida mediante hashing o cifrado local.
  - `LoginViewModel` y Casos de Uso compilando y pasando las pruebas unitarias en verde.
  - Inyección de dependencias configurada correctamente en Koin.

- **REPORTE OBLIGATORIO DE FINALIZACIÓN**:
  - [ ] Dependencias registradas en `libs.versions.toml`.
  - [ ] Contrato `IAuthRepository` e implementación `AuthRepositoryImpl` creados.
  - [ ] Casos de Uso de autenticación implementados en la capa de dominio.
  - [ ] `LoginUiState` y `LoginViewModel` implementados.
  - [ ] Módulo Koin actualizado con los nuevos componentes de Auth.
  - [ ] Tests en verde (`gradlew test`).
  - *Archivos Creados/Modificados*: (Detallar lista de archivos de Dominio, Datos, Presentación y DI).

### **Tarea 05.C: Refactorización de Clases Base de Navegación, Pantalla de Login e Integración**

- **Objetivo**: Crear la interfaz visual de `LoginScreen`, refactorizar las clases base de la aplicación (`App`, `NavigationGraph` / `MainContainer`) para interceptar la sesión y controlar el flujo de navegación entre la pantalla de Login y las pantallas principales.

- **Pasos a ejecutar**:
  1. **Creación de la Pantalla Visual (`LoginScreen.kt`)**:
     - Maquetar `LoginScreen` en `presentation/screen/`:
       - Título principal y subtítulo (*"Store Cost - Acceso al Sistema"*).
       - Campo de texto para contraseña (`OutlinedTextField` con `PasswordVisualTransformation` y botón para alternar visibilidad).
       - Botón principal **"INGRESAR"** con indicador de carga (`CircularProgressIndicator`).
       - Mensajes de error en `Text` con color `MaterialTheme.colorScheme.error`.
  2. **Refactorización de Clases Base y Navegación (`App.kt`, `MainContent.kt`)**:
     - Refactorizar el componente base `App` o la navegación raíz:
       - Observar el estado reactivo de sesión (`CheckAuthStatusUseCase` / `LoginUiState.isAuthenticated`).
       - Si no está autenticado: Renderizar exclusivamente `LoginScreen`.
       - Si está autenticado: Renderizar el contenedor principal con el menú de navegación (Gestión de Precios, Ventas y Cierre).
  3. **Integración de Cierre de Sesión**:
     - Agregar la opción o botón de **"Cerrar Sesión"** en la barra superior (`TopAppBar`) o en el Drawer para permitir volver a la pantalla de Login limpiando la sesión.

- **Criterio de Aceptación**:
  - Al iniciar la app sin sesión activa, se presenta obligatoriamente la `LoginScreen`.
  - Al ingresar la contraseña correcta, el sistema navega automáticamente a la pantalla principal.
  - Las clases base de la app no rompen la navegación existente de Gestión de Precios ni de Ventas y Cierre.
  - Compilación exitosa e integración probada.

- **REPORTE OBLIGATORIO DE FINALIZACIÓN**:
  - [ ] Componente `LoginScreen` maquetado con Material 3 y desacoplado del estado.
  - [ ] Clases base (`App.kt` / Navegación principal) refactorizadas para soporte de sesión.
  - [ ] Flujo condicional de navegación autenticado/no autenticado validado.
  - [ ] Opción de cierre de sesión agregada y conectada a `LogoutUseCase`.
  - [ ] Pruebas de integración manual y tests unitarios en verde.
  - *Archivos Creados/Modificados*: (Detallar la lista de pantallas, composables base, routers y archivos de configuración ajustados).
  
  # Tarea 06 - Auditoría de Fugas de Memoria, Gestión de Recursos y Componentes Reutilizables (Code Review Checklist)

## Definición de Pasos de Auditoría y Verificación

### 1. Auditoría de Fugas de Memoria (Memory Leaks) y Gestión de Recursos
- **Objetivo**: Garantizar la liberación adecuada de recursos, corrutinas, contextos y listeners para evitar consumo excesivo de RAM o cierres inesperados (OOM).

- **Pasos a ejecutar**:
  - [ ] **Corrutinas y Scopes**:
     - Verificar que ninguna corrutina utilice `GlobalScope`. Asegurar que todas las corrutinas se ejecuten dentro de `viewModelScope` o `coroutineScope` delimitados por ciclo de vida.
  - [ ] **Consumo de Estados en UI (Compose)**:
     - Validar que en Compose la recolección de `StateFlow` o `Flow` se realice mediante `collectAsStateWithLifecycle()` para pausar/cancelar la suscripción al destruir la vista.
  - [ ] **Contextos y Referencias Estáticas**:
     - Asegurar que ningún singleton, clase estática o repositorio conserve referencias directas a componentes con ciclo de vida corto (ej. `Context` de una Activity, `View` o callbacks visuales).
  - [ ] **Listeners, Observers y Dispatchers**:
     - Comprobar que los listeners, observadores o suscripciones abiertas (como llamadas a APIs de ubicación, sensores o conectores) se cancelen/desregistren correctamente mediante `DisposableEffect` en Compose o al destruirse el `ViewModel` (`onCleared`).

### 2. Detección y Extracción de Componentes y Código Duplicado (DRY)
- **Objetivo**: Identificar elementos repetidos en la interfaz de usuario y lógica de negocio para refactorizarlos en componentes reutilizables o funciones genéricas.

- **Pasos a ejecutar**:
  - [ ] **Componentes de UI Repetitivos (Ej. OutlinedTextField)**:
     - Revisar si existen elementos visuales duplicados entre pantallas (por ejemplo, `OutlinedTextField` con estilos, validaciones o comportamientos similares en gestión de precios y ventas).
     - Si es así, crear un componente reutilizable genérico y reemplazar las instancias duplicadas en las pantallas correspondientes.
  - [ ] **Funciones y Métodos Duplicados**:
     - Auditar clases, ViewModels y utilidades en búsqueda de lógica, validaciones o transformaciones de datos repetidas.
     - Centralizar dichos métodos en funciones de extensión o utilidades compartidas para mantener el código limpio y evitar la duplicación (principio DRY).
	 
# Tarea 07 - Implementación de Tareas Periódicas y Políticas Diferenciadas de Limpieza de Base de Datos (Room, Permisos y WorkManager)

## Objetivo
Configurar e implementar tareas periódicas en segundo plano utilizando WorkManager para gestionar políticas diferenciadas de retención de datos según su tipo y pantalla de origen:
1. **Gestión de Precios**: Limpieza diaria de registros/precios temporales.
2. **Ventas Diarias y Cierre**: Generación de reporte quincenal, exportación de respaldos, notificación al usuario y posterior purga de registros mayores a 15 días.
3. **Validación de Permisos**: Garantizar la verificación de permisos en tiempo de ejecución (ej. notificaciones) previo al envío de alertas.

---

## Arquitectura y Distribución de Responsabilidades (Clean Architecture - KMP)
* **Módulo `shared`**: Define las consultas de borrado específicas por entidad/tabla en Room, expone los métodos en repositorios y contiene los Casos de Uso con los períodos de corte correspondientes (diario vs. quincenal).
* **Módulo `androidApp` (Nativo)**: Implementa los `Worker` de WorkManager (o la orquestación periódica), gestiona la validación de permisos en tiempo de ejecución y programa los intervalos de ejecución.

---

## Pasos a Ejecutar

### 1. Capa de Datos y Negocio (`shared`)
- [ ] **Definición de Consultas de Borrado Diferenciadas (Room DAO)**:
  - Crear consulta para eliminar registros de la pantalla de **Gestión de Precios** con antigüedad diaria (`DELETE FROM precios_table WHERE timestamp < :limiteDiario`).
  - Crear consulta para eliminar registros de **Ventas Diarias y Cierre** con antigüedad superior a 15 días (`DELETE FROM ventas_cierre_table WHERE timestamp < :limiteQuincenal`).
- [ ] **Actualización de Repositorios**: Exponer ambas operaciones de borrado independiente a la capa de dominio.
- [ ] **Creación de Casos de Uso**:
  - `CleanDailyPricesUseCase`: Calcula el límite de 24 horas y ejecuta el borrado de precios.
  - `CleanBiweeklySalesUseCase`: Calcula el límite de 15 días y borra las ventas/cierres procesados.

### 2. Validación y Gestión de Permisos (`androidApp`)
- [ ] **Verificación de Permisos**: Comprobar el estado actual de los permisos del sistema (por ejemplo, `POST_NOTIFICATIONS` en Android 13+).
- [ ] **Manejo Seguro**: Garantizar que las notificaciones solo se lancen si los permisos están concedidos, evitando fallos en tiempo de ejecución.

### 3. Refactorización y Programación de Tareas en Segundo Plano (`androidApp`)
- [ ] **Estrategia de Ejecución y Tareas Periódicas**:
  - **Proceso Diario (Gestión de Precios)**: Ejecutar la purga de precios temporales/diarios.
  - **Proceso Quincenal (Ventas y Cierre)**:
    1. Validar permisos de notificación.
    2. Generar el reporte/respaldo quincenal con los datos de ventas y cierres.
    3. Guardar el archivo físico en el dispositivo.
    4. Notificar al usuario que el reporte está listo para ser enviado por correo electrónico.
    5. Invocar el Caso de Uso `CleanBiweeklySalesUseCase` para purgar de Room las ventas y cierres de más de 15 días.
- [ ] **Configuración en WorkManager**: Programar las solicitudes periódicas con sus respectivos intervalos (diario y cada 15 días) y restricciones de sistema.

### 4. Verificación y Pruebas
- [ ] Validar que los datos de **Gestión de Precios** se limpien al día sin afectar el historial de **Ventas y Cierre**.
- [ ] Verificar que el reporte quincenal incluya todo el historial completo de ventas antes de que se ejecute la limpieza de los 15 días.
- [ ] Confirmar la recepción de notificaciones y la retención correcta en la base de datos Room.

# Tarea 08 - Implementación de Ofuscación y Hardening de Seguridad (R8 y ProGuard)

## Objetivo
Activar y configurar la ofuscación de código y la compresión de recursos mediante **R8** en el módulo `androidApp` para proteger la lógica de negocio, minimizar el tamaño del APK y dificultar la ingeniería inversa (descompilación de constantes de texto plano y nombres de clases/repositorios).

---

## Arquitectura y Distribución de Responsabilidades
- **Módulo `shared`**: Mantiene la lógica de negocio y los modelos limpios, cuyas estructuras deben preservarse para evitar fallos por reflexión.
- **Módulo `androidApp` (Nativo)**: Configura las reglas de compilación `release`, habilita R8 en `build.gradle.kts` e incorpora el archivo de exclusiones `proguard-rules.pro` para proteger librerías críticas de persistencia e inyección de dependencias.

---

## Pasos a Ejecutar

### 1. Configuración del Módulo Android (`androidApp`)
- [ ] **Habilitar R8 en Builds de Release**:
  - Modificar el archivo `androidApp/build.gradle.kts` dentro del bloque `buildTypes` para la variante `release`:
    ```kotlin
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    ```

### 2. Definición de Reglas de Ofuscación (`proguard-rules.pro`)
- [ ] **Crear o editar `androidApp/proguard-rules.pro`**:
  - Agregar las exclusiones necesarias para evitar que la ofuscación rompa frameworks externos basados en reflexión:
    ```pro
    # --- Koin (Inyección de dependencias por anotaciones) ---
    -keep class com.tickets.store_cost.** { *; }
    -dontwarn org.koin.**

    # --- Room (Base de datos local y DAOs) ---
    -keep class com.tickets.store_cost.data.local.** { *; }
    -keepattributes *Annotation*,Signature,EnclosingMethod

    # --- Modelos de Dominio y Datos ---
    -keep class com.tickets.store_cost.domain.model.** { *; }
    -keep class com.tickets.store_cost.data.model.** { *; }

    # --- Seguridad / Criptografía (SHA-256 / Java Security) ---
    -dontwarn java.security.**
    -keepclassmembers class * {
        @javax.inject.Inject <init>();
    }
    ```

### 3. Verificación y Compilación
- [ ] **Ejecución y Pruebas en Modo Release**:
  - Compilar el proyecto ejecutando el comando de Gradle:
    ```bash
    ./gradlew assembleRelease
    ```
  - Validar que la compilación finalice con éxito (`BUILD SUCCESSFUL`).
  - Instalar el APK de release resultante en un dispositivo físico para confirmar que 
  el login, el almacenamiento y las consultas locales operan correctamente sin errores de ejecución.