---
feature_id: specification-sync
title: Specification Synchronization
updated: 2026-02-18
---

# Specification Synchronization

## What it does
Resets a specification to its default state by synchronizing it from the bundled reference data, removing all local customizations including custom fields, subfields, indicators, and rule overrides.

## Why it exists
Provides administrators with a reset mechanism to restore specifications to their canonical defaults when local customizations cause issues or need to be discarded. Ensures specifications can be returned to a known good state.

## Entry point(s)
| Method | Path | Description |
|--------|------|-------------|
| POST | /specification-storage/specifications/{specificationId}/sync | Synchronizes specification to defaults |

## Business rules and constraints
- Sync operation removes all local customizations for the specification
- Local fields, subfields, indicators, and indicator codes created for this specification are deleted
- Rule enable/disable overrides are reset to their default states
- Only affects the specified specification; other specifications remain unchanged
- Operation is idempotent: syncing an already-default specification has no effect
- Specifications are automatically synced during tenant initialization when `syncSpecifications=true` tenant attribute is set (default)

## Error behavior
- **202 Accepted**: Operation queued successfully (sync happens asynchronously)
- **400 Bad Request**: Invalid specification ID format
- **404 Not Found**: Specification ID does not exist
- **500 Internal Server Error**: Database or system errors during sync

## Configuration
| Variable | Purpose |
|----------|---------|
| syncSpecifications (tenant attribute) | Controls whether specifications are synced during tenant initialization (default: `true`) |

## Dependencies and interactions
- Reads default specification data from bundled resources in the module
- Publishes events via [Specification Change Events](specification-change-events.md) after sync completes
- Affects related entities: fields, subfields, indicators, indicator codes, and specification rules
