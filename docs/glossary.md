# Glossary

This glossary defines key terms used throughout the mod-record-specifications module documentation.

## MARC Concepts

### Field
A discrete unit of information in a MARC record, identified by a unique three-character tag and encompassing specific data elements.

### Control Field
A specialized field (00X) containing coded information or control numbers, characterized by the absence of indicators and subfield codes.

### Data Field
Any field excluding 00X, structured with two indicators and subfields to contain specific data.

### Field Value
The substantive content contained within a given field.

### Tag
A three-digit numeric identifier designating a specific field within a MARC record.

### Indicator
A single-character value in a data field that provides supplementary information about the field's content or processing requirements.

### Indicator Order
The sequential position of an indicator within a data field (position 1 or 2).

### Indicator Code
A single alphanumeric character or blank (represented as `#`) used within an indicator to denote specific interpretations or processing instructions for the field's data.

### Subfield
A distinct data element within a data field.

### Subfield Code
A single-character identifier (0-9 or a-z) used to demarcate and identify subfields within a data field.

### Subfield Value
The specific data content associated with a particular subfield code within a data field.

## Specification Concepts

### Specification
A comprehensive set of rules and definitions describing the MARC format, applicable for MARC record validation. Contains field definitions, indicator definitions, subfield definitions, and validation rules.

### Specification Rule
A defined criterion used in record validation, comprising a name, description, and rule code, with optional activation status. Can be enabled or disabled to control validation behavior.

### Field Definition
A detailed description of a MARC field used in record validation, including indicator and subfield definitions, as well as repeatability, requirement, and deprecation status.

### Indicator Definition
A specification of a MARC indicator used in record validation, detailing indicator order and valid indicator codes.

### Indicator Code Definition
A description of permissible indicator codes for a specific indicator, including the code value and its meaning.

### Subfield Definition
A specification of valid subfield codes for a particular field, including repeatability, requirement, and deprecation status.

### Scope
A parameter that defines the applicability boundaries for various definitions within the specification.

## Field Type Hierarchy

### System Field
Fields managed entirely by the system with minimal tenant customization allowed. Includes Leader, 001, 005, 006, 007, 008, 245 (bibliographic), and 999. Validation rules cannot be changed by tenants, except for help URLs. For 245 and 999, indicators and subfields follow standard field rules.

### Standard Field
LOC-documented non-local fields where tenants can modify certain aspects while preserving LOC-documented codes. Tenants cannot modify LOC-documented indicator codes or subfield codes but can APPEND additional local codes. Can modify required/repeatable settings and help URLs.

### Local Field
Fields in local ranges (9XX, X9X, XX9 except 490 and 999) or custom non-LOC fields. Tenants have full control over all validation rules, indicators, subfields, and codes.

## Validation Concepts

### WARN (Warning)
A validation severity level that allows record saving with a warning message. Used for non-critical violations such as invalid indicator codes, invalid subfield codes, undefined fields, or deprecated field/subfield usage.

### ERROR (PREVENT SAVE)
A validation severity level that blocks record saving. Used for critical violations such as missing required fields/subfields or non-repeatable fields/subfields appearing multiple times.

### APPEND Pattern
The capability for tenants to add additional local codes (indicator codes or subfield codes) to standard fields without modifying the LOC-documented codes. Appended codes are removed during specification synchronization.

## Integration Concepts

### Specification Update Event
A Kafka event published to the `specification-storage.specification.update` topic to request specification updates. Consumed by the module for event-driven synchronization.

### Specification Change Event
A Kafka event published to the `specification-storage.specification.updated` topic when specification data is modified. Enables downstream systems to react to specification changes.

### Tenant-Scoped Topic
A Kafka topic that includes tenant identifier in its name pattern, enabling multi-tenant message processing. Format: `{environment}.{tenant}.{topic-name}`.

## Module Components

### mod-record-specifications-server
The REST API and Kafka integration component providing specification management endpoints and event processing.

### mod-record-specifications-validator
A standalone library for programmatic MARC record validation against specification definitions. Used by clients such as quickMARC.

### mod-record-specifications-dto
Shared data transfer objects (DTOs) used across module components.

## Special Designations

### Reserved Subfield
Subfield codes with special system purposes that cannot be customized. Example: `$9` is reserved for authority linking on linkable bibliographic fields.

### Undefined Indicator
An indicator value that is blank or not specified, represented as `#` in API responses and validation.

### quickMARC
The primary FOLIO cataloging interface for creating, deriving, and editing MARC records. The main consumer of mod-record-specifications validation capabilities.

### LOC (Library of Congress)
The authoritative source for standard MARC field, indicator, and subfield definitions. LOC-documented codes cannot be modified by tenants in standard fields.
