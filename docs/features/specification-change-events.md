---
feature_id: specification-change-events
title: Specification Change Events
updated: 2026-02-18
---

# Specification Change Events

## What it does
Publishes event notifications to Kafka whenever specification data is modified, enabling other systems and services to react to specification changes in near real-time. Events are published for all types of specification changes including field modifications, subfield updates, indicator changes, rule state changes, and synchronization operations.

## Why it exists
In a distributed FOLIO environment, multiple services and instances may depend on specification data for validation and cataloging operations. Publishing change events enables event-driven synchronization, cache invalidation, and workflow triggers across the system without requiring polling or tight coupling between services.

## Entry point(s)
| Type | Topic | Description |
|------|-------|-------------|
| Kafka Producer | specification-storage.specification.updated | Publishes specification change events after modifications |

## Business rules and constraints
- Events are published after successful database commits (not before)
- Event payload includes:
  - Specification ID (UUID)
  - Event type (if applicable)
  - Timestamp
- Publishing is idempotent (Kafka producer configured with `enable.idempotence: true`)
- Topic is tenant-scoped following pattern: `{environment}.{tenant}.specification-storage.specification.updated`
- Events are published for these operations:
  - Field create, update, delete
  - Subfield create, update, delete
  - Indicator create, update
  - Indicator code create, update, delete
  - Validation rule enable/disable
  - Specification synchronization (full reset)
  - Specification update processing (via Kafka consumer)

## Error behavior
- If event publishing fails, the database transaction is not rolled back (eventual consistency model)
- Kafka producer retries are configured (max 5 retries)
- Failed events may result in downstream systems having stale data until next change event
- Database changes persist regardless of event publishing success

## Configuration
| Variable | Purpose |
|----------|---------|
| KAFKA_HOST | Kafka broker hostname (default: kafka) |
| KAFKA_PORT | Kafka broker port (default: 9092) |
| KAFKA_SECURITY_PROTOCOL | Security protocol for Kafka connection (default: PLAINTEXT) |
| ENV | Environment name for topic scoping (default: folio) |

## Dependencies and interactions
- Kafka cluster must be available for event publishing
- Topic `specification-storage.specification.updated` must exist or auto-creation must be enabled
- Events consumed by:
  - Other mod-record-specifications instances in the cluster (for cache invalidation)
  - Downstream FOLIO modules that depend on specification data
  - External systems integrated with FOLIO
- Referenced by:
  - [Field Management](field-management.md)
  - [Subfield Management](subfield-management.md)
  - [Indicator Management](indicator-management.md)
  - [Specification Rule Management](specification-rule-management.md)
  - [Specification Synchronization](specification-sync.md)
  - [Specification Update Processing](specification-update-processing.md)
