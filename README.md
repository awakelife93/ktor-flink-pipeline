# Ktor Flink Pipeline

### Modern streaming data pipeline built with Ktor, Kafka, and Apache Flink in Kotlin.

> A multi-module streaming data pipeline using Ktor, Kafka and Flink for extensible real-time data processing.

## Project Structure

```
data-pipeline/
├── gradle/
│   └── libs.versions.toml      # Version Catalog (Centralized version management)
├── data-pipeline-api/          # Ktor + Kafka Producer module
│   ├── build.gradle.kts
│   ├── src/main/kotlin/
│   │   └── com/example/api/
│   │       ├── Main.kt
│   │       ├── dto/
│   │       ├── module/
│   │       ├── route/
│   │       └── service/
│   └── src/main/resources/
├── data-pipeline-flink/        # Flink Stream Processor module
│   ├── build.gradle.kts
│   ├── src/main/kotlin/
│   │   └── com/example/flink/
│   │       ├── Main.kt
│   │       ├── dto/
│   │       ├── factory/
│   │       ├── job/
│   │       ├── module/
│   │       └── service/
│   └── src/main/resources/
├── docker-compose.yml          # Infrastructure services
└── setup.sh                   # Quick setup script
```

## Architecture Overview

This pipeline processes data through the following flow:

1. **API Module** receives data events via REST API and publishes to Kafka
2. **Kafka** acts as a message broker for stream processing
3. **Flink Module** consumes data streams, performs windowed aggregations, and generates alerts
4. **Monitoring Tools** provide real-time visibility into the system

## Module Description

### data-pipeline-api

**Tech Stack**: Ktor, Kafka Producer
**Port**: 8085

**Responsibilities**:

- Collect data events via REST API endpoints
- Publish events to Kafka topics
- Provide automatic data generation for testing (currently log events)

### data-pipeline-flink

**Tech Stack**: Apache Flink, Kafka Consumer (JDK 11 optimized for Docker)

**Responsibilities**:

- Consume data streams from Kafka in real-time
- Perform windowed aggregation and analytics
- Generate alerts based on configurable thresholds
- Support extensible data processing patterns

## Running Options

This project supports two deployment environments:

### 1. Flink Local Application Environment

For local development and testing with direct IDE execution.

#### Start Infrastructure Services

**Option A: Using setup script (Recommended)**

```bash
chmod +x setup.sh
./setup.sh
```

**Option B: Using docker-compose directly**

```bash
# Basic setup with single TaskManager
docker-compose up -d

# Or with multiple TaskManagers for better performance
docker-compose up -d --scale flink-taskmanager=2 --build
```

#### Start Application Modules

**Start API Module**

```bash
./gradlew :data-pipeline-api:run
```

**Start Flink Module (in separate terminal)**

```bash
./gradlew :data-pipeline-flink:run
```

The Flink job dashboard will be available at http://localhost:8081

**Configuration**:

- Uses `localhost:9092` for Kafka connection (API Module)
- Direct JVM execution with local Flink runtime

### 2. Flink Docker Environment

For production-like deployment with JAR submission to Flink cluster.

#### Build JAR and Submit to Flink Cluster

**Build the shadowJar**

```bash
./gradlew :data-pipeline-flink:shadowJar
```

**Start Infrastructure (if not already running)**

```bash
docker-compose up -d --scale flink-taskmanager=2 --build
```

**Submit JAR to Flink JobManager**

```bash
# Upload and submit the JAR via Flink Web UI
# Navigate to http://localhost:8081
# Go to "Submit New Job" and upload the JAR file located at:
# data-pipeline-flink/build/libs/data-pipeline-flink-all.jar
```

**Start API Module**

```bash
./gradlew :data-pipeline-api:run
```

**Configuration**:

- Uses `ktor-flink-pipeline-kafka:29092` for Kafka connection (Docker network)
- JAR execution within Flink TaskManager containers
- JDK 11 optimized runtime for Docker deployment

**Monitor via Docker TaskManager**:

View running tasks and resource utilization:

```bash
# Check TaskManager containers
docker ps --filter "name=taskmanager"

# View TaskManager logs
docker logs ktor-flink-pipeline-taskmanager-1
docker logs ktor-flink-pipeline-taskmanager-2

# Monitor resource usage
docker stats --filter "name=taskmanager"
```

Access Flink Web UI at http://localhost:8081 to monitor job execution, checkpoints, and TaskManager status.

## API Reference

### Send Data Events

**POST /logs**

