# KMP Architecture Rules

## Stack Tecnológico
- **Framework & Multiplatform**: Kotlin Multiplatform (KMP) + Compose Multiplatform.
- **Lenguaje**: Kotlin (Strict Null Safety & Immutability by default).
- **Inyección de Dependencias (DI)**: Koin + **Koin Annotations (KSP)**.
- **Base de Datos Local**: Room KMP (`androidx.room`).
- **Arquitectura**: Clean Architecture modularizada (Presentation, Domain, Data).
- **Manejo de Fechas**: `kotlinx-datetime`.
- **Navegación**: Jetpack Navigation Compose (KMP) / Type-safe navigation.
- **Testing**: `kotlin.test`, `kotlinx-coroutines-test`.

## Principios Fundamentales

### 1. Clean Architecture (Estructura por Capas en `commonMain`)
- **Presentation (UI / ViewModels / States / Navigation)**:
  - Componentes visuales construidos con Compose Multiplatform (Material 3).
  - Patrón UDF (Unidirectional Data Flow): pantallas *stateless* que observan estados reactivos (`StateFlow`) emitidos por el ViewModel y envían acciones.
  - Cero lógica de negocio y cero consultas directas a base de datos o repositorios.
  - **Componentes de UI E específicos según Casos de Uso**:
    - **Pantalla de Precios y Registro de Ventas**:
      - Campos de entrada (`OutlinedTextField`) para ingresar el precio base y la cantidad de unidades de forma interactiva.
      - Selectores de modalidad (`SegmentedButton` o menús desplegables) para elegir entre `FACTURA_A`, `FACTURA_B`, `LOTE_100`, `LOTE_110` o `EFECTIVO_NEGRO`.
      - Tarjetas (`Card`) de resultados en tiempo real para visualizar el precio unitario calculado y el subtotal de la transacción.
      - Botón de confirmación para disparar el registro de la venta.
    - **Pantalla de Cierres y Reportes (Diario, Semanal, Mensual)**:
      - **Navegación por Pestañas (`TabRow` / `Tab`)**: Componente superior de Material 3 para alternar de forma interactiva entre las vistas "Diario", "Semanal" y "Mensual".
      - Selectores de rangos de fecha basados en `kotlinx-datetime`.
      - Tarjetas de resumen métrico (`Card`) que desglosan el total facturado global y el desglose detallado por tipo de operación para cada período seleccionado.
      - Listados eficientes y optimizados mediante **`LazyColumn`** para mostrar el historial de transacciones y agrupaciones de ventas.
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
│  Historial de Transacciones:           │
│  ┌──────────────────────────────────┐  │
│  │ 🔄 LazyColumn (Scrollable)       │  │
│  │ • Venta #124 - FACTURA_A         │  │
│  │    2 u. x $1,210.00 = $2,420.00  │  │
│  │
│  │ 
│  │     
│  └──────────────────────────────────┘  │
│                                        │
│                                        │
└────────────────────────────────────────┘

- **Domain (Entidades / Casos de Uso / Contratos)**:
  - Núcleo 100% puro de Kotlin (sin dependencias de frameworks de UI, Android, iOS o Room).
  - Define interfaces de repositorios (`IRepository`) y Casos de Uso que encapsulan la lógica atómica de negocio.
- **Data (Repositorios / Room DAOs / Mappers)**:
  - Implementación concreta de los contratos del Dominio anotada para el escaneo automático.
  - Lógica de persistencia local mediante Room KMP, definición de entidades de base de datos y mapeo bidireccional hacia los modelos de dominio.

### 2. Principios SOLID & Desacoplamiento Estricto
- **SRP (Single Responsibility)**: Cada clase, caso de uso o ViewModel tiene una única razón para cambiar. Los casos de uso ejecutan una sola regla de negocio.
- **OCP (Open/Closed)**: Extensible mediante interfaces en el Dominio, permitiendo agregar nuevas modalidades de pago o reglas de inventario sin alterar el código existente.
- **LSP (Liskov Substitution)**: Las implementaciones concretas de repositorios son intercambiables mediante sus interfaces sin alterar el comportamiento del sistema.
- **ISP (Interface Segregation)**: Interfaces de repositorios pequeñas y específicas, evitando contratos sobredimensionados ("God Interfaces").
- **DIP (Dependency Inversion)**: El Dominio depende exclusivamente de abstracciones. Las capas externas (Data y Presentation) dependen del Dominio, jamás a la inversa.

### 3. Inyección de Dependencias por Constructor (Koin Annotations & KSP)
- **Inyección en Constructores**: Las clases (Casos de Uso, Repositorios, ViewModels) declaran sus dependencias directamente en el constructor principal mediante Kotlin, resolviéndose automáticamente de forma limpia.
- **Anotaciones de Koin**: 
  - `@Single` o `@Factory` para repositorios, fuentes de datos y casos de uso.
  - `@KoinViewModel` para los ViewModels compartidos consumidos desde Compose.
  - `@Module` y `@ComponentScan` para que Koin escanee automáticamente y agrupe los componentes en el grafo.
- **Inicialización por Plataforma**: 
  - La inicialización de Koin se realiza de forma explícita dentro del punto de entrada principal de cada plataforma (como el método `onCreate()` de la clase `Application` en Android) utilizando la función `startKoin` junto con los módulos generados por Koin Annotations.
  - Se pasa la configuración global y el contexto específico de cada plataforma cuando sea requerido (por ejemplo, `androidContext()`).
  
### 4. Escalabilidad & Calidad de Código
- **Inmutabilidad por defecto**: Uso prioritario de `val` y data classes inmutables.
- **Concurrencia Reactiva**: Uso de Kotlin Coroutines y `Flow` / `StateFlow` para flujos de datos asíncronos y reactivos entre capas.
- **Cero código duplicado (DRY)**: Abstracción de lógica repetida en funciones de extensión o utilidades puras compartidas.
- **Manejo Robusto de Errores**: Propagación controlada de excepciones y resultados desde la capa `Data` hasta el `ViewModel` mediante estructuras seguras o contenedores de resultado.