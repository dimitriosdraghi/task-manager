# Task Manager API

A REST API for managing personal tasks, built with **Spring Boot**, **Spring Security (JWT)** and **PostgreSQL**.

Users register and log in to receive a **JWT**. With it they can create, view, update and delete **their own** tasks.
An **ADMIN** can view and delete the tasks of all users.

## Tech stack

- Java 21, Spring Boot 4.1, Maven
- Spring Web MVC (REST), Spring Data JPA / Hibernate, PostgreSQL
- Spring Security: stateless JWT authentication (jjwt), BCrypt password hashing, role-based access (USER / ADMIN)
- Bean Validation, centralized error handling (`@RestControllerAdvice`)
- JUnit 5 + Mockito unit tests

## Architecture

```
Request ──► Spring Security filter chain (JwtAuthFilter: checks the JWT) ──► Controller ──► Service (business rules) ──► Repository ──► PostgreSQL
```

| Layer | Classes |
|---|---|
| Controller | `AuthController`, `TaskController`, `AdminController` |
| Service | `AuthService`, `TaskService` (ownership rule: own tasks only, unless ADMIN) |
| Repository | `UserRepository`, `TaskRepository` (Spring Data JPA) |
| Entity | `User`, `Task` (`tasks.user_id` → `users.id`), `Role` |
| Security | `SecurityConfig`, `JwtAuthFilter`, `JwtService` |
| Errors | `GlobalExceptionHandler`, `TaskNotFoundException`, `UsernameAlreadyExistsException` |

## Endpoints

| Method | Endpoint | Description | Access |
|---|---|---|---|
| POST | `/auth/register` | Register (role is always USER) → 201 | Public |
| POST | `/auth/login` | Log in → 200 + JWT | Public |
| GET | `/tasks` | My tasks | Authenticated |
| POST | `/tasks` | Create a task → 201 | Authenticated |
| GET | `/tasks/{id}` | Get a task | Owner or ADMIN |
| PUT | `/tasks/{id}` | Replace a task | Owner or ADMIN |
| PATCH | `/tasks/{id}` | Partially update a task | Owner or ADMIN |
| DELETE | `/tasks/{id}` | Delete a task → 204 | Owner or ADMIN |
| GET | `/admin/tasks` | All tasks of all users | ADMIN |

Send the token in the header: `Authorization: Bearer <token>`

### Status codes

| Code | When |
|---|---|
| 400 | Validation failed (e.g. empty title), malformed JSON |
| 401 | Missing / invalid / expired token, wrong username or password |
| 403 | Valid token, but not allowed (USER on `/admin/**`, someone else's task) |
| 404 | Task not found |
| 409 | Username already exists |

## Run with Docker (recommended)

Requires only [Docker](https://www.docker.com/products/docker-desktop/).

```
cp .env.example .env        # then set your own DB_PASSWORD in .env
docker compose up -d --build
```

This starts two containers:

- `taskmanager-db`: PostgreSQL 18, data kept in the `taskdb-data` volume (reachable from the host on port 5434)
- `taskmanager-app`: the API on `http://localhost:8080`, started only after the database is healthy

Stop with `docker compose down` (add `-v` to also delete the database data).

## Run locally (without Docker)

1. **PostgreSQL**: create a database `taskmanager`. The default URL is `jdbc:postgresql://localhost:5433/taskmanager`
   (change it in `src/main/resources/application.properties` if your PostgreSQL uses another port, e.g. 5432).
2. **Environment variables**:
   - `DB_PASSWORD` (required): the password of the `postgres` database user
   - `JWT_SECRET` (optional, at least 32 characters): if missing, a development-only default is used
3. **Run** `TaskManagerApplication` (or `mvn spring-boot:run`). The API starts on `http://localhost:8080`.
   Hibernate creates the tables automatically (`ddl-auto=update`).
4. **Try it**: import `TaskManager.postman_collection.json` into Postman. The login requests save the token automatically.

### Make a user ADMIN

New users are always `USER`. To make one ADMIN, run in pgAdmin (Query Tool on `taskmanager`), or with Docker:
`docker exec -it taskmanager-db psql -U postgres -d taskmanager`

```sql
UPDATE users SET role = 'ADMIN' WHERE username = 'maria';
```

Then log in again, so that the new token contains the ADMIN role.

## Tests

```
mvn test
```

Unit tests for the ownership rules (`TaskServiceTest`) and for JWT creation / tampered / expired tokens (`JwtServiceTest`).
