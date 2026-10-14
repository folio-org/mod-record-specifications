---
feature_id: specification-sync
title: Specification Synchronization
updated: 2026-10-09
---

# Specification Synchronization

## What it does
Synchronizes a specification's fields, indicators, indicator codes and subfields from the bundled reference data (`spec/marc/*.html`). It runs in one of two modes:
- **Reset** (default): restores the specification to its canonical defaults and removes all local customizations.
- **Preserve local**: brings the specification up to date with the reference data while keeping local customizations, except where the reference data defines the same element itself.

The same machinery also delivers MARC specification corrections to existing tenants automatically on module upgrade (see [Automatic updates on tenant upgrade](#automatic-updates-on-tenant-upgrade)).

## Why it exists
Provides administrators with a reset mechanism to restore specifications to their canonical defaults when local customizations cause issues or need to be discarded. The preserve-local mode lets a specification pick up corrected reference data (for example, after LOC revises a MARC definition) without destroying what the tenant has added or configured. Ensures specifications can be returned to a known good state.

## Entry point(s)
| Method | Path | Description |
|--------|------|-------------|
| POST | /specification-storage/specifications/{specificationId}/sync | Synchronizes a specification; optional query parameter `preserveLocal` (boolean, default `false`) selects the mode |
| POST | /_/tenant | Tenant enable/upgrade: syncs a new tenant's specifications and applies pending specification updates on upgrade |

## Business rules and constraints

### Reset mode
- Sync removes all local customizations for the specification
- Local fields, subfields, indicators, and indicator codes created for this specification are deleted
- Operator edits to standard/system elements (for example a custom field `url`) are reset to the reference values
- Only affects the specified specification; other specifications remain unchanged
- Operation is idempotent: syncing an already-default specification has no effect, and element ids stay the same across syncs

### Preserve-local mode
Reconciles stored data with the reference data instead of replacing it:
- **Reference data wins on a collision.** If the reference data defines a field tag, subfield code, indicator order or indicator code that a local element also uses, the reference definition replaces the local one (including its id)
- **Local elements the reference data does not define are kept.** This covers whole local fields with their indicators, indicator codes and subfields, and local subfields or indicator codes added to a standard field
- **Operator edits on standard/system elements are kept:** a standard field's `url` and `required`, a system field's `url`, and a standard subfield's `required` (the values the scope rules allow to be edited). A custom `url` is dropped if the reference data marks the field deprecated, since deprecated fields have no url
- **Values locked for the element's scope** (label, repeatable, deprecated, and system `required`) always take the reference value
- Standard/system elements that the reference data no longer defines are deleted; local elements are never deleted
- Operation is idempotent: repeating it changes nothing

### Both modes
- Rule enable/disable overrides are not changed by sync; only fields and their children are synchronized
- Specifications are automatically synced during tenant initialization when `syncSpecifications=true` tenant attribute is set (default)

### Automatic updates on tenant upgrade
Fixes to the bundled specification data are registered in `MarcSpecUpdateService.KNOWN_UPDATES`, one entry per ticket, each scoped to a family and optionally one profile (no profile means every profile in the family):
- When an existing tenant is upgraded (`POST /_/tenant`), every specification that has a registered update not yet recorded for it is synced in preserve-local mode, once per specification regardless of how many updates are pending for it
- An update counts as applied per (code, family, profile): a code whose scope is later widened is applied to the newly covered profile, and nothing is applied twice
- Each applied combination is recorded in the tenant's `applied_spec_update` table (key: code, family, profile) together with `applied_date` and `specification_snapshot`, a full copy of the specification (fields, indicators, subfields, indicator codes) taken immediately before the sync, kept as a backup
- A newly created tenant that was synced on creation is recorded as up to date for all registered updates without a second sync; a tenant created with `syncSpecifications=false` is not recorded, so its first upgrade applies them
- If the sync fails, nothing is recorded and the updates are retried on the next upgrade

## API response statuses
- **202 Accepted**: Specification synchronized (the sync completes before the response is sent)
- **400 Bad Request**: Invalid specification ID format, invalid `preserveLocal` value, or the bundled specification data could not be read
- **404 Not Found**: Specification ID does not exist
- **500 Internal Server Error**: Database or system errors during sync

## Configuration
| Variable | Purpose |
|----------|---------|
| syncSpecifications (tenant attribute) | Controls whether specifications are synced when a tenant is first created (default: `true`). Does not affect pending-update handling on upgrade |

## Dependencies and interactions
- Reads default specification data from bundled resources in the module
- Preserve-local reconciliation uses the scope rules of fields and subfields (which values are editable) and the stored `specification_metadata` (stable element ids, and which indicators the reference data knows about)
- Publishes events via [Specification Change Events](specification-change-events.md) after sync completes
- Affects related entities: fields, subfields, indicators, and indicator codes
- Upgrade handling is implemented in `MarcSpecUpdateService`, called from the tenant service; see [MARC specification corrections](../marc-spec-corrections.md) for how to register a new update
