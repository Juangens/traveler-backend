# Solución Final - Configurar PostgreSQL

La contraseña que ingresaste (`123turia`) no funciona para el usuario `postgres`. 

Tienes **2 opciones**:

---

## ✅ OPCIÓN 1: Usar pgAdmin 4 (RECOMENDADO - 5 minutos)

pgAdmin 4 puede tener guardada la contraseña correcta.

### Pasos:

1. **Busca "pgAdmin 4"** en el menú inicio de Windows y ábrelo
   - O ejecuta: `open-pgadmin.bat`

2. **Configura Master Password** (solo para pgAdmin, puede ser cualquiera como "admin")

3. **Expande "Servers"** en el panel izquierdo

4. **Click en "PostgreSQL 18"**
   - Te pedirá la contraseña del usuario postgres
   - Prueba estas contraseñas comunes:
     - `123turia`
     - `postgres`
     - `admin`  
     - `root`
     - (déjala en blanco)

5. **Si alguna funciona**, verás la conexión establecida ✓

6. **Ahora crea la base de datos**:
   - Click derecho en "Databases" → "Create" → "Database"
   - Name: `traveler_db`
   - Click "Save"

7. **Crea el usuario**:
   - Click derecho en "Login/Group Roles" → "Create" → "Login/Group Role"
   - Tab "General" → Name: `traveler_user`
   - Tab "Definition" → Password: `dev123` (confirma también)
   - Tab "Privileges" → Marca "Can login?"
   - Click "Save"

8. **Da permisos**:
   - Click derecho en database `traveler_db` → "Query Tool"
   - Pega y ejecuta (botón ▶️):
   ```sql
   GRANT ALL ON SCHEMA public TO traveler_user;
   GRANT ALL PRIVILEGES ON DATABASE traveler_db TO traveler_user;
   ALTER USER traveler_user CREATEDB;
   ```

9. **Actualiza el .env** con la contraseña correcta de traveler_user:
   ```
   DB_PASSWORD=dev123
   ```

10. **Ejecuta la aplicación**: `mvnw.cmd spring-boot:run`

---

## ⚙️ OPCIÓN 2: Reinstalar PostgreSQL (10 minutos)

Si no logras conectar con pgAdmin 4 con ninguna contraseña:

### Pasos:

1. **Desinstalar PostgreSQL**:
   - Panel de Control → Programas y características
   - Busca "PostgreSQL 18" 
   - Click derecho → Desinstalar
   - Marca "Remove data directory" si te pregunta

2. **Eliminar carpetas restantes** (si existen):
   - `C:\Program Files\PostgreSQL\18`
   - `C:\Users\[tu_usuario]\AppData\Roaming\postgresql`

3. **Descargar PostgreSQL**:
   - https://www.postgresql.org/download/windows/
   - Descarga PostgreSQL 18 para Windows x64

4. **Instalar**:
   - Ejecuta el instalador
   - Componentes: marca TODOS (PostgreSQL Server, pgAdmin 4, Command Line Tools)
   - **IMPORTANTE**: Cuando pida contraseña para postgres:
     - Usa: `postgres123`
     - **ANÓTALA en papel** 📝
   - Puerto: 5432 (default)
   - Finish

5. **Después de instalar**:
   - Edita `quick-setup-with-password.bat`
   - Cambia `SET PGPASSWORD=123turia` por `SET PGPASSWORD=postgres123`
   - Ejecuta el archivo
   - Debería funcionar ✓

6. **Ejecuta la aplicación**: `mvnw.cmd spring-boot:run`

---

## 📋 Resumen

**Problema**: No conocemos la contraseña correcta del usuario `postgres`

**Soluciones**:
- **Opción 1** (más rápida): Usar pgAdmin 4 para conectar y configurar
- **Opción 2** (más segura): Reinstalar PostgreSQL anotando bien la contraseña

---

## 🆘 Si nada funciona

Como última alternativa, podemos:
1. Usar **H2 Database** (base de datos en memoria) temporalmente para el desarrollo
2. Cambiar a **MySQL** si lo tienes instalado
3. Usar **Docker** con PostgreSQL (aunque el proyecto especifica no usarlo)

Pero primero intenta la Opción 1 con pgAdmin 4.
