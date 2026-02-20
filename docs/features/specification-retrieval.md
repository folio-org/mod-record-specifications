---
feature_id: specification-retrieval
title: Specification Retrieval
updated: 2026-02-18
---

# Specification Retrieval

## What it does
Provides REST API endpoints to retrieve record specifications, either as a filtered collection or individually by ID. Supports filtering by format family and profile, with optional inclusion of related entities like rules and field definitions.

## Why it exists
Enables clients (primarily quickMARC) to discover available MARC record specifications for bibliographic and authority records and retrieve their complete definitions for validation during cataloging workflows. Different profiles (bibliographic, authority) require different specifications with distinct validation rules and field structures.

## Entry point(s)
| Method | Path | Description |
|--------|------|-------------|
| GET | /specification-storage/specifications | Returns a collection of specifications with optional filters |
| GET | /specification-storage/specifications/{specificationId} | Returns a single specification by ID |

## Business rules and constraints
- **Specification profiles**: 
  - `bibliographic`: MARC bibliographic record specifications (used for bib records in quickMARC)
  - `authority`: MARC authority record specifications (used for authority records in quickMARC)
- **Filtering options**:
  - `family`: Filter by format family (currently only `MARC` is supported)
  - `profile`: Filter by family profile (`bibliographic` or `authority`)
  - `include`: Controls related entities in response (`none` (default), `all`, `fields.required`)
- **Pagination** supported via `limit` and `offset` query parameters on collection endpoint
- **Include parameter behavior**:
  - `include=all`: Response contains all rules and field definitions (complete specification)
  - `include=fields.required`: Response contains only required field definitions
  - `include=none`: Response contains only specification metadata (minimal)
- **Field types in specifications**:
  - **System fields**: Leader, 001, 005, 245, 999, and control fields 006/007/008 (validation rules cannot be changed by tenants, except help URLs)
  - **Standard fields**: LOC-documented fields (tenants can modify some validation rules, append local indicator/subfield codes)
  - **Local fields**: 9XX, X9X, XX9 ranges (except 490 and 999) and custom tenant-defined fields (fully customizable)

## API response statuses
- **400 Bad Request**: Invalid query parameters (e.g., unsupported family or profile values)
- **404 Not Found**: Specification ID does not exist (single retrieval endpoint only)
- **500 Internal Server Error**: Database or system errors

## Configuration
No feature-specific configuration required.

## Dependencies and interactions
- Data sourced from PostgreSQL database tables: `specification`, `specification_rule`, `field`, etc.
- Specifications are initially populated during tenant initialization when `syncSpecifications=true` (tenant attribute)
