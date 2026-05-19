-- Script for https://folio-org.atlassian.net/browse/MRSPECS-202

-- Replace ${tenantId} placeholder with specific tenant id
SET SEARCH_PATH = ${tenantId}_mod_record_specifications;

-- Update field '590' to be non-deprecated and make its subfield 'd' repeatable in the MARC bibliographic specification.
WITH updated_field AS (
    UPDATE field f
    SET deprecated = false
    FROM specification s
    WHERE f.specification_id = s.id
      AND f.tag = '590'
      AND s.family = 'MARC'::family_enum
      AND s.profile = 'BIBLIOGRAPHIC'::family_profile_enum
    RETURNING f.id
)
UPDATE subfield sf
SET repeatable = true
FROM updated_field f
WHERE sf.field_id = f.id
  AND sf.code = 'd';
