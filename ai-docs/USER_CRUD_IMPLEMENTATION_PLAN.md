# Plan de Implementación: CRUD Básico para User

## Context

Este es el inicio de la **Fase 2** del proyecto traveler-backend. La Fase 1 (setup del repositorio y base de datos) está completa. Tenemos:

- Base de datos H2 con tabla `users` ya creada por Flyway
- Spring Boot 4.1.1 con Java 21 configurado
- Estructura de directorios lista pero vacía (entity/, repository/, service/, controller/, dto/)
- Spring Security en modo permisivo para desarrollo

El objetivo es implementar el **CRUD completo para la entidad User** siguiendo las mejores prácticas del proyecto definidas en AGENTS.md:

- Arquitectura en capas: Entity → Repository → Service → Controller
- DTOs para API (nunca exponer entidades directamente)
- Validación con Bean Validation
- Manejo centralizado de excepciones
- Passwords hasheados con BCrypt
- Tests completos para cada capa

Este es el primer CRUD del proyecto y establece el patrón que seguirán las demás entidades (Stay, Invitation, TravelerSubmission).

---

## Esquema de Base de Datos

**Tabla `users`** (ya existe):

```sql
id              BIGINT AUTO_INCREMENT PRIMARY KEY
email           VARCHAR(255) NOT NULL UNIQUE
password_hash   VARCHAR(255) NOT NULL
created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP

INDEX idx_users_email ON users(email)
```

---

## Archivos a Crear

### 1. Exception Layer (4 archivos)

**`src/main/java/com/traveler/traveler_backend/exception/UserNotFoundException.java`**

- RuntimeException para cuando no se encuentra un usuario
- Constructor acepta ID del usuario
- Mensaje: "User not found with id: {id}"

**`src/main/java/com/traveler/traveler_backend/exception/DuplicateEmailException.java`**

- RuntimeException para violaciones de unicidad de email
- Constructor acepta email
- Mensaje: "User with email {email} already exists"

**`src/main/java/com/traveler/traveler_backend/exception/ErrorResponse.java`**

- Record inmutable con campos:
  - `timestamp` (LocalDateTime)
  - `status` (int)
  - `error` (String)
  - `message` (String)

**`src/main/java/com/traveler/traveler_backend/exception/GlobalExceptionHandler.java`**

- `@RestControllerAdvice`
- Maneja UserNotFoundException → HTTP 404
- Maneja DuplicateEmailException → HTTP 409
- Maneja MethodArgumentNotValidException → HTTP 400 (validación)
- Maneja Exception genérica → HTTP 500 (sin exponer detalles internos)
- Retorna ErrorResponse en todos los casos

---

### 2. Entity Layer (1 archivo)

**`src/main/java/com/traveler/traveler_backend/entity/User.java`**

- `@Entity`, `@Table(name = "users")`
- Campos:
  - `id`: Long, `@Id @GeneratedValue(strategy = IDENTITY)`
  - `email`: String, `@Column(nullable = false, unique = true)`
  - `passwordHash`: String, `@Column(name = "password_hash", nullable = false)`
  - `createdAt`: LocalDateTime, `@CreationTimestamp`, `@Column(name = "created_at", updatable = false)`
  - `updatedAt`: LocalDateTime, `@UpdateTimestamp`, `@Column(name = "updated_at")`
- Constructor sin argumentos (JPA) y constructor builder
- `toString()` sin incluir passwordHash
- `equals/hashCode` basados en id

---

### 3. Repository Layer (1 archivo)

**`src/main/java/com/traveler/traveler_backend/repository/UserRepository.java`**

- Interface extends `JpaRepository<User, Long>`
- Métodos custom:
  - `Optional<User> findByEmail(String email)`
  - `boolean existsByEmail(String email)`

---

### 4. DTO Layer (3 archivos)

**`src/main/java/com/traveler/traveler_backend/dto/CreateUserRequest.java`**

- Record con campos:
  - `email`: String - `@NotBlank @Email @Size(max = 255)`
  - `password`: String (raw) - `@NotBlank @Size(min = 8, max = 100)`

**`src/main/java/com/traveler/traveler_backend/dto/UpdateUserRequest.java`**

- Record con campos opcionales:
  - `email`: String - `@Email @Size(max = 255)` (nullable)
  - `password`: String (raw) - `@Size(min = 8, max = 100)` (nullable)
- Al menos un campo debe estar presente

**`src/main/java/com/traveler/traveler_backend/dto/UserResponse.java`**

- Record con campos:
  - `id`: Long
  - `email`: String
  - `createdAt`: LocalDateTime
  - `updatedAt`: LocalDateTime
