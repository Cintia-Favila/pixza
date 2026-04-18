# Pixza Backend & Telegram Admin Bot

API RESTful desarrollada en Spring Boot para la gestión de categorías y lugares, integrada nativamente con un bot interactivo de Telegram para la administración remota. El sistema cuenta con autenticación segura vía JWT y está diseñado para despliegue en la nube.

## 🚀 Tecnologías y Arquitectura

* **Lenguaje:** Java 17
* **Framework Core:** Spring Boot 3.3.2
* **Seguridad:** Spring Security con JSON Web Tokens (JWT)
* **Persistencia de Datos:** Spring Data JPA / Hibernate
* **Base de Datos:** PostgreSQL
* **Integración:** TelegramBots API (Long Polling)
* **Infraestructura:** Docker (Desarrollo local) y Render (Producción)

## ⚙️ Características Principales

1.  **Autenticación Segura (JWT):** Generación y validación de tokens para proteger los endpoints de la API y restringir el acceso a los comandos del bot de Telegram.
2.  **Gestión de Lugares y Categorías (CRUD):** Endpoints estructurados para la creación, lectura, actualización y eliminación de registros geográficos y sus categorías asociadas.
3.  **Telegram Admin Bot (Inyección Directa):** Bot de administración que interactúa directamente con los servicios de la aplicación (sin peticiones HTTP internas) para ejecutar operaciones en la base de datos a través de una interfaz de chat.
4.  **Carga de Imágenes:** Capacidad de asociar URLs de imágenes a los lugares registrados.

## 📋 Variables de Entorno

Para ejecutar este proyecto, es necesario configurar las siguientes variables de entorno (ya sea en el sistema operativo, en un archivo `.env` o en el panel del proveedor de nube):

| Variable | Descripción | Ejemplo |
| :--- | :--- | :--- |
| `SPRING_DATASOURCE_URL` | URL de conexión JDBC a PostgreSQL | `jdbc:postgresql://localhost:5432/pixza` |
| `SPRING_DATASOURCE_USERNAME` | Usuario de la base de datos | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | Contraseña de la base de datos | `*****` |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | Estrategia de inicialización de JPA | `update` (producción) / `create` (inicio) |
| `TELEGRAM_BOT_NAME` | Username del bot configurado en @BotFather | `PixzaAdminBot` |
| `TELEGRAM_BOT_TOKEN` | Token de acceso HTTP del bot | `123456:ABC-DEF1234ghIkl-zyx57W2v1u123ew11` |

## 🛠️ Instalación y Uso Local

1.  **Clonar el repositorio:**
    ```bash
    git clone [https://github.com/TU_USUARIO/pixza.git](https://github.com/TU_USUARIO/pixza.git)
    cd pixza
    ```

2.  **Levantar la base de datos local:**
    Asegúrate de tener Docker instalado y ejecutándose.
    ```bash
    docker-compose up -d
    ```

3.  **Compilar y ejecutar la aplicación:**
    ```bash
    mvn clean install
    mvn spring-boot:run
    ```

## 🤖 Uso del Bot de Telegram

Una vez que la aplicación esté en ejecución, interactúa con el bot en Telegram:
1.  Inicia la conversación con el comando `/start`.
2.  Selecciona la opción de **Login**.
3.  Ingresa tus credenciales de administrador (creadas por defecto en la clase `ApplicationConfig`).
4.  Navega por los menús inline para gestionar categorías y lugares directamente desde tu dispositivo móvil.

## 📦 Estructura del Modelo de Datos

* **Users:** Gestiona credenciales y roles de acceso.
* **Categories:** Agrupaciones lógicas para los distintos lugares.
* **Places:** Entidades geográficas que contienen nombre, descripción, dirección (URL de Maps) y URL de imagen, vinculadas mediante una relación `@ManyToOne` a una categoría.
