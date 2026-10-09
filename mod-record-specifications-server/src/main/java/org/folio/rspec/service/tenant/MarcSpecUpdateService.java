package org.folio.rspec.service.tenant;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.folio.rspec.domain.dto.Family;
import org.folio.rspec.domain.dto.FamilyProfile;
import org.folio.rspec.domain.dto.IncludeParam;
import org.folio.rspec.domain.dto.SpecificationDto;
import org.folio.rspec.domain.entity.AppliedSpecUpdate;
import org.folio.rspec.domain.entity.AppliedSpecUpdateId;
import org.folio.rspec.domain.repository.AppliedSpecUpdateRepository;
import org.folio.rspec.service.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Applies and tracks one-off MARC spec corrections - fixes to the bundled
 * {@code spec/marc/*.html} (plus whatever {@code specification_metadata} override they need) that
 * must reach tenants provisioned before the fix shipped. See docs/marc-spec-corrections.md for the
 * full story and for how to add a new entry here.
 *
 * <p>Applying an update is just a {@code preserveLocal} resync of the specification(s) it's scoped
 * to: the HTML is already the fixed version by the time a tenant upgrades to it, and a
 * {@code preserveLocal} resync (see {@code SpecificationFieldService}) safely reconciles existing
 * tenant data against it without discarding local customizations. So every known update shares the
 * same action; what differs per update is which specification(s) it applies to (its
 * family/profile). Whether it still needs to run is tracked per concrete (code, family, profile)
 * in {@code applied_spec_update}: an update is pending for a specification until that exact
 * combination has a row, so a code whose scope is later widened is picked up for the newly
 * covered profile instead of being treated as done.
 */
@Service
@Log4j2
public class MarcSpecUpdateService {

  /**
   * One entry per ticket that fixed the bundled MARC spec HTML, scoped to the family/profile it
   * actually touched (a {@code null} profile means every profile in that family). Add the new
   * ticket's code here (and nowhere else) when its HTML fix ships; existing tenants pick it up as
   * a pending update on their next upgrade, resync only the specification(s) it's scoped to, and a
   * brand-new tenant is already up to date and simply marks it applied.
   */
  public static final List<SpecUpdate> KNOWN_UPDATES = List.of(
    new SpecUpdate("MRSPECS-201", Family.MARC, null),
    new SpecUpdate("MRSPECS-212", Family.MARC, FamilyProfile.BIBLIOGRAPHIC),
    new SpecUpdate("MRSPECS-213", Family.MARC, FamilyProfile.AUTHORITY)
  );

  private final AppliedSpecUpdateRepository appliedSpecUpdateRepository;
  private final SpecificationService specificationService;
  private final List<SpecUpdate> knownUpdates;

  @Autowired
  public MarcSpecUpdateService(AppliedSpecUpdateRepository appliedSpecUpdateRepository,
                               SpecificationService specificationService) {
    this(appliedSpecUpdateRepository, specificationService, KNOWN_UPDATES);
  }

  /**
   * Package-private so tests can exercise the family/profile scoping against a small update list
   * instead of the real {@link #KNOWN_UPDATES}.
   */
  MarcSpecUpdateService(AppliedSpecUpdateRepository appliedSpecUpdateRepository,
                        SpecificationService specificationService, List<SpecUpdate> knownUpdates) {
    this.appliedSpecUpdateRepository = appliedSpecUpdateRepository;
    this.specificationService = specificationService;
    this.knownUpdates = knownUpdates;
  }

  /**
   * For a tenant that just received its initial, full sync (so it's already current with every
   * known update): record every not-yet-recorded (code, family, profile) as applied without
   * resyncing again.
   */
  @Transactional
  public void markAllKnownUpdatesApplied() {
    var pending = pendingApplications(knownUpdates);
    recordApplied(pending, snapshotsOf(pending));
  }

  /**
   * For a tenant that already existed before this upgrade: resync every specification that has at
   * least one known update not yet recorded for it (once per specification, however many updates
   * are pending for it), then record each of those (code, family, profile) combinations.
   */
  @Transactional
  public void applyPendingUpdates() {
    var pending = pendingApplications(knownUpdates);
    if (pending.isEmpty()) {
      return;
    }

    log.info("Applying pending MARC spec updates: {}",
      pending.stream().map(p -> p.code() + "/" + p.specification().getProfile()).toList());
    var snapshots = snapshotsOf(pending);
    snapshots.keySet().forEach(specificationId -> specificationService.sync(specificationId, true));

    recordApplied(pending, snapshots);
  }

  /**
   * An update is pending for a specification until its exact (code, family, profile) row exists.
   * Looks specifications up without nested content ({@code IncludeParam.NONE}) since this runs on
   * every upgrade; the full snapshot is only loaded for what is actually pending.
   */
  private List<PendingApplication> pendingApplications(List<SpecUpdate> candidates) {
    var candidateCodes = candidates.stream().map(SpecUpdate::code).toList();
    Set<AppliedSpecUpdateId> applied = appliedSpecUpdateRepository.findByIdCodeIn(candidateCodes).stream()
      .map(row -> new AppliedSpecUpdateId(row.getCode(), row.getFamily(), row.getProfile()))
      .collect(Collectors.toSet());
    return candidates.stream()
      .flatMap(update -> specificationsInScope(update).stream()
        .filter(spec -> !applied.contains(new AppliedSpecUpdateId(update.code(), spec.getFamily(), spec.getProfile())))
        .map(spec -> new PendingApplication(update.code(), spec)))
      .toList();
  }

  private List<SpecificationDto> specificationsInScope(SpecUpdate update) {
    return specificationService.findSpecifications(update.family(), update.profile(), IncludeParam.NONE, 100, 0)
      .getSpecifications();
  }

  /**
   * Full nested content (fields/indicators/subfields/codes) of each distinct pending
   * specification, read once, before any {@code sync()} changes it.
   */
  private Map<UUID, SpecificationDto> snapshotsOf(List<PendingApplication> pending) {
    var snapshots = new LinkedHashMap<UUID, SpecificationDto>();
    pending.forEach(application -> snapshots.computeIfAbsent(application.specification().getId(),
      id -> specificationService.getSpecificationById(id, IncludeParam.ALL)));
    return snapshots;
  }

  /**
   * Records one row per pending (code, family, profile), each carrying that specification's
   * pre-resync snapshot. Rows are always concrete (never a null profile), and existing rows are
   * never touched because only combinations without a row are ever pending.
   */
  private void recordApplied(List<PendingApplication> pending, Map<UUID, SpecificationDto> snapshots) {
    var appliedDate = Timestamp.from(Instant.now());
    var entries = pending.stream()
      .map(application -> new AppliedSpecUpdate(application.code(), application.specification().getFamily(),
        application.specification().getProfile(), snapshots.get(application.specification().getId()), appliedDate))
      .toList();
    appliedSpecUpdateRepository.saveAll(entries);
  }

  /**
   * One update that hasn't been recorded yet for one concrete specification.
   */
  private record PendingApplication(String code, SpecificationDto specification) {
  }
}
