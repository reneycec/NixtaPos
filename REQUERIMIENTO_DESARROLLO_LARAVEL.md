# Requerimiento de Desarrollo: Integración de Puntos, Cierre de Caja y Sincronización Asíncrona (NIXTA POS ↔ Laravel)

Este documento detalla las especificaciones técnicas necesarias para que el equipo de Laravel implemente las funcionalidades de lealtad (puntos), el endpoint dedicado para cierre de caja y la sincronización asíncrona mediante notificaciones push.

---

## 1. Sistema de Puntos (Loyalty)

La aplicación requiere gestionar puntos por cliente. Los puntos se acumulan en el servidor basándose en las ventas recibidas y se consultan desde la APK.

### A. Modificaciones en el Esquema de Datos
Se deben agregar o actualizar los siguientes campos en las tablas correspondientes de Laravel/PostgreSQL:

- **Tabla `clientes`**:
  - `puntos_acumulados` (BigInt/Long): Total de puntos disponibles.
- **Tabla `ventas`**:
  - `puntos_ganados` (BigInt/Long): Puntos generados por esta transacción.
  - `puntos_canjeados` (BigInt/Long): Puntos utilizados como forma de pago en esta transacción.

### B. Nuevos Endpoints / Ajustes
1. **Consulta de Puntos (Real-time)**:
    - **Ruta**: `GET /api/v1/clientes/{uuid}/puntos`
    - **Query Params**: `tenant_id`
    - **Respuesta**: `{"puntos": 1500, "nombre": "Juan Perez"}`
2. **Ajuste en Sincronización Incremental (Pull)**:
    - El endpoint `GET /api/v1/sync/pull` debe incluir el campo `puntos_acumulados` dentro de cada objeto del arreglo `clientes`.

---

## 2. Endpoint Dedicado para Cierre de Caja (Cierre de Turno Móvil)

Para garantizar un registro directo, auditado e inmediato del cierre de caja desde la APK sin colisionar con las rutas del panel web, se especifica el siguiente endpoint dedicado:

### Especificaciones del Endpoint:
- **Ruta**: `POST /api/v1/caja/cierre-movil`
- **Método**: `POST`
- **Headers**:
  - `Content-Type: application/json`
  - `Authorization: Bearer {token}` (opcional/según configuración auth)

### Ejemplo de Payload (JSON enviado por la APK):
```json
{
  "sesion_caja_id": "a58b9f71-290d-40d3-a4f6-8299831f6c91",
  "monto_arqueo": 117400,
  "notas": "Cierre de turno tarde sin novedades en efectivo.",
  "usuario_cierre_id": "3bb3a1c6-2e86-4f4d-a9a7-95155f308a9f",
  "usuario_cierre_nombre": "Carlos Perez",
  "fecha_cierre": 1709245200000,
  "tenant_id": "8c5e065c-6622-4a00-9854-47b794170068",
  "sucursal_id": "f47ac10b-58cc-4372-a567-0e02b2c3d479"
}
```

> **Nota sobre `monto_arqueo`**: El valor se envía en centavos como un entero `Long` (ejemplo: `$1,174.00 MXN` viaja como `117400`). En Laravel se divide entre 100 para almacenar el valor decimal correspondiente.

### Ejemplo de Respuesta Esperada (HTTP 200 OK):
```json
{
  "success": true,
  "message": "Cierre de caja registrado exitosamente",
  "sesion_caja_id": "a58b9f71-290d-40d3-a4f6-8299831f6c91",
  "fecha_cierre": 1709245200000
}
```

---

## 3. Ejemplos de Intercambio de Datos (JSON Sincronización Masiva)

### A. APK → Laravel (Envío de Transacciones - Push Masivo)
Este es el formato que la APK enviará al endpoint `POST /api/v1/sync/push`.

