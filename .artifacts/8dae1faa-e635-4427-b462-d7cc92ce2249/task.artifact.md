# Tareas: Corrección de Login y Eliminación de PIN

- [x] **Fase 1: Datos y Persistencia**
    - [x] Agregar `getUsuarioById` en `PosDao.kt`
    - [x] Refactorizar `MainViewModel.kt`:
        - [x] Carga dinámica de `currentUsuario` desde `TerminalConfigManager`
        - [x] Implementar función `logout()`
        - [x] Cambiar lógica de "PIN" a "Password" en verificaciones de Supervisor y Cajero

- [x] **Fase 2: Integración de Login en UI**
    - [x] Modificar `MainScreen.kt` para mostrar `LoginScreen` si no hay sesión
    - [x] Ajustar navegación inicial

- [x] **Fase 3: Refactorización de Diálogos**
    - [x] Reemplazar teclado numérico en `PinKeypadDialog.kt` por campo de texto
    - [x] Reemplazar teclado numérico en `SupervisorOverrideDialog.kt` por campo de texto

- [x] **Fase 4: Verificación**
    - [x] Probar flujo completo de login y bloqueo
    - [x] Generar walkthrough final
