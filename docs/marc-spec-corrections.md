# MARC spec corrections via Liquibase

MARC occasionally revises its field/indicator/subfield definitions (see
https://www.loc.gov/marc/bibliographic/ and https://www.loc.gov/marc/authority/). This module
ships its own copy of those pages (`spec/marc/bibliographic.html`, `spec/marc/authority.html`)
and keeps a normalized, per-tenant copy of their content in the `field`, `indicator`,
`indicator_code` and `subfield` tables. When MARC changes, both copies need to move together.

## Why not just re-run the sync?

`SpecificationSyncService.sync()` re-parses the bundled HTML and does a full
`deleteBySpecificationId` + `saveAll` on a specification's fields (see
`SpecificationFieldService.syncFields`). That's correct for a brand-new tenant, but on an
already-provisioned one it has no way to tell a cataloger's `LOCAL`-scope field/subfield from
one that simply fell out of the HTML, so a blanket resync risks silently deleting local
customizations. That's why spec corrections for existing tenants are shipped as targeted,
idempotent Liquibase data changesets instead of by invoking sync() tenant-wide — the same
approach already used for MRSPECS-201 and MRSPECS-212 (see
`db/changelog/changes/marc-spec-updates/`).

Still update the bundled HTML files alongside the changeset. They're what `sync()` reads, so
a brand-new tenant (or any tenant explicitly resynced later) ends up consistent with the fix
without needing the changeset at all; the changeset exists to carry the same fix to tenants
that won't go through sync().

## Convention for a new spec-correction changeset

1. One file per ticket, under `db/changelog/changes/marc-spec-updates/MRSPECS-<ticket>-<short-desc>.xml`,
   added as another `<include>` in `changelog-marc-spec-updates.xml`. This folder is
   deliberately **not** one of the `v<major>.<minor>/` release-version folders: spec corrections
   are an ongoing activity, independent of module releases, and frequently need to be
   backported/cherry-picked onto older maintenance branches. Those branches won't have a
   matching `vX.Y` folder for whatever the current development version is, but they'll always
   have `changelog-marc-spec-updates.xml` (or can take it via the same cherry-pick) with its
   list of includes growing over time. `changelog-master.xml` includes
   `changes/changelog-marc-spec-updates.xml` once, after all the release-version includes.
2. Resolve rows by natural key, not by hardcoded id. Every statement starts from a `field_map`
   CTE joining `field` to `specification` on `(family, profile, tag)` (add an `indicator_map` on
   top of it keyed by `indicator_order` when touching indicators/codes). This makes the exact
   same SQL correct for every tenant schema — Liquibase already runs it once per tenant, so
   there's no `${tenantId}` placeholder or `SET search_path` to manage by hand.
3. Inserts use a fixed, literal UUID per new row (generate with `uuid_generate_v4()`/`uuidgen`
   once, paste it in) — never `gen_random_uuid()`. A fixed id lets the same literal be reused in
   the metadata-pinning changeset (step 5) and gives the row a stable identity across replays.
4. Make every statement idempotent:
   - Inserts: `ON CONFLICT (<the real unique constraint columns>) DO NOTHING`.
   - Updates: setting the same value twice is naturally a no-op; no extra guard needed.
5. Whenever a changeset **inserts** a new `subfield`, `indicator`, or `indicator_code` row, add
   a companion changeset patching `specification_metadata.fields` to pin the same id at the
   matching key path (`{tag, subfields, code}` or `{tag, indicators, <order>, codes, code}`).
   Without this, a future `sync()` run won't find that id in the metadata cache and will
   generate a new random one for the same code — silently changing its id. Use a `DO $$ ... FOR
   rec IN (VALUES ...) LOOP UPDATE ... END LOOP; END $$;` block (one `jsonb_set` per row, each
   guarded by `AND NOT (... ? code)`) rather than a single set-based `UPDATE ... FROM (VALUES
   ...)`: Postgres only applies one arbitrary match when a set-based `UPDATE ... FROM` has
   several join rows landing on the same target row (e.g. two new subfields on the same field),
   so a loop is required for correctness whenever a ticket adds more than one code to the same
   tag/order. Pure label/deprecated updates on already-existing rows need no metadata change —
   their id is unchanged and the cache doesn't store label text.
6. Add a `preConditions`/`tableExists` guard and a `<comment>` per changeset, matching the style
   already used in `v2.1/update-default-data.xml` and `v3.0/move-rule-metadata.xml`.

Before committing, it's worth applying the new SQL directly (e.g. via `docker exec ... psql`)
against a scratch schema that already has the *old* data, diffing the result against a schema
that already has the *new* data, and re-running the same SQL a second time to confirm it's a
true no-op the second time.
