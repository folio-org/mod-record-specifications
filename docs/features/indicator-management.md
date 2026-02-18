---
feature_id: indicator-management
title: Indicator Management
updated: 2026-02-18
---

# Indicator Management

## What it does
Provides retrieval and management of MARC field indicators and their valid code values, allowing institutions to view all indicators and create/update custom local indicator definitions and append local indicator codes to both standard and local fields. Enables discovery of indicator structures and customization for institution-specific cataloging needs.

## Why it exists
MARC data fields use indicators (positions 1 and 2) with specific code values defined by Library of Congress. This feature enables discovery of valid indicator positions and code values, while also allowing institutions to define local indicators for custom fields and append additional local indicator codes to standard fields for institution-specific cataloging practices while preserving base LOC-documented definitions.

## Entry point(s)

### Indicator Retrieval
| Method | Path | Description |
|--------|------|-------------|
| GET | /specification-storage/fields/{fieldId}/indicators | Returns all indicator definitions for a field |

### Indicator Management
| Method | Path | Description |
|--------|------|-------------|
| POST | /specification-storage/fields/{fieldId}/indicators | Creates a new local indicator definition |
| PUT | /specification-storage/indicators/{indicatorId} | Updates an existing indicator definition |

### Indicator Code Management  
| Method | Path | Description |
|--------|------|-------------|
| GET | /specification-storage/indicators/{indicatorId}/indicator-codes | Returns all code definitions for an indicator |
| POST | /specification-storage/indicators/{indicatorId}/indicator-codes | Creates a new local code definition |
| PUT | /specification-storage/indicator-codes/{indicatorCodeId} | Updates an existing code definition |
| DELETE | /specification-storage/indicator-codes/{indicatorCodeId} | Deletes a code definition |

## Business rules and constraints

### Indicator Retrieval
- Response includes both standard specification indicators and local custom indicators
- Each indicator definition includes:
  - Order (position): 1 or 2
  - Label (description)
  - Collection of valid indicator codes with their labels
- MARC fields can have 0, 1, or 2 indicators
- Indicators are ordered by position (order 1 before order 2)
- All indicators are returned (no pagination)

### Indicator Definitions
- **Indicator order**: Must be 1 or 2 (representing first or second indicator position)
- **Required fields**: `order` and `label` are mandatory for create/update operations
- **Indicator properties**:
  - `order`: Indicator position (1 or 2)
  - `label`: Human-readable description (max 350 characters)
- **Order uniqueness**: Cannot create an indicator with the same order as an existing indicator in the field (max 2 indicators per field)
- **Local indicators only**: Can only update indicators that were created locally; standard specification indicators cannot be modified
- **No delete endpoint**: Indicators cannot be deleted directly; delete the parent field to remove all indicators

### Indicator Code Definitions
- **Code format**: Must be a single character (0-9, a-z lowercase, or # for blank/undefined)
- **Required fields**: `code` and `label` are mandatory for create/update operations
- **Code properties**:
  - `code`: Single character indicator value
  - `label`: Human-readable description of the code's meaning (max 350 characters)
  - `deprecated`: Mark code as deprecated (default: `false`)
- **Code uniqueness**: Cannot create a code with the same value as an existing code in the indicator
- **Standard vs Local codes**:
  - **Standard fields**: Cannot modify LOC-documented indicator codes, but can APPEND additional local indicator codes not specified by MARC
  - **Local fields**: Can fully customize all indicator codes
  - **System fields** (Leader, 001, 005, 245, 999, 006/007/008): Cannot modify indicator codes
- **Undefined value representation**: Express undefined/blank indicator as `#` in API responses

### General Constraints
- **Sync impact**: Local indicators and appended indicator codes are removed when specification is synced to defaults
- **Cascading delete**: If parent field is deleted, all indicators and their codes are removed
- **Cascading operations**: If parent indicator is deleted (via field deletion), all indicator codes are removed

## Error behavior
- **200 OK**: Indicator or indicator code definitions retrieved successfully
- **201 Created**: Local indicator or indicator code successfully created
- **202 Accepted**: Indicator or indicator code successfully updated
- **204 No Content**: Indicator code successfully deleted (indicators cannot be deleted directly)
- **400 Bad Request**: Invalid field ID or indicator/code data (malformed code, missing required fields, order/code already exists, order out of range)
- **404 Not Found**: Field ID, indicator ID, or indicator code ID does not exist
- **409 Conflict**: Attempting to modify a standard (non-local) indicator/code, or order/code conflict
- **500 Internal Server Error**: Database or system errors

## Configuration
No feature-specific configuration required.

## Dependencies and interactions
- Data sourced from PostgreSQL `indicator` and `indicator_code` tables
- Indicator definitions loaded from bundled specification data during sync
- Publishes events via [Specification Change Events](specification-change-events.md) after indicator or code changes
- Local indicators and codes are removed during [Specification Synchronization](specification-sync.md) operations
