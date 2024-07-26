## v1.0.0 YYYY-mm-DD
### New APIs versions
* Provides `specification-storage v1.0`
* Requires `API_NAME vX.Y`

### Features
#### Server
##### Specification Storage API
* implement endpoint to fetch specifications ([MRSPECS-3](https://folio-org.atlassian.net//browse/MRSPECS-3))
* implement endpoints to fetch specification rules and enable/disable it ([MRSPECS-4](https://folio-org.atlassian.net//browse/MRSPECS-4))
* implement endpoints to manage field definitions ([MRSPECS-6](https://folio-org.atlassian.net//browse/MRSPECS-6))
* implement sync endpoint to initialize specification with defaults ([MRSPECS-12](https://folio-org.atlassian.net//browse/MRSPECS-12))
* implement endpoints to manage indicator/code definitions ([MRSPECS-7](https://folio-org.atlassian.net//browse/MRSPECS-7))
* implement GET/POST endpoints to manage subfield definitions ([MRSPECS-8](https://folio-org.atlassian.net//browse/MRSPECS-8))
* add limitations for modifying and deleting system/standard field definition ([MRSPECS-11](https://folio-org.atlassian.net//browse/MRSPECS-11))
* add include parameter for GET specifications endpoint ([MRSPECS-28](https://folio-org.atlassian.net//browse/MRSPECS-28))
* implement PUT/DELETE endpoints for indicators, indicator codes ([MRSPECS-25](https://folio-org.atlassian.net//browse/MRSPECS-25))
* Send Kafka event in case of specification change ([MRSPECS-40](https://folio-org.atlassian.net//browse/MRSPECS-40))
* Restrict creating indicators/subfields for control fields ([MRSPECS-39](https://folio-org.atlassian.net//browse/MRSPECS-39))

#### Validator
* implement validation by MARC specification ([MRSPECS-38](https://folio-org.atlassian.net//browse/MRSPECS-38))

#### General
* Implement build dependants GitHub workflow on PR creation ([MRSPECS-9](https://folio-org.atlassian.net//browse/MRSPECS-9))

### Bug fixes
* Description ([ISSUE](https://folio-org.atlassian.net/browse/ISSUE))
