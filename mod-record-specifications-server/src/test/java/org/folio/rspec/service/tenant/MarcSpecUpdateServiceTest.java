package org.folio.rspec.service.tenant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.folio.rspec.domain.dto.Family;
import org.folio.rspec.domain.dto.FamilyProfile;
import org.folio.rspec.domain.dto.IncludeParam;
import org.folio.rspec.domain.dto.SpecificationDto;
import org.folio.rspec.domain.dto.SpecificationDtoCollection;
import org.folio.rspec.domain.entity.AppliedSpecUpdate;
import org.folio.rspec.domain.repository.AppliedSpecUpdateRepository;
import org.folio.rspec.service.SpecificationService;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class MarcSpecUpdateServiceTest {

  private static final SpecUpdate ALL_PROFILES = new SpecUpdate("TEST-ALL", Family.MARC, null);
  private static final SpecUpdate BIB_ONLY = new SpecUpdate("TEST-BIB", Family.MARC, FamilyProfile.BIBLIOGRAPHIC);
  private static final SpecUpdate AUTH_ONLY = new SpecUpdate("TEST-AUTH", Family.MARC, FamilyProfile.AUTHORITY);

  @Mock
  private AppliedSpecUpdateRepository appliedSpecUpdateRepository;
  @Mock
  private SpecificationService specificationService;

  private MarcSpecUpdateService service;

  private SpecificationDto bibSpec;
  private SpecificationDto authSpec;

  @BeforeEach
  void stubSpecifications() {
    service = new MarcSpecUpdateService(appliedSpecUpdateRepository, specificationService);
    bibSpec = specificationDto(FamilyProfile.BIBLIOGRAPHIC);
    authSpec = specificationDto(FamilyProfile.AUTHORITY);
    lenient().when(specificationService.findSpecifications(eq(Family.MARC), any(), eq(IncludeParam.NONE),
        eq(100), eq(0)))
      .thenAnswer(invocation -> {
        FamilyProfile profile = invocation.getArgument(1);
        var specs = profile == null ? List.of(bibSpec, authSpec)
          : List.of(profile == FamilyProfile.BIBLIOGRAPHIC ? bibSpec : authSpec);
        return new SpecificationDtoCollection().specifications(specs);
      });
    lenient().when(specificationService.getSpecificationById(any(UUID.class), eq(IncludeParam.ALL)))
      .thenAnswer(invocation -> fullSnapshot(invocation.getArgument(0)));
  }

  @Test
  void markAllKnownUpdatesApplied_savesOneRowPerKnownUpdatePerProfileInItsScope() {
    service.markAllKnownUpdatesApplied();

    var expectedTuples = MarcSpecUpdateService.KNOWN_UPDATES.stream()
      .flatMap(update -> List.of(FamilyProfile.BIBLIOGRAPHIC, FamilyProfile.AUTHORITY).stream()
        .filter(profile -> update.profile() == null || update.profile() == profile)
        .map(profile -> tuple(update.code(), Family.MARC, profile)))
      .toList();
    assertThat(savedRows())
      .extracting(AppliedSpecUpdate::getCode, AppliedSpecUpdate::getFamily, AppliedSpecUpdate::getProfile)
      .containsExactlyInAnyOrderElementsOf(expectedTuples);
    verify(specificationService, never()).sync(any(UUID.class), eq(true));
  }

  @Test
  void applyPendingUpdates_syncsEachSpecificationOnce_forTheRealKnownUpdates() {
    service.applyPendingUpdates();

    verify(specificationService, times(1)).sync(bibSpec.getId(), true);
    verify(specificationService, times(1)).sync(authSpec.getId(), true);
    verify(specificationService, times(2)).sync(any(UUID.class), eq(true));
  }

  @Test
  void applyPendingUpdates_doesNothing_whenEveryCombinationInScopeIsAlreadyRecorded() {
    appliedRows(row("TEST-ALL", FamilyProfile.BIBLIOGRAPHIC), row("TEST-ALL", FamilyProfile.AUTHORITY));

    serviceFor(ALL_PROFILES).applyPendingUpdates();

    verify(specificationService, never()).sync(any(UUID.class), eq(true));
    verify(specificationService, never()).getSpecificationById(any(UUID.class), eq(IncludeParam.ALL));
    verify(appliedSpecUpdateRepository, never()).saveAll(anyList());
  }

  @Test
  void applyPendingUpdates_resyncsOnlyTheMissingProfile_whenCodeIsRecordedForJustOneOfItsProfiles() {
    appliedRows(row("TEST-ALL", FamilyProfile.BIBLIOGRAPHIC));

    serviceFor(ALL_PROFILES).applyPendingUpdates();

    verify(specificationService).sync(authSpec.getId(), true);
    verify(specificationService, never()).sync(bibSpec.getId(), true);
    assertThat(savedRows())
      .extracting(AppliedSpecUpdate::getCode, AppliedSpecUpdate::getProfile)
      .containsExactly(tuple("TEST-ALL", FamilyProfile.AUTHORITY));
  }

  @Test
  void applyPendingUpdates_picksUpNewlyCoveredProfile_whenCodeScopeWasWidened() {
    appliedRows(row("TEST-BIB", FamilyProfile.BIBLIOGRAPHIC));

    serviceFor(new SpecUpdate("TEST-BIB", Family.MARC, null)).applyPendingUpdates();

    verify(specificationService).sync(authSpec.getId(), true);
    verify(specificationService, never()).sync(bibSpec.getId(), true);
    assertThat(savedRows())
      .extracting(AppliedSpecUpdate::getCode, AppliedSpecUpdate::getProfile)
      .containsExactly(tuple("TEST-BIB", FamilyProfile.AUTHORITY));
  }

  @Test
  void applyPendingUpdates_resyncsOnlyTheScopedProfile_whenUpdateIsProfileScoped() {
    serviceFor(BIB_ONLY).applyPendingUpdates();

    verify(specificationService).sync(bibSpec.getId(), true);
    verify(specificationService, never()).sync(authSpec.getId(), true);
    verify(specificationService, never()).findSpecifications(Family.MARC, null, IncludeParam.NONE, 100, 0);
    assertThat(savedRows())
      .extracting(AppliedSpecUpdate::getCode, AppliedSpecUpdate::getFamily, AppliedSpecUpdate::getProfile)
      .containsExactly(tuple("TEST-BIB", Family.MARC, FamilyProfile.BIBLIOGRAPHIC));
  }

  @Test
  void applyPendingUpdates_storesThePreSyncFullSpecificationAsSnapshot() {
    serviceFor(BIB_ONLY).applyPendingUpdates();

    var snapshot = savedRows().getFirst().getSpecificationSnapshot();
    assertThat(snapshot.getId()).isEqualTo(bibSpec.getId());
    assertThat(snapshot.getTitle()).isEqualTo("full");
  }

  @Test
  void applyPendingUpdates_dedupesSpecificationsSharedByTwoPendingUpdates() {
    serviceFor(ALL_PROFILES, AUTH_ONLY).applyPendingUpdates();

    verify(specificationService, times(1)).sync(bibSpec.getId(), true);
    verify(specificationService, times(1)).sync(authSpec.getId(), true);
    assertThat(savedRows())
      .extracting(AppliedSpecUpdate::getCode, AppliedSpecUpdate::getProfile)
      .containsExactlyInAnyOrder(tuple("TEST-ALL", FamilyProfile.BIBLIOGRAPHIC),
        tuple("TEST-ALL", FamilyProfile.AUTHORITY), tuple("TEST-AUTH", FamilyProfile.AUTHORITY));
  }

  @Test
  void applyPendingUpdates_onlyRecordsTheCodesThatArePending() {
    appliedRows(row("TEST-ALL", FamilyProfile.BIBLIOGRAPHIC), row("TEST-ALL", FamilyProfile.AUTHORITY));

    serviceFor(ALL_PROFILES, BIB_ONLY).applyPendingUpdates();

    assertThat(savedRows())
      .extracting(AppliedSpecUpdate::getCode, AppliedSpecUpdate::getProfile)
      .containsExactly(tuple("TEST-BIB", FamilyProfile.BIBLIOGRAPHIC));
  }

  private MarcSpecUpdateService serviceFor(SpecUpdate... updates) {
    return new MarcSpecUpdateService(appliedSpecUpdateRepository, specificationService, List.of(updates));
  }

  private void appliedRows(AppliedSpecUpdate... rows) {
    when(appliedSpecUpdateRepository.findByIdCodeIn(any())).thenReturn(List.of(rows));
  }

  private List<AppliedSpecUpdate> savedRows() {
    ArgumentCaptor<List<AppliedSpecUpdate>> captor = ArgumentCaptor.captor();
    verify(appliedSpecUpdateRepository).saveAll(captor.capture());
    return captor.getValue();
  }

  private AppliedSpecUpdate row(String code, FamilyProfile profile) {
    return new AppliedSpecUpdate(code, Family.MARC, profile, specificationDto(profile),
      Timestamp.from(Instant.now()));
  }

  private static SpecificationDto fullSnapshot(UUID id) {
    return new SpecificationDto().id(id).title("full");
  }

  private static SpecificationDto specificationDto(FamilyProfile profile) {
    return new SpecificationDto().id(UUID.randomUUID()).family(Family.MARC).profile(profile);
  }
}
