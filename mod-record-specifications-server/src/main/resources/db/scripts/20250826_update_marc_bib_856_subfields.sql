-- Replace ${tenantId} placeholder with specific tenant id
-- Script for https://folio-org.atlassian.net/browse/MRSPECS-97
SET SEARCH_PATH = ${tenantId}_mod_record_specifications;

-- Update subfield with codes 'g', 'h', 'l', 'n', 'r', 't' related to field '856' in MARC bibliographic specification
UPDATE subfield
SET
    label = CASE code
                WHEN 'g' THEN 'Persistent identifier'
                WHEN 'h' THEN 'Non-functioning Uniform Resource Identifier'
                WHEN 'l' THEN 'Standardized information governing access'
                WHEN 'n' THEN 'Terms governing access'
                WHEN 'r' THEN 'Standardized information governing use and reproduction'
                WHEN 't' THEN 'Terms governing use and reproduction'
                ELSE label
            END,
    repeatable = true,
    required = false,
    deprecated = false
WHERE code IN ('g', 'h', 'l', 'n', 'r', 't')
  AND field_id IN (
    SELECT f.id
    FROM field f
    JOIN specification s ON f.specification_id = s.id
    WHERE f.tag = '856'
      AND s.family = 'MARC'::family_enum
      AND s.profile = 'BIBLIOGRAPHIC'::family_profile_enum
);

