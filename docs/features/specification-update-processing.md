---
feature_id: specification-update-processing
title: Specification Update Processing
updated: 2026-02-18
---

# Specification Update Processing

## What it does
Processes specification update requests received via Kafka topic, enabling external systems to trigger updates to specification definitions (currently supports subfield updates) in an event-driven manner.

## Why it exists
Enables integration with external specification management systems or centralized configuration services that need to push specification changes to deployed instances asynchronously. Supports event-driven architecture for specification synchronization across multiple environments.

## Entry point(s)
| Type | Topic | Description |
|------|-------|-------------|
| Kafka Consumer | specification-storage.specification.update | Processes specification update request events |

### Event processing
- **When processed**: On each message arrival, processed sequentially within consumer group
- **Event types handled**: 
  - `SUBFIELD`: Updates subfield definitions for a target field in specifications matching family and profile
- **Processing behavior**: 
  - Locates specifications matching the event's family and profile
  - Finds target field by tag within matched specifications
  - Creates or updates subfield based on event payload
  - Publishes specification updated event after successful processing

## Business rules and constraints
- **Topic pattern**: `{environment}.{tenant}.specification-storage.specification.update` (e.g., `folio.diku.specification-storage.specification.update`)
- **Consumer group**: `{environment}-mod-record-specification-group`
- **Concurrency**: Single consumer per partition (concurrency: 1)
- **Offset reset**: Processes from earliest available message on first startup
- **Event validation**: Events must include valid family, profile, and definition type
- **Idempotency**: Subfield updates are idempotent (reprocessing same event produces same result)
- **Tenant context**: Processing executes within the tenant context extracted from message headers

## Error behavior
- Invalid event structure: Logged and skipped (message is consumed but not processed)
- Unknown definition type: Throws `IllegalArgumentException`, message remains uncommitted
- Specification or field not found: Update is skipped for that specification/field
- Processing failures: Message remains uncommitted and will be retried based on Kafka consumer configuration

## Configuration
| Variable | Purpose |
|----------|---------|
| folio.environment | Environment name used in topic pattern matching |
| KAFKA_HOST | Kafka broker hostname (default: `kafka`) |
| KAFKA_PORT | Kafka broker port (default: `9092`) |
| KAFKA_SECURITY_PROTOCOL | Kafka security protocol (default: `PLAINTEXT`) |

## Dependencies and interactions
- Consumes from Kafka topic `specification-storage.specification.update` (tenant-scoped)
- Publishes events via [Specification Change Events](specification-change-events.md) after successful processing
- Modifies specification field structures (currently subfield definitions)
- Requires valid tenant context in message headers for multi-tenant processing
