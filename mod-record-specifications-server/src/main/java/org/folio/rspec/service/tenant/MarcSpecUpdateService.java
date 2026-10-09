package org.folio.rspec.service.tenant;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.rspec.domain.dto.Family;
import org.folio.rspec.domain.dto.FamilyProfile;
import org.folio.rspec.domain.dto.IncludeParam;
import org.folio.rspec.domain.dto.SpecificationDto;
import org.folio.rspec.domain.entity.AppliedSpecUpdate;
import org.folio.rspec.domain.repository.AppliedSpecUpdateRepository;
import org.folio.rspec.service.SpecificationService;
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
 * family/profile) and whether it still needs to run for a given tenant, tracked one row per
 * concrete (code, family, profile) in {@code applied_spec_update}.
 */
@Service
@Log4j2
@RequiredArgsConstructor
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

  /**
   * For a tenant that just received its initial, full sync (so it's already current with every
   * known update): record all of them as applied without resyncing again.
   */
  @Transactional
  public void markAllKnownUpdatesApplied() {
    markApplied(KNOWN_UPDATES, resolveSpecificationsInScope(KNOWN_UPDATES));
  }

  /**
   * For a tenant that already existed before this upgrade: resync the specification(s) scoped to
   * any known update it hasn't applied yet (deduplicated, so a specification shared by several
   * pending updates is only resynced once), then record all of those updates as applied.
   */
  @Transactional
  public void applyPendingUpdates() {
    applyPendingUpdates(KNOWN_UPDATES);
  }

  /**
   * Package-private so tests can exercise the family/profile scoping against a small candidate
   * list instead of the real {@link #KNOWN_UPDATES}. {@link #applyPendingUpdates()} is the only
   * production entry point.
   */
  void applyPendingUpdates(List<SpecUpdate> candidates) {
    var pending = pendingUpdates(candidates);
    if (pending.isEmpty()) {
      return;
    }

    log.info("Applying pending MARC spec updates: {}", pending.stream().map(SpecUpdate::code).toList());
    var specificationsByCode = resolveSpecificationsInScope(pending);
    specificationsByCode.values().stream()
      .flatMap(List::stream)
      .collect(Collectors.toMap(SpecificationDto::getId, Function.identity(), (first, duplicate) -> first))
      .values()
      .forEach(specification -> specificationService.sync(specification.getId(), true));

    markApplied(pending, specificationsByCode);
  }

  /**
   * Fetches with {@code IncludeParam.ALL} (full nested fields/indicators/subfields/codes), not
   * just {@code NONE}: the same lookup used to pick the resync targets also doubles as the
   * pre-resync snapshot for {@link #markApplied}, read before {@code sync()} changes anything.
   */
  private Map<String, List<SpecificationDto>> resolveSpecificationsInScope(List<SpecUpdate> updates) {
    return updates.stream().collect(Collectors.toMap(SpecUpdate::code, update ->
      specificationService.findSpecifications(update.family(), update.profile(), IncludeParam.ALL, 100, 0)
        .getSpecifications()));
  }

  private List<SpecUpdate> pendingUpdates(List<SpecUpdate> candidates) {
    var candidateCodes = candidates.stream().map(SpecUpdate::code).toList();
    Set<String> applied = appliedSpecUpdateRepository.findByIdCodeIn(candidateCodes).stream()
      .map(AppliedSpecUpdate::getCode)
      .collect(Collectors.toSet());
    return candidates.stream().filter(update -> !applied.contains(update.code())).toList();
  }

  /**
   * Records one row per specification actually in an update's scope (not one row per update), so
   * a {@code null}-profile update ends up with a concrete row per profile it covered and the
   * composite (code, family, profile) key is never asked to hold a null profile. Each row's
   * {@code specificationSnapshot} is that specification's full, pre-resync content.
   */
  private void markApplied(List<SpecUpdate> updates, Map<String, List<SpecificationDto>> specificationsByCode) {
    var appliedDate = Timestamp.from(Instant.now());
    var entries = updates.stream()
      .flatMap(update -> specificationsByCode.get(update.code()).stream()
        .map(spec -> new AppliedSpecUpdate(update.code(), spec.getFamily(), spec.getProfile(), spec, appliedDate)))
      .toList();
    appliedSpecUpdateRepository.saveAll(entries);
  }
}
