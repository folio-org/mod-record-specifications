---
feature_id: specification-rule-management
title: Specification Rule Management
updated: 2026-02-18
---

# Specification Rule Management

## What it does
Manages validation rules for specifications, allowing administrators to view all available rules for a specification and enable or disable specific rules to control record validation behavior. Automatically tracks creation and modification metadata (user and timestamp) for all rule configuration changes.

## Why it exists
Provides flexibility in record validation by allowing institutions to customize which validation rules are enforced for their cataloging workflows (primarily quickMARC create/derive/edit operations). Different institutions may have different cataloging policies requiring different validation rule sets. Audit trail metadata supports compliance, troubleshooting, and governance requirements for rule configuration changes.

## Entry point(s)
| Method | Path | Description |
|--------|------|-------------|
| GET | /specification-storage/specifications/{specificationId}/rules | Returns all rules for a specification (enabled and disabled) |
| PATCH | /specification-storage/specifications/{specificationId}/rules/{id} | Toggles a rule's enabled state |

## Business rules and constraints
- Each specification has a set of available validation rules (see Available Rules section below)
- Rules can be in one of two states: enabled (actively enforced during validation) or disabled (not enforced)
- **Tenant customization**: Tenants can enable/disable rules at any time without requiring updates to existing records
- PATCH operation accepts a request body with `enabled` boolean field
- Rule toggle is idempotent: setting a rule to its current state succeeds without changes
- All rules are returned by GET endpoint regardless of enabled status
- Rule definitions (name, description, code) cannot be modified; only the enabled state can be toggled
- **Audit metadata**: All rule responses include metadata fields (createdByUserId, createdDate, updatedByUserId, updatedDate)
- Metadata is automatically populated and updated via JPA auditing on all rule state changes
- User ID is captured from the request context; timestamps use server time at the moment of operation
- **Validation behavior**: When a rule is enabled, it determines whether validation errors are:
  - **ERROR** (prevent save): For required fields, non-repeatable constraints, required subfields
  - **WARNING** (allow save): For invalid indicator codes, undefined fields/subfields, deprecated elements

## Available Rules

The following validation rules are available for MARC specifications:

### Field-Level Rules

| Code | Name | Description |
|------|------|-------------|
| `undefinedField` | Undefined Field | Field is not defined but exists in a record |
| `deprecatedField` | Deprecated Field | Field is deprecated but exists in a record |
| `nonRepeatableField` | Non-Repeatable Field | Field is non-repeatable but exists more than once in a record |
| `missingField` | Missing Field | Field is required but does not exist in a record |
| `invalidFieldValue` | Invalid Field Value | Field is flat and contains an invalid value |
| `invalidFieldTag` | Invalid Field Tag | Field has invalid tag |
| `nonRepeatable1XXField` | Non-Repeatable 1XX Field | More than one field from the 1XX group exists in a record |
| `nonRepeatableRequired1XXField` | Non-Repeatable Required 1XX Field | Exactly one field from the 1XX group exists in a record |

### Indicator Rules

| Code | Name | Description |
|------|------|-------------|
| `invalidIndicator` | Invalid Indicator | Field contains specified indicators and codes but with invalid indicator in the record |
| `undefinedIndicatorCode` | Undefined Indicator Code | Indicator code is not defined but exists in a record's field |

### Subfield Rules

| Code | Name | Description |
|------|------|-------------|
| `undefinedSubfield` | Undefined Subfield | Subfield is not defined but exists in a record's field |
| `deprecatedSubfield` | Deprecated Subfield | Subfield is deprecated but exists in a record's field |
| `nonRepeatableSubfield` | Non-Repeatable Subfield | Subfield is non-repeatable but exists more than once in a record's field |
| `missingSubfield` | Missing Subfield | Subfield is required but does not exist in a record's field |
| `invalidSubfieldValue` | Invalid Subfield Value | Subfield value is not valid based on subfield definition |
| `invalidLccnSubfieldValue` | Invalid LCCN Subfield Value | Subfield value is not valid based on LCCN structure |

## API response statuses
- **200 OK**: Rules retrieved successfully (GET endpoint)
- **202 Accepted**: Rule state updated successfully (PATCH endpoint)
- **400 Bad Request**: Invalid request body or parameters
- **404 Not Found**: Specification ID or rule ID does not exist
- **500 Internal Server Error**: Database or system errors

## Configuration
No feature-specific configuration required. Uses standard JPA auditing configuration for metadata tracking.

## Dependencies and interactions
- Publishes events via [Specification Change Events](specification-change-events.md) after rule state changes
- Rule state affects record validation behavior when records are validated against the specification
- Depends on Spring Data JPA auditing (`@EntityListeners(AuditingEntityListener.class)`) for automatic metadata tracking
