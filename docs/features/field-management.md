---
feature_id: field-management
title: Field Management
updated: 2026-02-18
---

# Field Management

## What it does
Provides retrieval and management of MARC field definitions for a specification, including both standard fields (from the base specification) and local custom fields. Allows institutions to view all fields and create, update, or delete custom local field definitions for institution-specific cataloging needs.

## Why it exists
Different institutions use local MARC field tags for institution-specific cataloging needs beyond the standard LOC-documented fields. This feature enables discovery of all valid fields and full customization of local field definitions, including indicators and subfields, supporting institution-specific cataloging practices while preserving the base specification.

## Entry point(s)

### Field Retrieval
| Method | Path | Description |
|--------|------|-------------|
| GET | /specification-storage/specifications/{specificationId}/fields | Returns collection of all field definitions for the specification |

### Field Management
| Method | Path | Description |
|--------|------|-------------|
| POST | /specification-storage/specifications/{specificationId}/fields | Creates a new local field definition |
| PUT | /specification-storage/fields/{id} | Updates an existing field definition |
| DELETE | /specification-storage/fields/{id} | Deletes a field definition |

## Business rules and constraints

### Field Retrieval
- Response includes both standard specification fields and local custom fields
- Each field definition includes:
  - Field tag (e.g., "001", "245")
  - Label and help URL
  - Required/repeatable flags
  - Indicator definitions
  - Subfield definitions
  - Deprecation status
- Field order follows MARC field tag numeric ordering
- Pagination is not applied (all fields returned)

### Field Management
- **Field tag format**: Must be a 3-digit numeric string (e.g., "245", "900")
- **Local field ranges**: Typically 9XX, X9X, XX9 (except 490 and 999), or any custom field not documented by Library of Congress
- **Required fields**: `tag` and `label` are mandatory for create/update operations
- **Field properties**:
  - `label`: Human-readable description (max 350 characters)
  - `url`: Optional help URL (max 350 characters, must be valid URL format with http/https protocol)
  - `repeatable`: Whether field can appear multiple times (default: `true`)
  - `required`: Whether field must be present (default: `false`)
  - `deprecated`: Mark field as deprecated (default: `false`)
- **Tag uniqueness**: Cannot create a local field with the same tag as an existing field in the specification
- **Local fields only**: Can only update/delete fields that were created locally; standard specification fields and system fields cannot be modified
- **Full customization**: For local fields, tenants have complete control over all validation rules including indicators and subfields
- **Cascading delete**: Deleting a field removes all associated subfields and indicators
- **Sync impact**: Local fields are removed when specification is synced to defaults

## Error behavior
- **200 OK**: Field definitions retrieved successfully
- **201 Created**: Local field successfully created
- **202 Accepted**: Field successfully updated
- **204 No Content**: Field successfully deleted
- **400 Bad Request**: Invalid specification ID or field data (malformed tag, missing required fields, invalid URL, tag already exists)
- **404 Not Found**: Specification ID or field ID does not exist
- **409 Conflict**: Attempting to modify or delete a standard (non-local) field
- **500 Internal Server Error**: Database or system errors

## Configuration
No feature-specific configuration required.

## Dependencies and interactions
- Data sourced from PostgreSQL tables: `field`, `indicator`, `subfield`
- Field definitions are initially loaded from bundled specification data during sync
- Publishes events via [Specification Change Events](specification-change-events.md) after field changes
- Local fields are removed during [Specification Synchronization](specification-sync.md) operations
