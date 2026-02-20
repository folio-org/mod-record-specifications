# Module Features

This module provides the following features:

## Specification Management
| Feature                                                          | Description                                                                            |
|------------------------------------------------------------------|----------------------------------------------------------------------------------------|
| [Specification Retrieval](features/specification-retrieval.md)   | Retrieve MARC specifications with filtering and optional inclusion of related entities |
| [Specification Synchronization](features/specification-sync.md)  | Reset specifications to default state by removing local customizations                 |

## Record Validation
| Feature                                                                   | Description                                                               |
|---------------------------------------------------------------------------|---------------------------------------------------------------------------|
| [Record Validation Against Specifications](features/record-validation.md) | Programmatic validation of MARC records against specification definitions |

## Validation Rules
| Feature                                                                    | Description                                                                     |
|----------------------------------------------------------------------------|---------------------------------------------------------------------------------|
| [Specification Rule Management](features/specification-rule-management.md) | View and enable/disable validation rules with automatic audit metadata tracking |

## Field Definitions
| Feature                                              | Description                                                                                    |
|------------------------------------------------------|------------------------------------------------------------------------------------------------|
| [Field Management](features/field-management.md)     | Retrieve all field definitions and create, update, or delete custom local field definitions    |

## Subfield Definitions
| Feature                                                  | Description                                                                                        |
|----------------------------------------------------------|----------------------------------------------------------------------------------------------------|
| [Subfield Management](features/subfield-management.md)   | Retrieve all subfield definitions and create, update, or delete custom local subfield definitions  |

## Indicator Definitions
| Feature                                                    | Description                                                                                           |
|------------------------------------------------------------|-------------------------------------------------------------------------------------------------------|
| [Indicator Management](features/indicator-management.md)   | Retrieve all indicator definitions, create and update custom indicators, and append indicator codes   |

## Event-Driven Integration
| Feature                                                                        | Description                                                                      |
|--------------------------------------------------------------------------------|----------------------------------------------------------------------------------|
| [Specification Update Processing](features/specification-update-processing.md) | Process specification update requests via Kafka for event-driven synchronization |
| [Specification Change Events](features/specification-change-events.md)         | Publish event notifications when specification data is modified                  |
