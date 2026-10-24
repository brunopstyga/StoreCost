# Especificación de Casos de Prueba (TDD) 

Este documento detalla el comportamiento conceptual y los escenarios de validación obligatorios que deben implementarse en la fase roja del ciclo TDD para el módulo de precios, ventas y cierres de jornada.

## 1. Casos de Prueba de Precios y Modalidades de Pago
El objetivo de esta prueba es asegurar que las matemáticas de recargo y ajuste operen de forma determinista y sin desvíos, utilizando un valor base estandarizado de **$1,000.00**.

- **`FACTURA_A`**: Aplica el recargo de IVA del 21% sobre el precio base. El resultado esperado es de **$1,210.00**.
- **`FACTURA_B`**: Aplica el recargo de IVA del 10.5% sobre el precio base. El resultado esperado es de **$1,105.00**.
- **`LOTE_100`**: Aplica un margen de lote del 100% sobre el precio base. El resultado esperado es de **$2,000.00**.
- **`LOTE_110`**: Aplica un margen de lote del 110% sobre el precio base. El resultado esperado es de **$2,100.00**.
- **`EFECTIVO_NEGRO`**: Aplica un ajuste por pago en efectivo del 35% sobre el precio base. El resultado esperado es de **$1,350.00**.

---

## 2. Casos de Prueba de Registro de Ventas
Esta prueba valida la transacción comercial individual y el cálculo del subtotal por ítem.

- **Cálculo de Subtotal**: Se simula la venta de una cantidad determinada de unidades (por ejemplo, 2 unidades) de un producto cuyo precio unitario ya fue calculado bajo una modalidad específica (por ejemplo, `FACTURA_A` a $1,210.00 c/u). 
- **Resultado Esperado**: El sistema debe multiplicar la cantidad por el precio unitario y arrojar un subtotal exacto de **$2,420.00**, previniendo errores de redondeo.

---

## 3. Casos de Prueba de Cierres Temporales (Diario, Semanal y Mensual)
Esta sección valida la capacidad del sistema para agrupar y filtrar registros de ventas en el tiempo utilizando `kotlinx-datetime`.

- **Cierre Diario**: 
  - *Comportamiento*: Se inyecta una lista de ventas asociadas a una fecha puntual. 
  - *Validación*: El sistema debe aislar únicamente los registros de ese día, sumar los montos totales facturados y desglosarlos correctamente por modalidad de operación.
- **Cierre Semanal**: 
  - *Comportamiento*: Se inyecta un set de datos continuo que abarca un período de 7 días consecutivos. 
  - *Validación*: El caso de uso debe ser capaz de barrer todo el rango temporal y consolidar la suma exacta de toda la semana.
- **Cierre Mensual**: 
  - *Comportamiento*: Se inyecta la información acumulada a lo largo de todo el mes calendario. 
  - *Validación*: El reporte global debe reflejar el consolidado exacto sin perder ningún registro intermedio.