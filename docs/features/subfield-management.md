---
feature_id: subfield-management
title: Subfield Management
updated: 2026-02-18
---

# Subfield Management

## What it does
Provides retrieval and management of subfield definitions for MARC fields, including both standard subfields and local custom subfields. For standard fields, tenants can append local subfield codes beyond those documented by Library of Congress. For local fields, tenants have full control over all subfield definitions.

## Why it exists
Institutions may use local subfield codes within standard or local MARC fields for institution-specific data beyond what Library of Congress documents. This feature enables discovery of all valid subfield codes and customization of subfield structures while preserving base LOC-documented subfield definitions for standard fields.

## Entry point(s)

### Subfield Retrieval
| Method | Path | Description |
|--------|------|-------------|
| GET | /specification-storage/fields/{fieldId}/subfields | Returns all subfield definitions for a field |

### Subfield Management
| Method | Path | Description |
|--------|------|-------------|
| POST | /specification-storage/fields/{fieldId}/subfields | Creates a new local subfield definition |
| PUT | /specification-storage/subfields/{subfieldId} | Updates an existing subfield definition |
| DELETE | /specification-storage/subfields/{subfieldId} | Deletes a subfield definition |

## Business rules and constraints

### Subfield Retrieval
- Response includes both standard specification subfields and local custom subfields
- Each subfield definition includes:
  - Subfield code (single character: 0-9 or a-z)
  - Label (description)
  - Required/repeatable flags
  - Deprecation status
- Subfields are ordered by code
- All subfields are returned (no pagination)

### Subfield Management
- **Subfield code format**: Must be a single character (0-9 or a-z lowercase)
- **Required fields**: `code` and `label` are mandatory for create/update operations
- **Subfield properties**:
  - `label`: Human-readable description (max 350 characters)
  - `repeatable`: Whether subfield can appear multiple times in the field (default: `true`)
  - `required`: Whether subfield must be present in the field (default: `false`)
  - `deprecated`: Mark subfield as deprecated (default: `false`)
- **Code uniqueness**: Cannot create a local subfield with the same code as an existing subfield in the field
- **Standard vs Local subfields**:
  - **Standard fields**: Cannot modify LOC-documented subfield codes and names, but can APPEND additional local subfield codes not specified by MARC
  - **Local fields**: Can fully customize all subfield codes and properties
  - **System fields**: Cannot modify subfields for system fields (Leader, 001, 005, 006/007/008), except for 245 and 999 which follow standard field rules
- **Reserved subfield codes**: Cannot specify $9 for linkable bib fields as it is reserved for authority linking
- **Sync impact**: Local subfields (appended to standard fields or created for local fields) are removed when specification is synced to defaults
- **Cascading delete**: If parent field is deleted, all subfields (including local) are removed

## API response statuses
- **200 OK**: Subfield definitions retrieved successfully
- **201 Created**: Local subfield successfully created
- **202 Accepted**: Subfield successfully updated
- **204 No Content**: Subfield successfully deleted
- **400 Bad Request**: Invalid field ID or subfield data (malformed code, missing required fields, code already exists)
- **404 Not Found**: Field ID or subfield ID does not exist
- **409 Conflict**: Attempting to modify or delete a standard (non-local) subfield
- **500 Internal Server Error**: Database or system errors

## Configuration
No feature-specific configuration required.

## Dependencies and interactions
- Data sourced from PostgreSQL `subfield` table
- Subfield definitions loaded from bundled specification data during sync
- Publishes events via [Specification Change Events](specification-change-events.md) after subfield changes
- Local subfields are removed during [Specification Synchronization](specification-sync.md) operations
