---
feature_id: record-validation
title: Record Validation Against Specifications
updated: 2026-02-18
---

# Record Validation Against Specifications

## What it does
Provides programmatic validation of MARC bibliographic and authority records against specification definitions, checking records for compliance with field structures, indicator values, subfield requirements, and all enabled validation rules. Returns detailed validation errors with severity levels (WARN or ERROR) that determine whether the record can be saved or must be corrected.

## Why it exists
Enables applications (primarily quickMARC) to validate MARC records during cataloging workflows (create/derive/edit operations), ensuring records conform to MARC standards and institution-specific cataloging rules. Supports quality control by catching validation errors early while allowing catalogers to proceed with warnings when appropriate.

## Entry point(s)
This is a **library feature** provided by the `mod-record-specifications-validator` module, not a REST API endpoint. Applications integrate the validator programmatically:

```java
SpecificationValidator validator = new MarcSpecificationValidator(translationProvider, converter);
List<ValidationError> errors = validator.validate(marcRecord, specification);
```

## Business rules and constraints
- **Supported formats**: MARC bibliographic and authority records (MARC4J `Record` objects or custom `MarcRecord` model)
- **Primary use case**: Validation for quickMARC create/derive/edit workflows
- **Validation scope**: Only enabled rules for the specification are enforced (disabled rules are skipped)
- **Validation behavior**:
  - **PREVENT SAVE (ERROR severity)**: Critical violations that prevent record from being saved
    - Required field is missing
    - Non-repeatable field appears multiple times
    - Required subfield is missing
    - Non-repeatable subfield appears multiple times
  - **WARN (WARNING severity)**: Non-critical violations that allow saving with warning
    - Invalid indicator code (not specified in rules)
    - Invalid subfield code (not specified in rules)
    - Undefined field (field not in validation list)
    - Deprecated field/subfield usage
- **Validation categories**:
  - **Field-level validation**: Field presence, repeatability, deprecation, tag validity, 1XX field constraints
  - **Indicator validation**: Invalid indicator values, undefined indicator codes
  - **Subfield validation**: Subfield presence, repeatability, deprecation, undefined subfields, LCCN structure validation
- **System fields** (validation rules cannot be changed): Leader, 001, 005, 245 (bib), 999, and control fields 006/007/008
- **Validation errors include**:
  - `path`: Location in the record (e.g., `001`, `245$a`, `100[1]$d`)
  - `severity`: ERROR (prevent save) or WARNING (allow save with warning)
  - `definitionType`: Type of definition violated (FIELD, INDICATOR, SUBFIELD)
  - `definitionId`: UUID of the violated definition
  - `ruleCode`: Code of the violated rule (e.g., `undefinedField`, `missingSubfield`)
  - `message`: Localized human-readable error message
- **Custom converters**: Applications can provide custom converters to validate non-standard MARC representations
- **Translation support**: Error messages support internationalization via `TranslationProvider`

## Available Validation Rules

The validator enforces the following rules when enabled in the specification (see [Specification Rule Management](specification-rule-management.md) for complete rule descriptions):

### Field-Level Rules (8)
- `undefinedField`, `deprecatedField`, `nonRepeatableField`, `missingField`
- `invalidFieldValue`, `invalidFieldTag`
- `nonRepeatable1XXField`, `nonRepeatableRequired1XXField`

### Indicator Rules (2)
- `invalidIndicator`, `undefinedIndicatorCode`

### Subfield Rules (6)
- `undefinedSubfield`, `deprecatedSubfield`, `nonRepeatableSubfield`
- `missingSubfield`, `invalidSubfieldValue`, `invalidLccnSubfieldValue`

## Error behavior
- **Invalid input**: Throws `IllegalArgumentException` if record type is unsupported or converter fails
- **Validation success**: Returns empty list when record is valid
- **Validation failures**: Returns list of `ValidationError` objects describing all violations
- **Rule evaluation**: Only enabled rules are evaluated; disabled rules are silently skipped

## Configuration
No configuration required. Validation behavior is entirely controlled by:
- The specification definition provided at validation time
- Which rules are enabled in that specification
- The `TranslationProvider` implementation for error message localization

## Dependencies and interactions
- **Input dependencies**:
  - `SpecificationDto`: Complete specification with fields, subfields, indicators, and enabled rules (obtained from specification retrieval APIs)
  - MARC record: MARC4J `Record` object or custom `MarcRecord` model
  - `TranslationProvider`: For localizing validation error messages
- **Output**: List of `ValidationError` objects
- **Library dependencies**:
  - `marc4j`: For MARC record parsing and conversion
  - `mod-record-specifications-dto`: For specification and error DTOs
- **Integration pattern**: Applications retrieve specifications via [Specification Retrieval](specification-retrieval.md), then validate records programmatically using this library
