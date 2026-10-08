# MARC spec corrections

MARC occasionally revises its field/indicator/subfield definitions (see
https://www.loc.gov/marc/bibliographic/ and https://www.loc.gov/marc/authority/). This module
ships its own copy of those pages (`spec/marc/bibliographic.html`, `spec/marc/authority.html`)
and keeps a normalized, per-tenant copy of their content in the `field`, `indicator`,
`indicator_code` and `subfield` tables. When MARC changes, both copies need to move together, and
a tenant that was already provisioned before the fix shipped needs to be brought up to date too.

## How a correction reaches an existing tenant

1. Fix the bundled HTML (`spec/marc/bibliographic.html` / `authority.html`) to match the current
   LOC page. This alone is already enough for a brand-new tenant, and for any tenant an operator
   explicitly resyncs afterward.
2. Add one entry to `MarcSpecUpdateService.KNOWN_UPDATES` — the ticket's code, plus the
   family/profile the fix actually touched (`FamilyProfile.BIBLIOGRAPHIC`,
   `FamilyProfile.AUTHORITY`, or `null` for every profile in that family, when the fix spans both).
   That's the entire mechanism; there's nothing else to write per ticket.

On the next `POST /_/tenant` for that tenant (a module upgrade), `ExtendedTenantService` calls
`MarcSpecUpdateService.applyPendingUpdates()`, which:
- Reads `applied_spec_update` to see which known codes this tenant has already applied.
- For whatever's still pending, resyncs (`preserveLocal=true`) only the specification(s) in that
  update's scope — not every specification, so a bibliographic-only fix never touches the
  authority spec at all.
- Records one row per specification actually in scope, keyed by `(code, family, profile)` with
  `applied_date` and `specification_snapshot` alongside. A `null`-profile update ends up with one
  concrete row per profile it covered - e.g. `(MRSPECS-212, MARC, BIBLIOGRAPHIC)` and
  `(MRSPECS-212, MARC, AUTHORITY)` - never a row with a literal `null` profile, and never
  overwriting a previous row: because the scope is part of the key, if a code's scope in
  `KNOWN_UPDATES` is ever corrected later (e.g. narrowed from "every profile" to just one), the
  next upgrade adds the new (code, family, profile) row alongside the old one rather than
  replacing it, so every scope a code was ever actually applied with for this tenant stays on
  record. A later upgrade only acts on whatever's newly added to `KNOWN_UPDATES` since (checked by
  `code` alone - `pendingUpdates` doesn't care which profiles were recorded for it, only that the
  code has at least one row).
- `specification_snapshot` is a full backup of that specification (fields, indicators, subfields,
  indicator codes - the same shape `GET .../specifications/{id}?include=all` returns) taken right
  before the resync that applied the update, so the pre-fix state is always on record even though
  the live tables get overwritten. The lookup that picks which specifications are in an update's
  scope (`findSpecifications(family, profile, IncludeParam.ALL, ...)`) already fetches this full
  shape, so capturing it as the snapshot costs nothing extra.

A brand-new tenant skips straight to `markAllKnownUpdatesApplied()` instead: its initial sync
already ran against the current (fixed) HTML, so every known code is recorded as applied without
resyncing again.

## Why `preserveLocal`, not a full resync

`SpecificationSyncService.sync()` re-parses the bundled HTML and reconciles it against a
specification's fields (see `SpecificationFieldService.syncFields`). Without `preserveLocal=true`
it does a full `deleteBySpecificationId` + `saveAll`, which has no way to tell a cataloger's
`LOCAL`-scope field/subfield from one that simply fell out of the HTML, so a blanket resync on an
already-provisioned tenant risks silently deleting local customizations. `preserveLocal=true`
switches to a reconcile mode instead: any tag/order/code the spec defines overrides whatever is
currently stored there (even a `LOCAL` row — the spec wins once it defines that slot), but a
`LOCAL` definition the spec still doesn't know about is left untouched. For `indicator`, which has
no `scope` column, "does the spec know about this order" is read off `specification_metadata`
instead.

A STANDARD/SYSTEM row isn't purely spec-owned either: the scope validators
(`FieldStandardScopeValidator`/`FieldSystemScopeValidator`/`SubfieldStandardScopeValidator`/
`SubfieldSystemScopeValidator`) let an operator edit a handful of fields even on a non-LOCAL
row — a STANDARD field's `url` and `required`, a SYSTEM field's `url` only, a STANDARD subfield's
`required`. Those are policy choices, not LOC facts, so `preserveLocal` sync keeps them across a
resync too, as long as keeping them doesn't contradict an invariant the sync itself enforces —
right now that's just "a deprecated field has no url": if the spec now marks the field deprecated,
the custom url is dropped like it would be for any other field, never carried over. Everything
else that's user-editable (`required` on both scopes) has no such invariant, so it's always kept
once it differs from what the spec would otherwise compute. Indicators and indicator codes have no
operator-editable fields at non-LOCAL scope at all (`updateCode`/`updateIndicator` reject anything
but `LOCAL` outright), so there's nothing equivalent to preserve there.

## Why application tracking, not a Liquibase data changeset

An earlier version of this mechanism shipped each correction as its own idempotent Liquibase
changeset directly patching `field`/`indicator`/`indicator_code`/`subfield` and
`specification_metadata`. That worked but didn't scale well: every ticket meant hand-writing SQL
that re-derived rows by natural key, pinning fixed UUIDs for any new row, and separately patching
`specification_metadata.fields` to keep `sync()` from reassigning that id later - easy to get
subtly wrong (two real bugs turned up only once something actually ran `sync()` against a freshly
provisioned tenant, not just against raw SQL in isolation). Since the bundled HTML is already the
source of truth `sync()` reads, and `preserveLocal` sync is now safe to run against an
already-provisioned tenant, a correction no longer needs its own SQL at all: fix the HTML, add one
line to `KNOWN_UPDATES`, and the existing sync machinery does the rest. `applied_spec_update`
(created once, in `db/changelog/changes/v3.1/create-applied-spec-update-table.xml`) is the only
schema change a correction ever needs again, and it's already there.

## Testing a new entry

- Unit-test `MarcSpecUpdateService` (see `MarcSpecUpdateServiceTest`) if the scoping logic itself
  changes.
- For the correction itself, provisioning a **genuinely fresh** tenant and confirming the expected
  field/subfield/indicator state is the real check — a long-lived local schema you've been poking
  at manually will hide bugs that only show up on a tenant's first-ever sync, since by then most
  tags already have a complete `specification_metadata` entry from earlier syncs.
  `SpecificationStoragePreserveLocalSyncApiIT` and `ExtendedTenantServiceTest` show the pattern for
  exercising this through `IntegrationTestBase`.
