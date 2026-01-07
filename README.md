# ClassBuddy

ClassBuddy is a JavaFX desktop app for classroom management. It’s built around a simple MVC-style structure (controllers + services + models) and uses SQLite for local persistence.

## What it does (current)
- Role-based experience (Admin / Student)
- Classroom creation and joining (password-protected)
- Calendar/routine-style scheduling screens (routines, exams, notices, etc.)
- Local notifications via a background scheduler

## Tech stack
- Java 21 + JavaFX 21 (UI)
- Maven (build)
- SQLite (local database)
- BCrypt (password hashing)
- Ikonli (icons)

## Project layout
- `src/main/java/com/classbuddy/`
  - `controller/` JavaFX controllers (UI logic)
  - `service/` database + business logic
  - `model/` POJOs
  - `util/` helpers (DB init, transitions, etc.)
- `src/main/resources/`
  - `fxml/` UI views
  - `css/` styles
  - `schema.sql` database schema

## Running the app (Windows)
### Option 1: Quick start
- Run [START.bat](START.bat)

### Option 2: Maven wrapper
From the repo root:
- Run: `mvnw.cmd clean javafx:run`

### Compile only
- Run [COMPILE_ONLY.bat](COMPILE_ONLY.bat)
- Or: `mvnw.cmd clean compile`

## Database
- The app stores data in `classbuddy_data/classbuddy.db` under the project directory.
- Schema is loaded from [src/main/resources/schema.sql](src/main/resources/schema.sql) on startup.

If you want a clean DB for testing, close the app and delete the `classbuddy_data/` folder.

## Entry point
- JavaFX main class: `com.classbuddy.App`
- First screen: `src/main/resources/fxml/login.fxml`

## Notes for development
- Controllers typically need data injected after FXML load (see the pattern described in [copilot-instructions.md](copilot-instructions.md)).
- If you hit an NPE in `initialize()`, it usually means required data (user/classroom) wasn’t set yet.
