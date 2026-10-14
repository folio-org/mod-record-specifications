package org.folio.rspec.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import org.folio.rspec.domain.dto.Family;
import org.folio.rspec.domain.dto.FamilyProfile;
import org.folio.rspec.domain.dto.SpecificationDto;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;

@UnitTest
class AppliedSpecUpdateTest {

  private final Timestamp appliedDate = Timestamp.from(Instant.now());
  private final SpecificationDto snapshot = new SpecificationDto().id(UUID.randomUUID());

  @Test
  void constructor_exposesEveryValueThroughGetters() {
    var entity = entity("CODE-1", FamilyProfile.BIBLIOGRAPHIC);

    assertThat(entity.getCode()).isEqualTo("CODE-1");
    assertThat(entity.getFamily()).isEqualTo(Family.MARC);
    assertThat(entity.getProfile()).isEqualTo(FamilyProfile.BIBLIOGRAPHIC);
    assertThat(entity.getSpecificationSnapshot()).isSameAs(snapshot);
    assertThat(entity.getAppliedDate()).isEqualTo(appliedDate);
  }

  @Test
  void equalsAndHashCode_areBasedOnTheCompositeKeyOnly() {
    var first = entity("CODE-1", FamilyProfile.BIBLIOGRAPHIC);
    var sameKeyOtherPayload = new AppliedSpecUpdate("CODE-1", Family.MARC, FamilyProfile.BIBLIOGRAPHIC,
      new SpecificationDto(), Timestamp.from(Instant.EPOCH));

    assertThat(first).isEqualTo(first).isEqualTo(sameKeyOtherPayload).hasSameHashCodeAs(sameKeyOtherPayload);
  }

  @Test
  void equals_isFalse_whenKeyDiffersOrOtherIsNullOrAnotherType() {
    var first = entity("CODE-1", FamilyProfile.BIBLIOGRAPHIC);

    assertThat(first)
      .isNotEqualTo(entity("CODE-2", FamilyProfile.BIBLIOGRAPHIC))
      .isNotEqualTo(entity("CODE-1", FamilyProfile.AUTHORITY))
      .isNotEqualTo(null)
      .isNotEqualTo("CODE-1");
  }

  @Test
  void equals_isFalse_forTwoEntitiesWithoutKey() {
    assertThat(new AppliedSpecUpdate()).isNotEqualTo(new AppliedSpecUpdate());
  }

  private AppliedSpecUpdate entity(String code, FamilyProfile profile) {
    return new AppliedSpecUpdate(code, Family.MARC, profile, snapshot, appliedDate);
  }
}
