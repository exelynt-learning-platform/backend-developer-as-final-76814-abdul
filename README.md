# Resource Booking System

Secure RESTful API for booking resources (rooms, vehicles, equipment) with JWT authentication
and role-based access control (RBAC), built with Spring Boot 3 / Java 17 / Spring Security.

## Tech Stack
- Java 17, Spring Boot 3.3
- Spring Web, Spring Data JPA, Spring Security
- JWT via `io.jsonwebtoken` (jjwt)
- H2 (default, zero-setup) / PostgreSQL / MySQL (via profiles)
- Lombok, Bean Validation

## Project Structure
```
src/main/java/com/exelynt/booking/
  config/       SecurityConfig, JwtAuthFilter, DataSeeder
  security/     JwtService (issue/validate tokens)
  model/        User, ResourceEntity, Reservation, Role, ReservationStatus
  repository/   Spring Data JPA repositories
  dto/          request/ and response/ payloads
  service/      AuthService, ResourceService, ReservationService, CustomUserDetailsService
  controller/   AuthController, ResourceController, ReservationController
  exception/    Custom exceptions + GlobalExceptionHandler
```

## Running it

### Quick start (H2 in-memory, no DB install needed)
```bash
mvn spring-boot:run
```
The app starts on `http://localhost:8080`. An H2 console is available at `/h2-console`
(JDBC URL: `jdbc:h2:mem:bookingdb`, user `sa`, blank password) if you want to inspect data.

### With PostgreSQL
1. Create a database: `createdb booking_system`
2. Add the `mysql-connector-j` dependency removal isn't needed — Postgres driver is already in `pom.xml`.
3. Run:
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=postgres
   ```
   Override credentials via `DB_USERNAME` / `DB_PASSWORD` env vars if not using the defaults
   (`postgres`/`postgres`).

### With MySQL
1. Add this dependency to `pom.xml`:
   ```xml
   <dependency>
       <groupId>com.mysql</groupId>
       <artifactId>mysql-connector-j</artifactId>
       <scope>runtime</scope>
   </dependency>
   ```
2. Create a database: `CREATE DATABASE booking_system;`
3. Run:
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=mysql
   ```

### Environment variables (all optional, sensible defaults provided)
| Variable | Purpose | Default |
|---|---|---|
| `JWT_SECRET` | HMAC signing key for JWTs | dev-only fallback (change for production) |
| `JWT_EXPIRATION_MS` | Token lifetime in ms | `3600000` (1 hour) |
| `ADMIN_USERNAME` / `ADMIN_PASSWORD` / `ADMIN_EMAIL` | Bootstrap admin, seeded on first run | `admin` / `admin123` / `admin@exelynt.com` |
| `DB_USERNAME` / `DB_PASSWORD` | DB credentials (postgres/mysql profiles) | profile-specific |

## API Reference

### Auth
| Method | Path | Access | Notes |
|---|---|---|---|
| POST | `/auth/register` | Public | Always creates a `USER` (never `ADMIN`) |
| POST | `/auth/login` | Public | Returns `{ token, username, role }` |

**Login example:**
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```
Use the returned `token` as `Authorization: Bearer <token>` on all subsequent requests.

### Resources
| Method | Path | Access |
|---|---|---|
| GET | `/resources` | USER, ADMIN |
| GET | `/resources/{id}` | USER, ADMIN |
| POST | `/resources` | ADMIN only |
| PUT | `/resources/{id}` | ADMIN only |
| DELETE | `/resources/{id}` | ADMIN only |

**Create a resource (as admin):**
```bash
curl -X POST http://localhost:8080/resources \
  -H "Authorization: Bearer <ADMIN_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"name":"Conference Room A","type":"ROOM","description":"Seats 10","available":true}'
```

### Reservations
| Method | Path | Access | Notes |
|---|---|---|---|
| POST | `/reservations` | USER, ADMIN | User identity taken from JWT, never from the body |
| GET | `/reservations` | USER, ADMIN | ADMIN sees all; USER sees only their own |
| GET | `/reservations/{id}` | USER, ADMIN | USER gets 403 if not the owner |
| PATCH | `/reservations/{id}/status` | USER, ADMIN | ADMIN: any status. USER: may only set `CANCELLED` on their own |
| DELETE | `/reservations/{id}` | ADMIN only | Hard delete |

**Create a reservation (as a regular user):**
```bash
curl -X POST http://localhost:8080/reservations \
  -H "Authorization: Bearer <USER_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"resourceId":1,"startTime":"2026-10-01T09:00:00","endTime":"2026-10-01T10:00:00"}'
```

## Key security decisions

- **Stateless JWT auth** — no server-side sessions; `SecurityConfig` sets
  `SessionCreationPolicy.STATELESS` and a custom `JwtAuthFilter` runs before Spring Security's
  auth filter to populate the `SecurityContext` from the Bearer token on every request.
- **RBAC enforced twice** — once broadly via URL matchers in `SecurityConfig`
  (`hasRole("ADMIN")` / `hasAnyRole("USER","ADMIN")`), and again per-method via `@PreAuthorize`
  on controllers, for defense in depth.
- **Ownership enforced in the service layer** — `ReservationService` resolves the current user
  from the `SecurityContext` (populated from the JWT), never from client input. A `USER`
  can only list/view/cancel their own reservations; attempting anything else throws
  `AccessDeniedOwnershipException` → HTTP 403.
- **Passwords** are BCrypt-hashed (`BCryptPasswordEncoder`) and never returned in any response DTO.
- **Public registration always creates a `USER`** — there's no way for a client to self-register
  as `ADMIN`; the only admin account comes from the seeded bootstrap admin (see env vars above).

## Testing the RBAC rules manually
1. Log in as `admin` / `admin123` → note the token.
2. Register a second account via `/auth/register` (becomes a `USER`) and log in with it.
3. As `USER`: `GET /resources` works; `POST /resources` returns `403 Forbidden`.
4. As `USER`: create a reservation, then try `GET /reservations/{someone-else's-id}` → `403`.
5. As `ADMIN`: `GET /reservations` returns every user's reservations; `PATCH .../status` can set
   any status; `DELETE /reservations/{id}` works.
