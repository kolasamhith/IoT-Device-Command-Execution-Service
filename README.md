# IoT Device Command Execution Service

A backend service built using Spring Boot that simulates how IoT systems (such as smart lighting platforms) manage and execute device commands across different zones.

## 🚀 Features

- **Device-Based Command Processing:** Execute commands on specific devices (e.g., `LAMP_101`) grouped by zones (e.g., `STREET_ZONE_A`).
- **Zone-Level Bulk Execution:** Execute commands across an entire zone using a dedicated endpoint.
- **Scheduling Engine:** Supports immediate and scheduled execution of commands using a background worker.
- **Retry Mechanism:** Simulates unreliable device communication by retrying failed commands with a 5-second delay, up to a configurable maximum number of attempts.
- **Status Lifecycle Management:** Tracks command states (`PENDING -> RUNNING -> DONE / FAILED`) for execution monitoring.
- **Logging System:** Maintains execution logs for debugging and tracking command history.
- **Validation Layer:** Uses DTOs with Bean Validation for clean API request validation and error handling.
- **Thread-Safe In-Memory Architecture:** Uses Java concurrent collections (`ConcurrentHashMap`, `CopyOnWriteArrayList`) for safe multi-threaded execution without database dependency.
- **Externalized Configuration:** Retry limits and runtime settings are managed through `application.properties`.

---

## 🛠️ Tech Stack

- **Language:** Java 17
- **Framework:** Spring Boot (Web, Validation)
- **Build Tool:** Maven
- **Architecture:** Layered (Controller, Service, Model, DTO)
- **Testing:** JUnit 5

---

## ⚙️ Getting Started

### Prerequisites

- Java 17 installed on your machine
- Port `8081` available (configured in `application.properties`)

### Running the Application

1. Clone the repository
2. Navigate to the project root directory
3. Start the application using the Maven wrapper

#### Windows

```bash
.\mvnw spring-boot:run
```

#### Mac / Linux

```bash
./mvnw spring-boot:run
```

The server will start at:

```text
http://localhost:8081
```

---

# 📡 API Documentation

## 1. Queue a New Command

Schedules a command for a device. If `scheduledTime` is omitted, it executes immediately.

### Endpoint

```http
POST /commands
```

### Headers

```http
Content-Type: application/json
```

### JSON Payload

- `commandName` (**Required**) — The action to perform (Max 50 chars)
- `deviceId` (**Required**) — The target hardware ID
- `commandType` (**Required**) — Action type (`TURN_ON`, `TURN_OFF`, `DIM`, `BRIGHTEN`, `SET_COLOR`, `REBOOT`)
- `zone` (**Optional**) — The physical grouping (Defaults to `"UNKNOWN_ZONE"`)
- `scheduledTime` (**Optional**) — ISO Date-Time for future execution

### Example Request

```bash
curl -X POST "http://localhost:8081/commands" \
-H "Content-Type: application/json" \
-d '{
  "commandName": "Turn on Street Light",
  "deviceId": "LAMP_101",
  "commandType": "TURN_ON",
  "zone": "STREET_A"
}'
```

---

## 2. View All Commands

Retrieves the current state of the command queue.

### Endpoint

```http
GET /commands
```

### Example Request

```bash
curl http://localhost:8081/commands
```

---

## 3. Retrieve System Audit Logs

Fetches the telemetry and execution history for the entire system.

### Endpoint

```http
GET /commands/logs
```

### Example Request

```bash
curl http://localhost:8081/commands/logs
```

---

## 4. Retrieve Logs for a Specific Command

Fetches the execution history for a single command ID, useful for tracing retries.

### Endpoint

```http
GET /commands/{id}/logs
```

### Example Request

```bash
curl http://localhost:8081/commands/1/logs
```

---

## 5. Force Execute Command (Manual Override)

Bypasses the scheduler and forces an immediate execution attempt.

### Endpoint

```http
POST /commands/{id}/execute
```

### Example Request

```bash
curl -X POST http://localhost:8081/commands/1/execute
```

---

## 6. Execute All Commands for a Zone

Triggers execution for all commands belonging to a specific zone.

### Endpoint

```http
POST /commands/zones/{zone}/execute
```

### Example Request

```bash
curl -X POST http://localhost:8081/commands/zones/STREET_A/execute
```

---

# 🧠 Simulation Logic

To simulate real-world IoT behavior without physical devices:

- A background scheduler runs every 5 seconds to process `PENDING` commands
- Device availability is simulated using the command ID

### Execution Simulation

- **Even IDs** → Successful execution (`DONE`)
- **Odd IDs** → Simulated device failure

### Retry Logic

- Failed commands are retried up to the configured retry limit
- A 5-second delay is applied before each retry
- After max retries, the command is marked as `FAILED`

---

# ✅ Testing

Unit tests are included for the service layer.

### Run Tests

```bash
./mvnw test
```

The test suite validates:

- Retry handling
- Command execution flow
- Zone-level execution
- Scheduler behavior
- Failure state transitions