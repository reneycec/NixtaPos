# Plan de Verificación Final - Nixta POS v2

Este documento detalla los pasos para validar que la integración de arquitectura profesional y sincronización cumple con los requerimientos.

## 1. Verificación de Infraestructura (Hilt & DataStore)
- [ ] **Hilt Setup**: Verificar que la aplicación compila sin errores tras la adición de `NixtaApp`, `DatabaseModule`, `NetworkModule` y `DataStoreModule`.
- [ ] **Persistencia DataStore**: Validar que la URL, Tenant y Sucursal se guardan y recuperan correctamente entre reinicios de la app.

## 2. Flujo de Configuración Inicial (SetupScreen)
- [ ] **Paso 1 (Conexión)**: Ingresar una URL y verificar que el `testConnectionAndFetchTenants` dispara la carga de datos (vía Mock).
- [ ] **Paso 2 (Identificación)**: Verificar que al seleccionar un Tenant se cargan las sucursales y, posteriormente, los usuarios.
- [ ] **Paso 3 (Seguridad)**: Probar el ingreso de PIN con el teclado numérico personalizado y validar el acceso.

## 3. Sincronización Automática (WorkManager)
- [ ] **Sync Inmediato**: Confirmar que al iniciar sesión se dispara la tarea de sincronización.
- [ ] **Sync en Segundo Plano**: Verificar que el `SyncWorker` se programa correctamente cada 15 minutos.

## 4. UI Punto de Venta
- [ ] **Teclado Numérico**: Realizar una venta de prueba y validar que el teclado en el modal de cobro captura montos correctamente y permite decimales (máx 2).

## 5. Contrato Backend
- [ ] **Documentación**: Validar que el archivo `REQUERIMIENTOS_INTEGRACION_LARAVEL.txt` en la raíz contiene todas las directivas acordadas.
