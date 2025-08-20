## v2.0.2 2025-08-28
### Bug fixes
* Fix non-obsolete subfield choosing logic ([MRSPECS-95](https://folio-org.atlassian.net/browse/MRSPECS-95))

---

## v2.0.1 2025-04-18
### Features
* Refactor specification sync from URL to local copy ([MRSPECS-87](https://folio-org.atlassian.net/browse/MRSPECS-87))

---

## v2.0.0 2025-03-03
### Breaking changes
* Upgrade to Java 21 ([MRSPECS-81](https://folio-org.atlassian.net/browse/MRSPECS-81))

### Features
* Include specification `family`, `profile` into specification updated event ([MRSPECS-73](https://folio-org.atlassian.net/browse/MRSPECS-73))

### Bug fixes
* Fix tenant upgrade not being ran on schema existence ([MRSPECS-71](https://folio-org.atlassian.net/browse/MRSPECS-71))
* Fix cache not being updated by dependant modules on specification update ([MRSPECS-76](https://folio-org.atlassian.net/browse/MRSPECS-76))
* Fix build dependants workflow ([MRSPECS-68](https://folio-org.atlassian.net/browse/MRSPECS-68))
* Fix documentation generation github workflow  ([MRSPECS-78](https://folio-org.atlassian.net/browse/MRSPECS-78))
* Fix specification.update consumer startup  ([MRSPECS-82](https://folio-org.atlassian.net/browse/MRSPECS-82))
* Add retry for specification fetching in case of failure ([MRSPECS-85](https://folio-org.atlassian.net/browse/MRSPECS-85))

### Dependencies
* Bump `spring-boot` from `3.3.5` to `3.4.3`
* Bump `folio-spring-support` from `8.2.0` to `9.0.0`
* Bump `folio-service-tools` from `4.1.0` to `5.0.0`
* Bump `lombok` from `1.18.34` to `1.18.36`
* Bump `mapstruct` from `1.6.2` to `1.6.3`
* Bump `jsoup` from `1.18.1` to `1.18.3`
* Bump `commons-text` from `1.12.0` to `1.13.0`

---

## v1.0.0 2024-10-31
### New APIs versions
* Provides `specification-storage v1.0`

### Features
#### Server
##### Specification Storage API
* Implement endpoint to fetch specifications ([MRSPECS-3](https://folio-org.atlassian.net//browse/MRSPECS-3))
* Implement endpoints to fetch specification rules and enable/disable it ([MRSPECS-4](https://folio-org.atlassian.net//browse/MRSPECS-4))
* Implement endpoints to manage field definitions ([MRSPECS-6](https://folio-org.atlassian.net//browse/MRSPECS-6))
* Implement sync endpoint to initialize specification with defaults ([MRSPECS-12](https://folio-org.atlassian.net//browse/MRSPECS-12))
* Implement endpoints to manage indicator/code definitions ([MRSPECS-7](https://folio-org.atlassian.net//browse/MRSPECS-7))
* Implement GET/POST endpoints to manage subfield definitions ([MRSPECS-8](https://folio-org.atlassian.net//browse/MRSPECS-8))
* Add limitations for modifying and deleting system/standard field definition ([MRSPECS-11](https://folio-org.atlassian.net//browse/MRSPECS-11))
* Add include parameter for GET specifications endpoint ([MRSPECS-28](https://folio-org.atlassian.net//browse/MRSPECS-28))
* Implement PUT/DELETE endpoints for indicators, indicator codes ([MRSPECS-25](https://folio-org.atlassian.net//browse/MRSPECS-25))
* Implement PUT/DELETE endpoints for subfields ([MRSPECS-24](https://folio-org.atlassian.net//browse/MRSPECS-24))
* Send Kafka event in case of specification change ([MRSPECS-40](https://folio-org.atlassian.net//browse/MRSPECS-40))
* Restrict creating indicators/subfields for control fields ([MRSPECS-39](https://folio-org.atlassian.net//browse/MRSPECS-39))
* Implement GET specification by id endpoint ([MRSPECS-50](https://folio-org.atlassian.net//browse/MRSPECS-50))
* Sync specifications on new tenant creation ([MRSPECS-53](https://folio-org.atlassian.net//browse/MRSPECS-53))
* Consume specification update request events ([MRSPECS-60](https://folio-org.atlassian.net//browse/MRSPECS-60))
* Set empty url for deprecated fields on sync specifications ([MRSPECS-70](https://folio-org.atlassian.net//browse/MRSPECS-70))

#### Validator
* Implement validation by MARC specification ([MRSPECS-38](https://folio-org.atlassian.net//browse/MRSPECS-38))
* Implement Non-Repeatable 1XX Fields validator ([MRSPECS-41](https://folio-org.atlassian.net/browse/MRSPECS-41))
* Implement Non-Repeatable Required 1XX Fields validator ([MRSPECS-42](https://folio-org.atlassian.net/browse/MRSPECS-42))
* Implement Invalid Field Tag validator ([MRSPECS-44](https://folio-org.atlassian.net/browse/MRSPECS-44))
* Implement Invalid Indicator validation ([MRSPECS-45](https://folio-org.atlassian.net/browse/MRSPECS-45))
* Implement Undefined Indicator validation ([MRSPECS-46](https://folio-org.atlassian.net/browse/MRSPECS-46))
* Implement Missing Subfield validation([MRSPECS-47](https://folio-org.atlassian.net/browse/MRSPECS-47))
* Implement Undefined Subfield validation ([MRSPECS-49](https://folio-org.atlassian.net/browse/MRSPECS-49))
* Implement Non-Repeatable Subfield validation ([MRSPECS-48](https://folio-org.atlassian.net/browse/MRSPECS-48))
* Implement Invalid LCCN Subfield validation ([MRSPECS-59](https://folio-org.atlassian.net/browse/MRSPECS-59))

#### General
* Implement build dependants GitHub workflow on PR creation ([MRSPECS-9](https://folio-org.atlassian.net//browse/MRSPECS-9))
* Adjust build dependants github action to be triggered only on dto/validator submodules changes ([MRSPECS-26](https://folio-org.atlassian.net//browse/MRSPECS-26))
* Add module descriptor validator plugin and fix permission name validation issue ([MRSPECS-55](https://folio-org.atlassian.net//browse/MRSPECS-55))
* Fix link to OAS file ([MRSPECS-56](https://folio-org.atlassian.net//browse/MRSPECS-56))

### Bug fixes
* Validation errors triggered by different fields with the same tag return all errors for the zero-indexed tag only.([MRSPECS-66](https://folio-org.atlassian.net/browse/MRSPECS-66))