Send individual or batch data events to the system.

```bash
curl -X POST http://localhost:8085/logs \
  -H "Content-Type: application/json" \
  -d '{
    "events": [
      {
        "level": "ERROR",
        "service": "auth-service",
        "message": "Authentication failed",
        "timestamp": 1640995200000
      }
    ]
  }'
```

### Control Auto Data Generation

**Start automatic data generation**

```bash
curl -X POST http://localhost:8085/start
```

This will start generating random log events continuously. You'll see real-time stream processing output like:

```
TotalLogCountsSink:8> LogCount(level=INFO, count=3, timestamp=1759052980437)
TotalLogCountsSink:4> LogCount(level=WARN, count=3, timestamp=1759052981070)
TotalLogCountsSink:8> LogCount(level=INFO, count=4, timestamp=1759052982422)
TotalLogCountsSink:2> LogCount(level=ERROR, count=2, timestamp=1759052982998)
TotalLogCountsSink:8> LogCount(level=INFO, count=5, timestamp=1759052985764)
TotalLogCountsSink:4> LogCount(level=WARN, count=4, timestamp=1759052988453)
TotalLogCountsSink:4> LogCount(level=WARN, count=5, timestamp=1759052991409)
TotalLogCountsSink:8> LogCount(level=INFO, count=6, timestamp=1759052992237)
TotalLogCountsSink:4> LogCount(level=WARN, count=6, timestamp=1759052994323)
TotalLogCountsSink:4> LogCount(level=WARN, count=7, timestamp=1759052995748)
TotalLogCountsSink:2> LogCount(level=ERROR, count=3, timestamp=1759052997628)
TotalLogCountsSink:2> LogCount(level=ERROR, count=4, timestamp=1759052999421)
ProcessingTimeErrorAlertSink:2> LogCount(level=ERROR, count=4, timestamp=1759052999421)
TotalLogCountsSink:4> LogCount(level=WARN, count=8, timestamp=1759053001275)
TotalLogCountsSink:8> LogCount(level=INFO, count=7, timestamp=1759053002610)
TotalLogCountsSink:2> LogCount(level=ERROR, count=5, timestamp=1759053005309)
...
```

The numbers (e.g., `TotalLogCountsSink:4>`, `TotalLogCountsSink:8>`, `ProcessingTimeErrorAlertSink:2>`) represent the Flink subtask indices,
showing parallel processing across
multiple subtasks. Each log level is processed and aggregated separately, with running counts that increment as new events are processed.

**Stop automatic data generation**

```bash
curl -X POST http://localhost:8085/stop
```

## Development

### Build Commands

```bash
# Build all modules
./gradlew build

# Build specific modules
./gradlew :data-pipeline-api:build
./gradlew :data-pipeline-flink:build

# Build shadowJar for Docker deployment
./gradlew :data-pipeline-flink:shadowJar

# Run specific modules (Local Environment)
./gradlew :data-pipeline-api:run
./gradlew :data-pipeline-flink:run
```

### JDK Version Strategy

This project uses different JDK versions for different modules to optimize performance and compatibility:

**API Module (data-pipeline-api)**:

- **JDK 21**: Uses the latest features and performance improvements for web server operations
- Leverages modern Kotlin coroutines and Ktor framework optimizations
- Better memory management and throughput for HTTP request handling

**Flink Module (data-pipeline-flink)**:

- **JDK 11**: Optimized for Docker containerization and Apache Flink compatibility
- Ensures stable runtime within Flink TaskManager containers
- Reduced memory footprint and faster container startup times
- Maintains compatibility with Flink's official Docker images

This dual-JDK approach provides the best performance characteristics for each module's specific use case while maintaining deployment
flexibility.

### Configuration Management

**Environment-specific Kafka Configuration**:

This project uses **automatic environment detection** to select the appropriate Kafka configuration:

```kotlin
// KafkaConfigSelector.kt - Auto-detection based on execution mode
object KafkaConfigSelector {
	val isRunningFromJar = this::class.java.protectionDomain
		.codeSource.location.toString().contains(".jar")

	val kafkaBootstrapServers = if (isRunningFromJar) {
		"ktor-flink-pipeline-kafka:29092"  // shadowJar execution (Flink cluster)
	} else {
		"localhost:9092"  // IDE execution (local development)
	}
}
```

**Automatic Configuration Selection**:

- **Local Environment**: Automatically uses `localhost:9092` when running from IDE
- **Docker Environment**: Automatically uses `ktor-flink-pipeline-kafka:29092` when running from shadowJar

