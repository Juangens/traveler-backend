# Migración de PostgreSQL a H2

**Fecha**: 2 de noviembre de 2026

## 📋 Resumen

El proyecto ha sido migrado de PostgreSQL a H2 Database para simplificar el desarrollo local y eliminar problemas de configuración de base de datos externa.

## ✅ Cambios Realizados

### 1. Dependencias (pom.xml)

**Eliminado:**

```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-postgresql</artifactId>
</dependency>
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

**Agregado:**

```xml
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
```

### 2. Configuración (application.properties)

**Antes:**

```properties
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/traveler_db}
spring.datasource.username=${DB_USERNAME:traveler_user}
spring.datasource.password=${DB_PASSWORD:changeme}
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

**Ahora:**

```properties
spring.datasource.url=jdbc:h2:file:./data/travelerdb;DB_CLOSE_ON_EXIT=FALSE;AUTO_RECONNECT=TRUE
spring.datasource.username=sa
spring.datasource.password=
spring.datasource.driver-class-name=org.h2.Driver
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect
```

### 3. Migraciones SQL

**Cambio principal:** `BIGSERIAL` → `BIGINT AUTO_INCREMENT`

H2 no soporta el tipo `BIGSERIAL` de PostgreSQL. Se cambió a `BIGINT AUTO_INCREMENT` que es compatible con ambas bases de datos.

**Archivo modificado:**

- `src/main/resources/db/migration/V1__initial_schema.sql`

### 4. Archivos Eliminados

Se eliminaron todos los archivos relacionados con la configuración de PostgreSQL:

**Scripts .bat:**

- `add-postgres-to-path.bat`
- `check-postgres.bat`
- `edit-pghba.bat`
- `find-pg-config.bat`
- `fix-database.bat`
- `open-pgadmin.bat`
- `quick-setup-with-password.bat`
- `quick-setup.bat`
- `restart-postgres.bat`
- `setup-database.bat`
- `setup-postgres-db.bat`
- `setup-with-windows-auth.bat`
- `test-connection.bat`
- `verify-postgres.bat`

**Archivos SQL:**

- `setup-database.sql`

**Documentación:**

- `PGADMIN_STEPS.md`
- `SETUP_POSTGRESQL.md`
- `INTELLIJ_DATABASE_SETUP.md`
- `reset-postgres-password.md`

### 5. Documentación Actualizada

**Nuevos archivos:**

- `H2_DATABASE.md` - Guía completa de uso de H2

**Actualizados:**

- `README.md` - Secciones de setup y troubleshooting
- `.env` - Comentarios sobre H2
- `.env.example` - Ejemplo actualizado

## 🚀 Cómo Usar H2

### Iniciar la aplicación

```bash
./mvnw.cmd spring-boot:run
```

### Acceder a la consola H2

1. Abrir navegador: `http://localhost:8080/h2-console`
2. Credenciales:
   - **JDBC URL**: `jdbc:h2:file:./data/travelerdb`
   - **User Name**: `sa`
   - **Password**: (dejar vacío)

### Base de datos

- **Ubicación**: `./data/travelerdb.mv.db`
- **Persistencia**: Los datos se guardan entre reinicios
- **Reset**: Eliminar carpeta `./data/` para empezar de cero

## 🔄 Volver a PostgreSQL (Futuro)

Si en el futuro se necesita volver a PostgreSQL para producción:

1. Restaurar dependencias en `pom.xml`
2. Actualizar `application.properties`
3. Cambiar SQL a usar `BIGSERIAL` en lugar de `BIGINT AUTO_INCREMENT`
4. Configurar servidor PostgreSQL

## ✨ Ventajas de H2

✅ **Sin instalación externa** - Base de datos embebida  
✅ **Sin configuración** - Funciona inmediatamente  
✅ **Sin problemas de contraseñas** - No requiere autenticación compleja  
✅ **Consola web integrada** - Ver y consultar datos fácilmente  
✅ **Perfecto para desarrollo** - Rápido de resetear y probar  
✅ **Compatible con tests** - Ideal para tests de integración

## 📝 Notas Técnicas

- Flyway sigue funcionando igual, las migraciones se aplican automáticamente
- El esquema SQL es compatible con ambas bases de datos (H2 y PostgreSQL)
- JPA/Hibernate funciona transparentemente con H2
- Los tests no requieren cambios

## ⚠️ Limitaciones

- H2 no es para producción a gran escala
- Algunas características avanzadas de PostgreSQL no están disponibles
- Para ambientes productivos, considerar volver a PostgreSQL o usar otra base de datos robusta
