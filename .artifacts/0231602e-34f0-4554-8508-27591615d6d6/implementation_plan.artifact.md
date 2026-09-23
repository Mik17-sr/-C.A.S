# Plan de Implementación: Pantallas de Login y Registro

Este plan detalla la creación de las pantallas de inicio de sesión y registro, integrándolas con la base de datos Room existente y configurando la navegación necesaria.

## Cambios Propuestos

### Configuración de Dependencias

Se añadirán las dependencias necesarias para la navegación en Compose y la integración con ViewModel.

#### [MODIFY] [libs.versions.toml](file:///C:/Users/Dylan/Documents/kotlin/-C.A.S/gradle/libs.versions.toml)
Añadir versiones y librerías para `navigation-compose` y `lifecycle-viewmodel-compose`.

#### [MODIFY] [build.gradle.kts](file:///C:/Users/Dylan/Documents/kotlin/-C.A.S/app/build.gradle.kts)
Incluir las nuevas librerías en el proyecto.

---

### Capa de Datos y Lógica (Auth)

Se creará un ViewModel para manejar la lógica de autenticación y se actualizará `CasApplication` para proveer los repositorios de forma sencilla.

#### [MODIFY] [CasApplication.kt](file:///C:/Users/Dylan/Documents/kotlin/-C.A.S/app/src/main/java/com/example/cas/CasApplication.kt)
Instanciar la base de datos y los repositorios para que sean accesibles globalmente.

#### [NEW] [AuthViewModel.kt](file:///C:/Users/Dylan/Documents/kotlin/-C.A.S/app/src/main/java/com/example/cas/ui/auth/AuthViewModel.kt)
Lógica para validar credenciales, registrar usuarios y manejar el estado de la UI.

---

### Interfaz de Usuario (UI)

Creación de las pantallas usando Jetpack Compose y Material 3.

#### [NEW] [LoginScreen.kt](file:///C:/Users/Dylan/Documents/kotlin/-C.A.S/app/src/main/java/com/example/cas/ui/auth/LoginScreen.kt)
Pantalla con campos de Usuario y Contraseña, botón de ingreso y enlace a registro.

#### [NEW] [RegisterScreen.kt](file:///C:/Users/Dylan/Documents/kotlin/-C.A.S/app/src/main/java/com/example/cas/ui/auth/RegisterScreen.kt)
Pantalla con campos para Nombre, Email, Usuario y Contraseña.

---

### Navegación

#### [MODIFY] [MainActivity.kt](file:///C:/Users/Dylan/Documents/kotlin/-C.A.S/app/src/main/java/com/example/cas/MainActivity.kt)
Configurar el `NavHost` para manejar el flujo entre Login, Registro y la pantalla principal (Home).

## Plan de Verificación

### Verificación Manual
1.  **Registro:** Crear un nuevo usuario y verificar que se guarde correctamente.
2.  **Login:** Ingresar con las credenciales creadas y verificar que navegue a la pantalla principal.
3.  **Validación:** Intentar ingresar con datos incorrectos y verificar que se muestre un mensaje de error.
