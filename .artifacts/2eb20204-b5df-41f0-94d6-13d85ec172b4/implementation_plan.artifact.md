# Plan de Implementación: Módulo de Gestión de Comunidad

Este plan detalla la refactorización y mejora del módulo de gestión de comunidad (anteriormente `ApiTester`) para proporcionar una experiencia de usuario completa con operaciones CRUD (Crear, Leer, Actualizar, Borrar) sobre los miembros de la comunidad.

## Cambios Propuestos

### 1. Modelado y Lógica de Datos

#### [MODIFY] [UserRepository.kt](file:///C:/Users/estiv/OneDrive/Desktop/Cinemacth/app/src/main/java/com/example/cinemacth/data/repository/UserRepository.kt)
*   Asegurar que los métodos de repositorio sean robustos para las operaciones CRUD. (Ya parece estar bien, pero se revisará).

### 2. Capa de Presentación (ViewModel)

#### [NEW] [CommunityViewModel.kt](file:///C:/Users/estiv/OneDrive/Desktop/Cinemacth/app/src/main/java/com/example/cinemacth/ui/viewmodel/CommunityViewModel.kt)
*   Renombrar y refactorizar `ApiTesterViewModel`.
*   Simplificar la gestión de estados para centrarse en los datos del usuario.
*   Proporcionar métodos claros para `loadUsers`, `addUser`, `updateUser` y `deleteUser`.

#### [DELETE] [ApiTesterViewModel.kt](file:///C:/Users/estiv/OneDrive/Desktop/Cinemacth/app/src/main/java/com/example/cinemacth/ui/viewmodel/ApiTesterViewModel.kt)
*   Eliminar el archivo antiguo una vez migrado.

### 3. Interfaz de Usuario (Compose)

#### [NEW] [CommunityScreen.kt](file:///C:/Users/estiv/OneDrive/Desktop/Cinemacth/app/src/main/java/com/example/cinemacth/ui/screens/CommunityScreen.kt)
*   Crear una interfaz moderna basada en una lista de usuarios.
*   **Lista de Usuarios**: Cada elemento mostrará el nombre e email con botones de acción (Editar/Borrar).
*   **Botón Flotante (FAB)**: Para abrir un diálogo de "Nuevo Usuario".
*   **Diálogos**: Implementar diálogos modales para la creación y edición de usuarios, evitando el uso de pestañas complicadas.
*   **Feedback**: Mostrar indicadores de carga y mensajes de éxito/error.

#### [DELETE] [ApiTesterScreen.kt](file:///C:/Users/estiv/OneDrive/Desktop/Cinemacth/app/src/main/java/com/example/cinemacth/ui/screens/ApiTesterScreen.kt)
*   Eliminar el archivo antiguo una vez migrado.

### 4. Navegación y App

#### [MODIFY] [MainActivity.kt](file:///C:/Users/estiv/OneDrive/Desktop/Cinemacth/app/src/main/java/com/example/cinemacth/MainActivity.kt)
*   Actualizar la ruta `Screen.Users` para usar `CommunityScreen` y `CommunityViewModel`.

## Plan de Verificación

### Pruebas Manuales
1.  **Listar**: Abrir la pantalla de Usuarios y verificar que se carguen los miembros.
2.  **Registrar**: Hacer clic en el FAB, completar el formulario y verificar que el nuevo usuario aparezca en la lista (o se muestre mensaje de éxito).
3.  **Editar**: Hacer clic en el icono de editar de un usuario, cambiar sus datos y guardar.
4.  **Borrar**: Hacer clic en el icono de borrar y confirmar la acción.