- **NO incluye password**
- Método estático `fromEntity(User user)` para mapeo

---

### 5. Configuration Layer (1 archivo)

**`src/main/java/com/traveler/traveler_backend/config/PasswordEncoderConfig.java`**

- `@Configuration`
- `@Bean` que retorna `BCryptPasswordEncoder` (default strength 10)
- Este bean se inyecta en UserService

---

### 6. Service Layer (1 archivo)

**`src/main/java/com/traveler/traveler_backend/service/UserService.java`**

- `@Service`, `@Transactional` a nivel de clase
- Inyección de constructor: UserRepository, PasswordEncoder

**Métodos:**

**`createUser(CreateUserRequest request) → UserResponse`**

- Verificar email único con `existsByEmail()`
- Si existe, lanzar `DuplicateEmailException`
- Hashear password con `passwordEncoder.encode()`
- Crear y guardar User
- Retornar `UserResponse.fromEntity()`

**`getUserById(Long id) → UserResponse`**

- Buscar con `findById()`
- Si no existe, lanzar `UserNotFoundException`
- Retornar UserResponse
- `@Transactional(readOnly = true)`

**`getAllUsers() → List<UserResponse>`**

- `findAll()` y mapear a UserResponse
- `@Transactional(readOnly = true)`
- Nota: Versión básica sin paginación (agregar después)

**`updateUser(Long id, UpdateUserRequest request) → UserResponse`**

- Buscar usuario existente
- Si `request.email()` no null y diferente:
  - Verificar unicidad del nuevo email
  - Si está tomado, lanzar `DuplicateEmailException`
  - Actualizar email
- Si `request.password()` no null:
  - Hashear y actualizar passwordHash
- Guardar y retornar UserResponse

**`deleteUser(Long id) → void`**

- Verificar existencia
- Si no existe, lanzar `UserNotFoundException`
- Eliminar con `deleteById()`

---

### 7. Controller Layer (1 archivo)

**`src/main/java/com/traveler/traveler_backend/controller/UserController.java`**

- `@RestController`, `@RequestMapping("/api/v1/users")`
- Inyección de constructor: UserService

**Endpoints:**

**POST /api/v1/users**

- `@PostMapping`, `@ResponseStatus(CREATED)`
- Parámetro: `@Valid @RequestBody CreateUserRequest`
- Llama `userService.createUser()`
- Retorna UserResponse (201)

**GET /api/v1/users/{id}**

- `@GetMapping("/{id}")`
- Parámetro: `@PathVariable Long id`
- Llama `userService.getUserById()`
- Retorna UserResponse (200)

**GET /api/v1/users**

- `@GetMapping`
- Llama `userService.getAllUsers()`
- Retorna List<UserResponse> (200)

**PUT /api/v1/users/{id}**

- `@PutMapping("/{id}")`
- Parámetros: `@PathVariable Long id`, `@Valid @RequestBody UpdateUserRequest`
- Llama `userService.updateUser()`
- Retorna UserResponse (200)

**DELETE /api/v1/users/{id}**

- `@DeleteMapping("/{id}")`
- `@ResponseStatus(NO_CONTENT)`
- Parámetro: `@PathVariable Long id`
- Llama `userService.deleteUser()`
- Retorna void (204)

---

## Tests

### Repository Tests (1 archivo)

**`src/test/java/com/traveler/traveler_backend/repository/UserRepositoryTest.java`**

- `@DataJpaTest`
- Tests:
  - save, findById, findByEmail, existsByEmail
  - update, delete
  - timestamps automáticos
  - constraint de email único

### Service Tests (1 archivo)

**`src/test/java/com/traveler/traveler_backend/service/UserServiceTest.java`**

- Mocks con Mockito: UserRepository, PasswordEncoder
- `@ExtendWith(MockitoExtension.class)`
- Tests para cada método:
  - createUser: éxito, email duplicado, hashing de password
  - getUserById: éxito, usuario no encontrado
  - getAllUsers: lista con datos, lista vacía
  - updateUser: email solo, password solo, ambos, duplicado, usuario no encontrado, hashing
  - deleteUser: éxito, usuario no encontrado

### Controller Tests (1 archivo)

**`src/test/java/com/traveler/traveler_backend/controller/UserControllerTest.java`**

- `@WebMvcTest(UserController.class)`
- `@MockBean` para UserService
- `@Autowired MockMvc`, `@Autowired ObjectMapper`
- Tests:
  - POST: éxito (201), validación (400), duplicado (409)
  - GET por ID: éxito (200), no encontrado (404)
  - GET todos: éxito (200)
  - PUT: éxito (200), validación (400), no encontrado (404), duplicado (409)
  - DELETE: éxito (204), no encontrado (404)