**Implementation**:

```kotlin
// Main.kt
private val KAFKA_BOOTSTRAP_SERVERS = KafkaConfigSelector.kafkaBootstrapServers
```

**Execution Mode Detection**:
You can verify which mode is detected by checking the application logs:

```bash
# For local execution (./gradlew run)
Execution mode - JAR: false, Kafka: localhost:9092

# For Docker execution (shadowJar upload to Flink)
Execution mode - JAR: true, Kafka: ktor-flink-pipeline-kafka:29092
```

**View Logs in Different Environments**:

- **Local Environment**: Check IDE console output
- **Docker Environment**: Check Flink TaskManager logs:
	```bash
	docker logs ktor-flink-pipeline-taskmanager-1 -f
	```
	Or view logs in Flink Web UI → Running Jobs → Logs tab

**JDK Version**:

- Flink module uses JDK 11 for Docker optimization and better container performance

### Dependency Management

This project uses Gradle Version Catalogs for centralized dependency management. All version information is maintained in
`gradle/libs.versions.toml`.

**Major Library Versions**:

- Kotlin: 2.2.20
- Ktor: 3.3.0
- Apache Kafka: 4.1.0
- Apache Flink: 1.20.2
- Jackson: 2.18.2
- Target JDK: 11 (Flink Module)

**Update Dependencies**:

```toml
[versions]
kotlin = "2.2.20"        # Update Kotlin version
ktor = "3.3.0"           # Update Ktor version
kafka = "4.1.0"          # Update Kafka version
```

## Monitoring and Operations

### Kafka Monitoring

**Kafka UI** - http://localhost:8080

Comprehensive Kafka cluster management with features including:

- Topic management and message browsing
- Consumer group monitoring and lag tracking
- Broker health and partition information
- Real-time message publishing and consuming

### Flink Monitoring

**Flink Dashboard** - http://localhost:8081

Job execution monitoring and management with features including:

- Real-time job status and performance metrics
- Task manager resource utilization
- Checkpoint and savepoint management
- Job execution timeline and failure analysis
- JAR upload and job submission interface

**Docker TaskManager Monitoring**:

```bash
# Monitor TaskManager containers
docker stats --filter "name=taskmanager"

# View TaskManager logs
docker logs ktor-flink-pipeline-taskmanager-1 -f

# Check TaskManager resource allocation
docker exec ktor-flink-pipeline-taskmanager-1 cat /proc/meminfo
```

## Extensibility & Future Implementations

### Current Implementation: Log Processing & Real-time Analytics

This pipeline currently demonstrates its capabilities through comprehensive log event processing:

**Features:**

- **REST API**: Receives log events in JSON format with flexible batch processing
- **Real-time Stream Processing**: Events are classified by level (INFO, WARN, ERROR, DEBUG) and processed in parallel
- **Running Aggregations**: Maintains continuous counts for each log level across multiple Flink task slots
- **Windowed Analytics**: 5-minute tumbling windows for time-based trend analysis
- **Alert System**: Configurable error threshold alerting (currently set to 50+ ERROR logs in 5 minutes)
- **Parallel Processing**: Automatic load balancing across multiple Flink TaskManager instances
- **Dual Deployment**: Support for both local development and containerized production environments

**Data Flow:**

1. API generates random log events at configurable intervals
2. Events are published to Kafka `logs` topic
3. Flink consumes events and performs keyed aggregations by log level
4. Real-time counts are output to console with task slot identification
5. Time-windowed aggregations trigger alerts when thresholds are exceeded

**Observable Outputs:**

- Live running counts showing cumulative log statistics per level
- Task slot distribution demonstrating parallel processing capabilities
- Alert notifications for critical error patterns

## Configuration

### Service Endpoints

| Service         | URL                   | Purpose                       |
|-----------------|-----------------------|-------------------------------|
| API Server      | http://localhost:8085 | Data ingestion REST API       |
| Kafka UI        | http://localhost:8080 | Kafka management interface    |
| Flink Dashboard | http://localhost:8081 | Job monitoring and management |
| Kafka Bootstrap | localhost:9092        | Kafka broker connection       |

### Environment Configuration

| Environment | Kafka Connection                | Execution Mode |
|-------------|---------------------------------|----------------|
| Local       | localhost:9092                  | Direct JVM     |
| Docker      | ktor-flink-pipeline-kafka:29092 | Container JAR  |

## Author

**Hyunwoo Park**
