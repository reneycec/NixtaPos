# Implementation Plan - Corrección de Flujo de Login y Eliminación de PIN de 4 Dígitos

Este plan corrige la omisión de la pantalla de Login al inicio de la aplicación y elimina definitivamente las restricciones de PIN de 4 dígitos en favor de contraseñas alfanuméricas de longitud flexible, cumpliendo con el requerimiento de control total Offline-First.

## User Review Required

> [!IMPORTANT]
> **Cambio de UI:** Los diálogos de "PIN Pad" (teclado numérico) para desbloqueo de terminal y autorización de supervisor serán reemplazados por campos de texto de contraseña estándar. Esto permite contraseñas seguras y elimina la confusión de la limitación de 4 dígitos.
>
> **Flujo de Inicio:** Al abrir la app, si no hay una sesión activa, se mostrará forzosamente la `LoginScreen`.

## Proposed Changes

### [Component] Datos y Persistencia

#### [MODIFY] [PosDao.kt](file:///F:/Proyectos/Nixta/nixta-pos%20v2/app/src/main/java/com/example/data/local/dao/PosDao.kt)
- Agregar método `getUsuarioById(id: String): Flow<UsuarioEntity?>` para recuperación reactiva del usuario logueado.

#### [MODIFY] [MainViewModel.kt](file:///F:/Proyectos/Nixta/nixta-pos%20v2/app/src/main/java/com/example/ui/MainViewModel.kt)
- Eliminar el usuario hardcodeado.
- Implementar `currentUsuario` como un `StateFlow` que observa el `currentUserId` de `TerminalConfigManager`.
- Agregar función `logout()` que limpie el ID de usuario en `TerminalConfigManager`.
- Refactorizar lógica de validación de "PIN" para que acepte contraseñas de cualquier longitud y requiera confirmación manual (botón "Aceptar") en lugar de auto-envío a los 4 dígitos.

---

### [Component] Interfaz de Usuario (UI)

#### [MODIFY] [MainScreen.kt](file:///F:/Proyectos/Nixta/nixta-pos%20v2/app/src/main/java/com/example/ui/MainScreen.kt)
- Integrar `LoginScreen`. Si `currentUsuario` es nulo, la pantalla principal mostrará el login.
- Inyectar o proveer el `LoginViewModel` necesario para la `LoginScreen`.

#### [MODIFY] [PinKeypadDialog.kt](file:///F:/Proyectos/Nixta/nixta-pos%20v2/app/src/main/java/com/example/ui/components/PinKeypadDialog.kt)
- Reemplazar el teclado numérico visual por un `OutlinedTextField` con `PasswordVisualTransformation`.
- Cambiar textos informativos de "PIN" a "Contraseña".

#### [MODIFY] [SupervisorOverrideDialog.kt](file:///F:/Proyectos/Nixta/nixta-pos%20v2/app/src/main/java/com/example/ui/components/SupervisorOverrideDialog.kt)
- Reemplazar el teclado numérico por un campo de texto para contraseña.
- Actualizar lógica para procesar la entrada completa al presionar un botón de confirmación.

---

## Verification Plan

### Automated Tests
- Validar reactividad de `currentUsuario` en `MainViewModel` cuando se actualiza el ID en el config manager.

### Manual Verification
1. **Login Inicial:** Abrir la app y verificar que aparece la pantalla de Login (Email/Password).
2. **Login Offline:** Desactivar internet y verificar que permite el acceso con credenciales guardadas localmente.
3. **Bloqueo de Terminal:** Bloquear la terminal y verificar que el nuevo diálogo pide contraseña (no PIN pad).
4. **Cierre de Sesión:** Verificar que al cerrar sesión, la app regresa a la pantalla de Login.
