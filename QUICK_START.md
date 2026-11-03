# 🚀 Quick Start - Traveler Backend

## Paso 1: Configurar PostgreSQL (5 minutos)

### Opción A: Desde CMD/PowerShell (Rápido)

1. Abre **CMD** o **PowerShell** como Administrador
2. Navega al proyecto:
   ```bash
   cd "C:\Users\jums\OneDrive - GFT Technologies SE\Desktop\traveler-backend"
   ```
3. Ejecuta el setup:
   ```bash
   setup-postgres-db.bat
   ```
   - Te pedirá la contraseña de `postgres`
   - Escribe la contraseña y presiona Enter

### Opción B: Desde IntelliJ (Más visual)

1. **Abrir Database Tool Window:**
   - `View → Tool Windows → Database`
   - O presiona `Alt+1` y selecciona "Database"

2. **Agregar Conexión PostgreSQL:**
   - Clic en `+` (Add)
   - Selecciona `Data Source → PostgreSQL`

3. **Configurar Conexión:**

   ```
   Host: localhost
   Port: 5432
   Database: postgres (temporal, para crear la DB)
   User: postgres
   Password: [tu contraseña de postgres]
   ```

4. **Test Connection** → Debería ser exitoso

5. **Abrir Query Console:**
   - Clic derecho en la conexión → `New → Query Console`

6. **Ejecutar el Setup SQL:**

   ```sql
   -- Copia y pega el contenido de setup-database.sql
   -- O abre el archivo y ejecuta con Ctrl+Enter
   ```

7. **Actualizar la Conexión:**
   - Cambia el Database de `postgres` a `traveler_db`
   - User: `traveler_user`
   - Password: `dev123`
   - Test Connection nuevamente

## Paso 2: Verificar la Configuración

```bash
# Desde CMD/PowerShell
test-connection.bat
```

O desde IntelliJ:

- En Database tool window, expande `traveler_db → schemas → public`
- Deberías ver la estructura vacía (las tablas se crearán con Flyway)

## Paso 3: Configurar Variables de Entorno en IntelliJ

### Opción A: Variables Manuales

1. `Run → Edit Configurations`
2. Selecciona `TravelerBackendApplication` (o crea una nueva)
3. En "Environment variables", pega:
   ```
   DB_URL=jdbc:postgresql://localhost:5432/traveler_db;DB_USERNAME=traveler_user;DB_PASSWORD=dev123
   ```

### Opción B: Plugin EnvFile (Recomendado)

1. **Instalar Plugin:**
   - `File → Settings → Plugins`
   - Busca "EnvFile" e instala
   - Reinicia IntelliJ si es necesario

2. **Configurar EnvFile:**
   - `Run → Edit Configurations`
   - Tab "EnvFile"
   - Clic `+` → "Add"
   - Selecciona el archivo `.env` del proyecto
   - Marca "Enable EnvFile"

## Paso 4: Configurar Java 21 (Importante)

El proyecto requiere Java 21, pero estás usando Java 24.

1. **Descargar Java 21:**
   - https://adoptium.net/temurin/releases/?version=21
   - Descarga e instala

2. **Configurar en IntelliJ:**
   - `File → Project Structure` (Ctrl+Alt+Shift+S)
   - **Project:**
     - SDK: Selecciona Java 21
     - Language level: 21
   - **Modules:**
     - Verifica que el Language level sea 21
   - **Apply → OK**

3. **Configurar Run Configuration:**
   - `Run → Edit Configurations`
   - JRE: Selecciona Java 21

## Paso 5: Ejecutar la Aplicación

### Desde IntelliJ:

1. Abre `TravelerBackendApplication.java`
2. Clic derecho → `Run 'TravelerBackendApplication'`

### Desde Terminal:

```bash
# Asegúrate de que JAVA_HOME apunte a Java 21
mvnw.cmd spring-boot:run
```

## ✅ Verificación Final

Si todo está correcto, verás:

```
INFO  o.f.core.internal.command.DbMigrate - Successfully applied 1 migration
INFO  com.zaxxer.hikari.HikariDataSource - HikariPool-1 - Start completed
INFO  c.t.t.TravelerBackendApplication - Started TravelerBackendApplication in 3.456 seconds
```

Y puedes acceder a: **http://localhost:8080**

## 🐛 Troubleshooting

### Error: "Connection refused"

```bash
# Verificar servicio PostgreSQL
sc query postgresql-x64-18

# Si está parado, iniciarlo
net start postgresql-x64-18
```

### Error: "password authentication failed"

- Verifica que la contraseña en `.env` sea `dev123`
- O cambia la contraseña en PostgreSQL:
  ```sql
  ALTER USER traveler_user WITH PASSWORD 'dev123';
  ```

### Error: "Unsupported class file major version 68"

- Estás usando Java 24 en lugar de Java 21
- Sigue el Paso 4 para configurar Java 21

### Error: "Failed to clean project"

- Cierra IntelliJ
- Borra la carpeta `target` manualmente
- Reabre IntelliJ

## 📚 Documentación Completa

- [INTELLIJ_DATABASE_SETUP.md](INTELLIJ_DATABASE_SETUP.md) - Guía detallada de IntelliJ
- [README.md](README.md) - Documentación completa del proyecto
- [PROJECT_PLAN.md](ai-docs/PROJECT_PLAN.md) - Plan del proyecto
- [.claude.md](.claude.md) - Instrucciones de desarrollo

## 🎯 Siguiente Paso

Una vez que la aplicación arranque correctamente, estarás listo para la **Fase 2: Implementación de CRUD**.
