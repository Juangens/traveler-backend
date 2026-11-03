# Base de Datos H2 - Traveler Backend

## ✅ ¿Qué es H2?

H2 es una base de datos en memoria/archivo Java:

- **No requiere instalación** externa
- **Perfecta para desarrollo** local
- **Consola web integrada** para ver los datos
- Compatible con SQL estándar

## 🚀 Cómo Usar

### 1. Ejecutar la Aplicación

```bash
./mvnw spring-boot:run
```

O desde IntelliJ IDEA:

- Abre `TravelerBackendApplication.java`
- Click en el botón verde ▶️ Run

### 2. Acceder a la Consola H2

Una vez la aplicación esté corriendo:

1. Abre tu navegador en: **http://localhost:8080/h2-console**

2. Usa estas credenciales:
   - **JDBC URL**: `jdbc:h2:file:./data/travelerdb`
   - **User Name**: `sa`
   - **Password**: `admin123`

3. Click en **Connect**

### 3. Ver los Datos

Desde la consola H2 puedes ejecutar queries SQL:

```sql
-- Ver todas las tablas
SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'PUBLIC';

-- Ver usuarios
SELECT * FROM USERS;

-- Ver invitaciones
SELECT * FROM INVITATIONS;

-- Ver submissions
SELECT * FROM TRAVELER_SUBMISSIONS;
```

## 📁 Ubicación de la Base de Datos

Los datos se guardan en:

```
./data/travelerdb.mv.db
```

Este archivo persiste los datos entre ejecuciones.

## 🔄 Resetear la Base de Datos

Si quieres empezar desde cero:

```bash
rm -rf ./data/
```

La próxima vez que ejecutes la aplicación, Flyway recreará todas las tablas.

## 📝 Migraciones

Flyway está configurado y las migraciones se encuentran en:

```
src/main/resources/db/migration/V1__initial_schema.sql
```

Las migraciones se ejecutan automáticamente al iniciar la aplicación.

## 🎯 Ventajas de H2

✅ Sin problemas de contraseñas  
✅ Sin servicios externos  
✅ Rápida configuración  
✅ Fácil de resetear  
✅ Consola web integrada

## 🔄 Migrar a PostgreSQL más tarde

Cuando estés listo para producción, solo necesitas:

1. Cambiar `application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/traveler_db
spring.datasource.username=traveler_user
spring.datasource.password=dev123
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

2. Actualizar `pom.xml` con la dependencia de PostgreSQL

Las migraciones SQL ya son compatibles con ambas bases de datos.
