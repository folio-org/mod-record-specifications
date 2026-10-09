package org.folio.rspec.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.sql.Timestamp;
import java.util.Objects;
import lombok.NoArgsConstructor;
import org.folio.rspec.domain.dto.Family;
import org.folio.rspec.domain.dto.FamilyProfile;
import org.folio.rspec.domain.dto.SpecificationDto;
import org.hibernate.Hibernate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Tracks which one-off MARC spec corrections (see docs/marc-spec-corrections.md) have already
 * been applied to this tenant, so {@code MarcSpecUpdateService} can tell a brand-new tenant
 * (already fully up to date via its initial sync) from an existing one that still needs a
 * targeted resync, and can skip updates it has already applied on a later module upgrade. Keyed
 * by (code, family, profile) - see {@link AppliedSpecUpdateId} - so the table doubles as a
 * durable record of every scope an update was ever applied with, not just the most recent one.
 * {@code specificationSnapshot} backs up the full specification (fields, indicators, subfields,
 * indicator codes) for that family/profile as it stood right before the resync that applied this
 * update, in case the update ever needs to be inspected or manually reverted.
 */
@Entity
@Table(name = AppliedSpecUpdate.TABLE_NAME)
@NoArgsConstructor
public class AppliedSpecUpdate {

  public static final String TABLE_NAME = "applied_spec_update";
  public static final String SPECIFICATION_SNAPSHOT_COLUMN = "specification_snapshot";
  public static final String APPLIED_DATE_COLUMN = "applied_date";

  @EmbeddedId
  private AppliedSpecUpdateId id;

  @Column(name = SPECIFICATION_SNAPSHOT_COLUMN, nullable = false)
  @JdbcTypeCode(SqlTypes.JSON)
  private SpecificationDto specificationSnapshot;

  @Column(name = APPLIED_DATE_COLUMN, nullable = false)
  private Timestamp appliedDate;

  public AppliedSpecUpdate(String code, Family family, FamilyProfile profile, SpecificationDto specificationSnapshot,
                           Timestamp appliedDate) {
    this.id = new AppliedSpecUpdateId(code, family, profile);
    this.specificationSnapshot = specificationSnapshot;
    this.appliedDate = appliedDate;
  }

  public String getCode() {
    return id.getCode();
  }

  public Family getFamily() {
    return id.getFamily();
  }

  public FamilyProfile getProfile() {
    return id.getProfile();
  }

  public SpecificationDto getSpecificationSnapshot() {
    return specificationSnapshot;
  }

  public Timestamp getAppliedDate() {
    return appliedDate;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
      return false;
    }
    AppliedSpecUpdate that = (AppliedSpecUpdate) o;
    return id != null && Objects.equals(id, that.id);
  }
}
