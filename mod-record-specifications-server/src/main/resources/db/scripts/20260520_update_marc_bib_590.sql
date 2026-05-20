-- Script for https://folio-org.atlassian.net/browse/MRSPECS-202

-- Replace ${tenantId} placeholder with specific tenant id
SET SEARCH_PATH = ${tenantId}_mod_record_specifications;

-- Update field '590' to be non-deprecated and set scope to LOCAL in MARC bibliographic specification
WITH updated_field AS (
    UPDATE field f
    SET deprecated = false,
        scope = 'LOCAL'
    FROM specification s
    WHERE f.specification_id = s.id
      AND f.tag = '590'
      AND s.family = 'MARC'::family_enum
      AND s.profile = 'BIBLIOGRAPHIC'::family_profile_enum
    RETURNING f.id
),
-- Update subfields of field '590' to set scope to LOCAL and make subfield 'd' repeatable
updated_subfield AS (
    UPDATE subfield sf
    SET scope = 'LOCAL',
        repeatable = CASE
            WHEN sf.code = 'd' THEN true
            ELSE repeatable
        END
    FROM updated_field f
    WHERE sf.field_id = f.id
    RETURNING 1
)
-- Update indicator codes of field '590' to set scope to LOCAL
UPDATE indicator_code ic
SET scope = 'LOCAL'
FROM indicator i, updated_field f
WHERE ic.indicator_id = i.id
  AND i.field_id = f.id;
