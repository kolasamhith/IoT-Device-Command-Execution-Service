# IoT Device Command Execution Service

A backend service built using Spring Boot that simulates how IoT systems (such as smart lighting platforms) manage and execute device commands across different zones.

## 🚀 Features

* Device-Based Command Processing: Execute commands on specific devices (e.g., `LAMP_101`) grouped by zones (e.g., `STREET_ZONE_A`).
* Scheduling Engine: Supports immediate and scheduled execution of commands using a background worker.
* Retry Mechanism: Simulates unreliable device communication by retrying failed commands with a 5-second delay, up to a maximum of 2 attempts.
* Status Lifecycle Management: Tracks command states (`PENDING -> RUNNING -> DONE / FAILED`) for execution monitoring.
* Logging System: Maintains execution logs for debugging and tracking command history.
* In-Memory Architecture: Uses Java data structures (`List<T>`) for fast execution without database dependency.

## 🛠️ Tech Stack

* Language: Java 17
* Framework: Spring Boot (Web, Validation)
* Build Tool: Maven
* Architecture: Layered (Controller, Service, Model)

## ⚙️ Getting Started

### Prerequisites
* Java 17 installed on your machine.
* Port `8081` available (configured in `application.properties`).

### Running the Application
1. Clone the repository.
2. Navigate to the project root directory.
3. Start the application using the Maven wrapper:

Windows:
Bash
.\mvnw spring-boot:run
Mac / Linux:

Bash
./mvnw spring-boot:run
The server will start at http://localhost:8081.

📡 API Documentation
1. Queue a New Command
Schedules a command for a device. If scheduledTime is omitted, it executes immediately.

Endpoint: POST /commands

Parameters:

commandName (Required): The action to perform (e.g., "TurnOn", "SetDimming50").

deviceId (Required): The target hardware ID.

zone (Optional): The physical grouping (Defaults to "UNKNOWN_ZONE").

scheduledTime (Optional): ISO Date-Time for future execution (e.g., 2026-03-28T22:35:00).

Example Request:

Bash
curl -X POST "http://localhost:8081/commands?commandName=TurnOn&deviceId=LAMP_101&zone=STREET_A"
2. View All Commands
Retrieves the current state of the command queue.

Endpoint: GET /commands

Example Request:

Bash
curl http://localhost:8081/commands
3. Retrieve System Audit Logs
Fetches the telemetry and execution history for the entire system.

Endpoint: GET /commands/logs

Example Request:

Bash
curl http://localhost:8081/commands/logs
4. Retrieve Logs for a Specific Command
Fetches the execution history for a single command ID, useful for tracing retries.

Endpoint: GET /commands/{id}/logs

Example Request:

Bash
curl http://localhost:8081/commands/1/logs
5. Force Execute Command (Manual Override)
Bypasses the scheduler and forces an immediate execution attempt.

Endpoint: POST /commands/{id}/execute

Example Request:

Bash
curl -X POST http://localhost:8081/commands/1/execute
🧠 Simulation Logic
To simulate real-world IoT behavior without physical devices:

A background scheduler runs every 5 seconds to process pending commands.

Device availability is simulated using command ID:

Even IDs -> Successful execution (DONE)

Odd IDs -> Simulated device failure

Retry Logic:

Failed commands are retried up to 2 times

A 5-second delay is applied before each retry

After max retries, the command is marked as FAILED
