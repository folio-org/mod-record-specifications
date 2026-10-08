package org.folio.rspec.service.tenant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.anyList;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class MarcSpecUpdateServiceTest {

  @Mock
  private AppliedSpecUpdateRepository appliedSpecUpdateRepository;
  @Mock
  private SpecificationService specificationService;

  @InjectMocks
  private MarcSpecUpdateService service;

  @Test
  void markAllKnownUpdatesApplied_savesOneRowPerKnownUpdatePerResolvedProfile() {
    var bibSpec = specificationDto(FamilyProfile.BIBLIOGRAPHIC);
    var authSpec = specificationDto(FamilyProfile.AUTHORITY);
    when(specificationService.findSpecifications(Family.MARC, null, IncludeParam.ALL, 100, 0))
      .thenReturn(new SpecificationDtoCollection().specifications(List.of(bibSpec, authSpec)));

    service.markAllKnownUpdatesApplied();

    ArgumentCaptor<List<AppliedSpecUpdate>> captor = ArgumentCaptor.captor();
    verify(appliedSpecUpdateRepository).saveAll(captor.capture());
    var expectedTuples = MarcSpecUpdateService.KNOWN_UPDATES.stream()
      .flatMap(update -> List.of(
        tuple(update.code(), Family.MARC, FamilyProfile.BIBLIOGRAPHIC),
        tuple(update.code(), Family.MARC, FamilyProfile.AUTHORITY)
      ).stream())
      .toList();
    assertThat(captor.getValue())
      .extracting(AppliedSpecUpdate::getCode, AppliedSpecUpdate::getFamily, AppliedSpecUpdate::getProfile)
      .containsExactlyInAnyOrderElementsOf(expectedTuples);
  }

  @Test
  void applyPendingUpdates_doesNothing_whenEveryCandidateAlreadyApplied() {
    var candidates = List.of(new SpecUpdate("TEST-1", Family.MARC, null));
    when(appliedSpecUpdateRepository.findByIdCodeIn(List.of("TEST-1")))
      .thenReturn(List.of(new AppliedSpecUpdate("TEST-1", Family.MARC, null, specificationDto(null),
        Timestamp.from(Instant.now()))));

    service.applyPendingUpdates(candidates);

    verify(specificationService, never()).findSpecifications(Family.MARC, null, IncludeParam.ALL, 100, 0);
    verify(appliedSpecUpdateRepository, never()).saveAll(anyList());
  }

  @Test
  void applyPendingUpdates_resyncsOnlyTheScopedProfile_whenUpdateIsProfileScoped() {
    var candidates = List.of(new SpecUpdate("TEST-BIB-ONLY", Family.MARC, FamilyProfile.BIBLIOGRAPHIC));
    when(appliedSpecUpdateRepository.findByIdCodeIn(List.of("TEST-BIB-ONLY"))).thenReturn(List.of());

    var bibSpec = specificationDto(FamilyProfile.BIBLIOGRAPHIC);
    when(specificationService.findSpecifications(Family.MARC, FamilyProfile.BIBLIOGRAPHIC, IncludeParam.ALL, 100, 0))
      .thenReturn(new SpecificationDtoCollection().specifications(List.of(bibSpec)));

    service.applyPendingUpdates(candidates);

    verify(specificationService).sync(bibSpec.getId(), true);
    verify(specificationService, never())
      .findSpecifications(Family.MARC, FamilyProfile.AUTHORITY, IncludeParam.ALL, 100, 0);
    verify(specificationService, never()).findSpecifications(Family.MARC, null, IncludeParam.ALL, 100, 0);

    ArgumentCaptor<List<AppliedSpecUpdate>> captor = ArgumentCaptor.captor();
    verify(appliedSpecUpdateRepository).saveAll(captor.capture());
    assertThat(captor.getValue())
      .extracting(AppliedSpecUpdate::getCode, AppliedSpecUpdate::getFamily, AppliedSpecUpdate::getProfile)
      .containsExactly(tuple("TEST-BIB-ONLY", Family.MARC, FamilyProfile.BIBLIOGRAPHIC));
    assertThat(captor.getValue().getFirst().getSpecificationSnapshot()).isSameAs(bibSpec);
  }

  @Test
  void applyPendingUpdates_dedupesSpecificationsSharedByTwoPendingUpdates() {
    var sharedBibSpec = specificationDto(FamilyProfile.BIBLIOGRAPHIC);
    var sharedAuthSpec = specificationDto(FamilyProfile.AUTHORITY);
    when(appliedSpecUpdateRepository.findByIdCodeIn(List.of("TEST-ALL-PROFILES", "TEST-AUTH-ONLY")))
      .thenReturn(List.of());
    when(specificationService.findSpecifications(Family.MARC, null, IncludeParam.ALL, 100, 0))
      .thenReturn(new SpecificationDtoCollection().specifications(List.of(sharedBibSpec, sharedAuthSpec)));
    when(specificationService.findSpecifications(Family.MARC, FamilyProfile.AUTHORITY, IncludeParam.ALL, 100, 0))
      .thenReturn(new SpecificationDtoCollection().specifications(List.of(sharedAuthSpec)));
    var candidates = List.of(
      new SpecUpdate("TEST-ALL-PROFILES", Family.MARC, null),
      new SpecUpdate("TEST-AUTH-ONLY", Family.MARC, FamilyProfile.AUTHORITY)
    );
    service.applyPendingUpdates(candidates);
    // authority spec is in scope for both pending updates, but only resynced once
    verify(specificationService).sync(sharedBibSpec.getId(), true);
    verify(specificationService, times(1)).sync(sharedAuthSpec.getId(), true);
    ArgumentCaptor<List<AppliedSpecUpdate>> captor = ArgumentCaptor.captor();
    verify(appliedSpecUpdateRepository).saveAll(captor.capture());
    assertThat(captor.getValue())
      .extracting(AppliedSpecUpdate::getCode, AppliedSpecUpdate::getFamily, AppliedSpecUpdate::getProfile)
      .containsExactlyInAnyOrder(tuple("TEST-ALL-PROFILES", Family.MARC, FamilyProfile.BIBLIOGRAPHIC),
        tuple("TEST-ALL-PROFILES", Family.MARC, FamilyProfile.AUTHORITY),
        tuple("TEST-AUTH-ONLY", Family.MARC, FamilyProfile.AUTHORITY));
  }

  @Test
  void applyPendingUpdates_onlyResyncsAndRecordsThePendingCandidate() {
    var candidates = List.of(
      new SpecUpdate("TEST-ALREADY-APPLIED", Family.MARC, null),
      new SpecUpdate("TEST-PENDING", Family.MARC, null)
    );
    when(appliedSpecUpdateRepository.findByIdCodeIn(List.of("TEST-ALREADY-APPLIED", "TEST-PENDING")))
      .thenReturn(
        List.of(new AppliedSpecUpdate("TEST-ALREADY-APPLIED", Family.MARC, FamilyProfile.BIBLIOGRAPHIC,
          specificationDto(FamilyProfile.BIBLIOGRAPHIC), Timestamp.from(Instant.now()))));
    var bibSpec = specificationDto(FamilyProfile.BIBLIOGRAPHIC);
    when(specificationService.findSpecifications(Family.MARC, null, IncludeParam.ALL, 100, 0))
      .thenReturn(new SpecificationDtoCollection().specifications(List.of(bibSpec)));

    service.applyPendingUpdates(candidates);

    ArgumentCaptor<List<AppliedSpecUpdate>> captor = ArgumentCaptor.captor();
    verify(appliedSpecUpdateRepository).saveAll(captor.capture());
    assertThat(captor.getValue())
      .extracting(AppliedSpecUpdate::getCode, AppliedSpecUpdate::getFamily, AppliedSpecUpdate::getProfile)
      .containsExactly(tuple("TEST-PENDING", Family.MARC, FamilyProfile.BIBLIOGRAPHIC));
  }

  private static SpecificationDto specificationDto(FamilyProfile profile) {
    return new SpecificationDto().id(UUID.randomUUID()).family(Family.MARC).profile(profile);
  }
}
