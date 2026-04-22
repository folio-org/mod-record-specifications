-- ==========================================================
-- DESCRIPTION: Add new subfields and update existing subfields in MARC bibliographic and authority specifications based on the latest MARC standards documentation.
--  - Marc Bibliographic Specification: https://www.loc.gov/marc/bibliographic/ecbdlist.html
--  - Marc Authority Specification: https://www.loc.gov/marc/authority/ecadlist.html
-- Script for https://folio-org.atlassian.net/browse/MRSPECS-201 ticket.
-- The script performs the following operations:
-- 1. Inserts new subfields for specific fields in the MARC bibliographic specification and authority specification if they do not already exist.
--    - Subfield '6' for fields 060 and 070 in the bibliographic specification
--    - Subfield 'z'  for field 245 in the bibliographic specification
--    - Subfields 'i' and '4' for field 381 in both bibliographic and authority specifications
--    - Subfield 'b' for fields 611 and 711 in the bibliographic specification
--    - Subfield 'z' for field 500 in the bibliographic specification
--    - Subfield 'b' for field 411 in the bibliographic specification
--    - Subfields 'd' and 'e' for field 242 in the bibliographic specification
--    - Subfield 'b' for field 111 in the bibliographic specification
--    - Subfield '1' for field 340 in the bibliographic specification
--    - Subfields 'i' and '4' for field 368 and 376 in the authority specification
-- 2. Updates labels and deprecation status of existing subfields in the MARC bibliographic and authority specifications according to the latest standards.
--    - Updating the label of subfield 'g' in field 884 in both bibliographic and authority specifications
--    - Marking subfields 'b', 'd', and 'e' in field 850 in the bibliographic specification as deprecated
--    - Marking subfield 'q' in fields 760, 762, 765, 767, 770, 772, 775, 776, 777, 780, and 785 in the bibliographic specification as deprecated
--    - Marking subfield 'z' in fields 515, 525, 530, 546, 547, and 550 in the bibliographic specification as deprecated
-- REQUIREMENTS:
-- Replace ${tenantId} placeholder with specific tenant id before executing the script.

DO $$
DECLARE
    inserted_count INT := 0;
    updated_count  INT := 0;
BEGIN
    -- Set schema context
    SET search_path = ${tenantId}_mod_record_specifications;

    -- ============================================
    -- FIELD MAP
    -- ============================================
    WITH field_map AS (
        SELECT f.id, f.tag, s.profile
        FROM field f
        JOIN specification s
            ON f.specification_id = s.id
        WHERE s.family = 'MARC'
    ),

    -- ============================================
    -- INSERT NEW SUBFIELDS
    -- ============================================
    ins AS (
        INSERT INTO subfield (
            id,
            created_by_user_id,
            updated_by_user_id,
            created_date,
            updated_date,
            code,
            label,
            repeatable,
            required,
            deprecated,
            scope,
            field_id
        )
        SELECT
            gen_random_uuid(),
            '00000000-0000-0000-0000-000000000000',
            '00000000-0000-0000-0000-000000000000',
            now(),
            now(),
            v.code,
            v.label,
            v.repeatable,
            v.required,
            v.deprecated,
            v.scope::scope_enum,
            f.id
        FROM (
            VALUES
                -- code, label, repeatable, required, deprecated, scope, tag, profile
                ('6','Linkage',false,false,false,'STANDARD','060','BIBLIOGRAPHIC'),
                ('6','Linkage',false,false,false,'STANDARD','070','BIBLIOGRAPHIC'),
                ('z','Title statement context note',false,false,false,'STANDARD','245','BIBLIOGRAPHIC'),
                ('i','Relationship information',true,false,false,'STANDARD','381','BIBLIOGRAPHIC'),
                ('4','Relationship',true,false,false,'STANDARD','381','BIBLIOGRAPHIC'),
                ('b','Number',true,false,true,'STANDARD','611','BIBLIOGRAPHIC'),
                ('b','Number',true,false,true,'STANDARD','711','BIBLIOGRAPHIC'),
                ('z','Source of note information',true,false,true,'STANDARD','500','BIBLIOGRAPHIC'),
                ('b','Number',true,false,true,'STANDARD','411','BIBLIOGRAPHIC'),
                ('d','Designation of section',true,false,true,'STANDARD','242','BIBLIOGRAPHIC'),
                ('e','Name of part/section',true,false,true,'STANDARD','242','BIBLIOGRAPHIC'),
                ('b','Number',true,false,true,'STANDARD','111','BIBLIOGRAPHIC'),
                ('1','Real World Object URI',true,false,false,'STANDARD','340','BIBLIOGRAPHIC'),
                ('i','Relationship information',true,false,false,'STANDARD','368','AUTHORITY'),
                ('4','Relationship',true,false,false,'STANDARD','368','AUTHORITY'),
                ('i','Relationship information',true,false,false,'STANDARD','376','AUTHORITY'),
                ('4','Relationship',true,false,false,'STANDARD','376','AUTHORITY'),
                ('i','Relationship information',true,false,false,'STANDARD','381','AUTHORITY'),
                ('4','Relationship',true,false,false,'STANDARD','381','AUTHORITY')
        ) AS v(code, label, repeatable, required, deprecated, scope, tag, profile)
        JOIN field_map f
            ON f.tag = v.tag
           AND f.profile = v.profile::family_profile_enum
        ON CONFLICT (field_id, code) DO NOTHING
        RETURNING 1
    )
    -- Count inserted rows
    SELECT COUNT(*) INTO inserted_count FROM ins;

    -- ============================================
    -- UPDATE EXISTING SUBFIELDS
    -- ============================================
    WITH field_map AS (
        SELECT f.id, f.tag, s.profile
        FROM field f
        JOIN specification s
            ON f.specification_id = s.id
        WHERE s.family = 'MARC'
    ),
    upd AS (
        UPDATE subfield sf
        SET
            label = CASE
                WHEN sf.code = 'g' THEN 'Conversion date/time'
                ELSE sf.label
            END,
            deprecated = CASE
                WHEN sf.code IN ('b','d','e') THEN true
                WHEN sf.code = 'q' THEN true
                WHEN sf.code = 'z' THEN true
                ELSE sf.deprecated
            END
        FROM field_map fm
        WHERE sf.field_id = fm.id
          AND (
                (sf.code = 'g' AND fm.tag = '884')
             OR (sf.code IN ('b','d','e') AND fm.tag = '850' AND fm.profile = 'BIBLIOGRAPHIC'::family_profile_enum)
             OR (sf.code = 'q' AND fm.tag IN ('760','762','765','767','770','772','775','776','777','780','785') AND fm.profile = 'BIBLIOGRAPHIC'::family_profile_enum)
             OR (sf.code = 'z' AND fm.tag IN ('515','525','530','546','547','550') AND fm.profile = 'BIBLIOGRAPHIC'::family_profile_enum)
          )
        RETURNING 1
    )
    -- Count updated rows
    SELECT COUNT(*) INTO updated_count FROM upd;

    -- ============================================
    -- SUMMARY
    -- ============================================
    RAISE INFO '=================SUMMARY===================';
    RAISE INFO 'INSERTED: %', inserted_count;
    RAISE INFO 'UPDATED:   %', updated_count;
    RAISE INFO '===========================================';

END $$;
