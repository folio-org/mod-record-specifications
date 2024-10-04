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
* implement PUT/DELETE endpoints for subfields ([MRSPECS-24](https://folio-org.atlassian.net//browse/MRSPECS-24))
* Send Kafka event in case of specification change ([MRSPECS-40](https://folio-org.atlassian.net//browse/MRSPECS-40))
* Restrict creating indicators/subfields for control fields ([MRSPECS-39](https://folio-org.atlassian.net//browse/MRSPECS-39))
* Implement GET specification by id endpoint ([MRSPECS-50](https://folio-org.atlassian.net//browse/MRSPECS-50))
* Sync specifications on new tenant creation ([MRSPECS-53](https://folio-org.atlassian.net//browse/MRSPECS-53))

#### Validator
* implement validation by MARC specification ([MRSPECS-38](https://folio-org.atlassian.net//browse/MRSPECS-38))
* implement Non-Repeatable 1XX Fields validator ([MRSPECS-41](https://folio-org.atlassian.net/browse/MRSPECS-41))
* implement Non-Repeatable Required 1XX Fields validator ([MRSPECS-42](https://folio-org.atlassian.net/browse/MRSPECS-42))
* implement Invalid Field Tag validator ([MRSPECS-44](https://folio-org.atlassian.net/browse/MRSPECS-44))
* implement Invalid Indicator validation ([MRSPECS-45](https://folio-org.atlassian.net/browse/MRSPECS-45))
* implement Undefined Indicator validation ([MRSPECS-46](https://folio-org.atlassian.net/browse/MRSPECS-46))
* implement Missing Subfield validation([MRSPECS-47](https://folio-org.atlassian.net/browse/MRSPECS-47))
* implement Undefined Subfield validation ([MRSPECS-49](https://folio-org.atlassian.net/browse/MRSPECS-49))
* implement Non-Repeatable Subfield validation ([MRSPECS-48](https://folio-org.atlassian.net/browse/MRSPECS-48))
* implement Invalid LCCN Subfield validation ([MRSPECS-59](https://folio-org.atlassian.net/browse/MRSPECS-59))

#### General
* Implement build dependants GitHub workflow on PR creation ([MRSPECS-9](https://folio-org.atlassian.net//browse/MRSPECS-9))
* Adjust build dependants github action to be triggered only on dto/validator submodules changes ([MRSPECS-26](https://folio-org.atlassian.net//browse/MRSPECS-26))
* Add module descriptor validator plugin and fix permission name validation issue ([MRSPECS-55](https://folio-org.atlassian.net//browse/MRSPECS-55))
* Fix link to OAS file ([MRSPECS-56](https://folio-org.atlassian.net//browse/MRSPECS-56))

### Bug fixes
* Description ([ISSUE](https://folio-org.atlassian.net/browse/ISSUE))