---

## Orden de Implementación

1. **Exception layer** - Base para manejo de errores
2. **Entity layer** - Modelo de datos
3. **Repository layer + tests** - Acceso a datos
4. **DTO layer** - Contratos de API
5. **Configuration** - PasswordEncoder bean
6. **Service layer + tests** - Lógica de negocio
7. **Controller layer + tests** - API REST

---

## API Endpoints Especificación

### POST /api/v1/users

```json
Request:
{
  "email": "user@example.com",
  "password": "securepass123"
}

Response (201):
{
  "id": 1,
  "email": "user@example.com",
  "createdAt": "2026-11-03T10:30:00",
  "updatedAt": "2026-11-03T10:30:00"
}

Errors: 400 (validación), 409 (email duplicado)
```

### GET /api/v1/users/{id}

```json
Response (200):
{
  "id": 1,
  "email": "user@example.com",
  "createdAt": "2026-11-03T10:30:00",
  "updatedAt": "2026-11-03T10:30:00"
}

Errors: 404 (no encontrado)
```

### GET /api/v1/users

```json
Response (200):
[
  {
    "id": 1,
    "email": "user1@example.com",
    "createdAt": "2026-11-03T10:30:00",
    "updatedAt": "2026-11-03T10:30:00"
  }
]
```

### PUT /api/v1/users/{id}

```json
Request (campos opcionales):
{
  "email": "newemail@example.com",
  "password": "newpassword123"
}

Response (200):
{
  "id": 1,
  "email": "newemail@example.com",
  "createdAt": "2026-11-03T10:30:00",
  "updatedAt": "2026-11-03T11:45:00"
}

Errors: 400 (validación), 404 (no encontrado), 409 (email duplicado)
```

### DELETE /api/v1/users/{id}

```
Response: 204 No Content
Errors: 404 (no encontrado)
```

---

## Archivos Críticos Existentes (Referencia)

- `src/main/resources/db/migration/V1__initial_schema.sql` - Schema ya creado
- `src/main/java/com/traveler/traveler_backend/config/SecurityConfig.java` - Seguridad permisiva actual
- `pom.xml` - Todas las dependencias ya disponibles
- `src/main/resources/application.properties` - Configuración H2

---

## Notas de Implementación

### Seguridad

- **Passwords**: Nunca logear, nunca retornar en responses
- **BCrypt**: Strength por defecto (10 rounds)
- **Hashing**: Siempre en el servicio, antes de guardar
- **Authorization**: Diseño permite agregar `@PreAuthorize` después

### Validación

- Email: formato válido, max 255 chars, único
- Password: min 8 chars, max 100 chars
- Update: al menos un campo presente

### Transacciones

- Service methods son `@Transactional`
- Métodos de lectura: `@Transactional(readOnly = true)`
- Operaciones atómicas: email check + save en mismo TX

### Calidad de Código

- Constructor injection (no `@Autowired` en campos)
- Records para DTOs inmutables
- Sin Lombok (proyecto no lo usa)
- Controllers delgados
- Lógica en services

---

## Verificación

Después de la implementación, ejecutar:

```bash
./mvnw clean test
```

**Expectativa**: Todos los tests pasan (verde)

**Testing manual** con H2 Console:

1. Arrancar aplicación: `./mvnw spring-boot:run`
2. Crear usuario con POST /api/v1/users
3. Verificar en H2 Console: http://localhost:8080/h2-console
   - JDBC URL: `jdbc:h2:file:./data/travelerdb`
   - User: `sa`, Password: `admin123`
   - Query: `SELECT * FROM users;`
4. Verificar password hasheado (debería empezar con $2a$)

**Testing con curl/Postman:**

```bash
# Crear usuario
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}'

# Obtener usuario
curl http://localhost:8080/api/v1/users/1

# Listar usuarios
curl http://localhost:8080/api/v1/users

# Actualizar usuario
curl -X PUT http://localhost:8080/api/v1/users/1 \
  -H "Content-Type: application/json" \
  -d '{"email":"newemail@example.com"}'

# Eliminar usuario
curl -X DELETE http://localhost:8080/api/v1/users/1
```

---

## Resumen de Archivos

**Total: 15 archivos nuevos**

**Producción (12):**

- 4 exception
- 1 entity
- 1 repository
- 3 dto
- 1 config
- 1 service
- 1 controller

**Tests (3):**

- 1 repository test
- 1 service test
- 1 controller test

Este CRUD establece el patrón base para las siguientes entidades del proyecto.