```json
{
  "tenant_id": "8c5e065c-6622-4a00-9854-47b794170068",
  "sucursal_id": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "ventas": [
    {
      "id": "e3b0c442-98fc-11eb-8195-42010a800003",
      "folio": "V-001-000456",
      "cliente_id": "d290f1ee-6c54-4b01-90e6-d701748f0851",
      "fecha": 1709245200000,
      "subtotal": 15000,
      "impuestos_desglosados": 2400,
      "total": 17400,
      "metodo_pago": "EFECTIVO",
      "puntos_ganados": 174,
      "puntos_canjeados": 0,
      "detalles": [
        {
          "producto_id": "7b5871c2-5569-4598-961d-73b37803e04e",
          "cantidad": 2.5,
          "precio_unitario": 5000,
          "subtotal": 12500
        }
      ]
    }
  ],
  "sesiones": [
    {
      "id": "a58b9f71-290d-40d3-a4f6-8299831f6c91",
      "usuario_apertura_id": "3bb3a1c6-2e86-4f4d-a9a7-95155f308a9f",
      "fondo_inicial": 100000,
      "fecha_apertura": 1709230800000,
      "estado": "CERRADA",
      "monto_arqueo": 117400
    }
  ],
  "sesiones": [
    {
      "id": "a58b9f71-290d-40d3-a4f6-8299831f6c91",
      "usuario_apertura_id": "3bb3a1c6-2e86-4f4d-a9a7-95155f308a9f",
      "fondo_inicial": 100000,
      "fecha_apertura": 1709230800000,
      "estado": "CERRADA",
      "monto_arqueo": 117400
    }
  ]
}
```

### B. APK → Laravel (Push Dedicado para Movimientos de Caja)
- **Endpoint**: `POST /api/v1/sync/push`
- **Header**: `x-tipo: movimiento`
- **Payload JSON (enviado inmediatamente al registrar o en sync en segundo plano)**:

```json
{
  "tenant_id": "8c5e065c-6622-4a00-9854-47b794170068",
  "sucursal_id": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "tipo": "movimiento",
  "movimientos": [
    {
      "id": "f81d4fae-7dec-11d0-a765-00a0c91e6bf6",
      "sesion_caja_id": "a58b9f71-290d-40d3-a4f6-8299831f6c91",
      "tipo": "DEPOSITO",
      "medio_pago": "EFECTIVO",
      "monto": 5000,
      "concepto": "chocolates",
      "referencia": "chocolates",
      "usuario_id": "3bb3a1c6-2e86-4f4d-a9a7-95155f308a9f",
      "persona_registra_id": "3bb3a1c6-2e86-4f4d-a9a7-95155f308a9f",
      "fecha": 1709235000000,
      "tenant_id": "8c5e065c-6622-4a00-9854-47b794170068",
      "sucursal_id": "f47ac10b-58cc-4372-a567-0e02b2c3d479"
    }
  ]
}
```

### B. Laravel → APK (Descarga de Catálogos - Pull)
Respuesta del endpoint `GET /api/v1/sync/pull`.

```json
{
  "server_time": 1709245500000,
  "productos": [
    {
      "id": "7b5871c2-5569-4598-961d-73b37803e04e",
      "sku": "PROD-001",
      "nombre": "Tortilla de Maíz kg",
      "precio_con_impuestos": 2200,
      "stock": 150.0,
      "tipo_articulo": "producto_terminado",
      "is_deleted": false
    }
  ],
  "clientes": [
    {
      "id": "d290f1ee-6c54-4b01-90e6-d701748f0851",
      "nombre": "Juan Perez",
      "telefono": "555-0199",
      "puntos_acumulados": 850,
      "condicion_pago": "CONTADO",
      "is_deleted": false
    }
  ]
}
```

---

## 4. Actualizaciones Silenciosas (Asincronía vía FCM)

Para cumplir con el requerimiento de "actualizaciones silenciosas", Laravel debe actuar como emisor de eventos cuando los datos cambien en el panel web.

### Flujo de Trabajo:
1. **Registro**: Al iniciar, la APK registra su `fcm_token` vinculado a un `tenant_id` y `sucursal_id`.
2. **Evento en Laravel**: Al modificar un producto, precio o puntos de un cliente, Laravel dispara un mensaje FCM de tipo **Data Message** (no de notificación visual).
3. **Estructura del Mensaje (Payload)**:
    ```json
    {
      "to": "/topics/sucursal_{sucursal_uuid}",
      "data": {
        "event": "SYNC_REQUIRED",
        "entity": "PRODUCTOS",
        "reference_id": "opcional-uuid-del-cambio",
        "timestamp": 1709245500000
      }
    }
    ```
4. **Acción en APK**: Al recibir este JSON en segundo plano, la APK invocará automáticamente al endpoint de `Pull` para refrescar sus tablas locales sin molestar al usuario.

---

## 5. Consideraciones Técnicas Generales
- **Montos**: Siempre enviar en centavos (Ej: $10.50 -> 1050).
- **Fechas**: Siempre en Unix Epoch Milisegundos (Ej: 1709245500000).
- **UUIDs**: Laravel debe respetar los UUIDs generados por la APK como Primary Keys.
