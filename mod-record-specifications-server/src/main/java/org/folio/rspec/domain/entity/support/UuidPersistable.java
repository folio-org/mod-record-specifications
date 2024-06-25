package org.folio.rspec.domain.entity.support;

import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Transient;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Persistable;
import org.springframework.lang.Nullable;

@Getter
@Setter
@MappedSuperclass
public abstract class UuidPersistable implements Persistable<UUID> {

  @Id
  private @Nullable UUID id;

  @Nullable
  @Override
  public UUID getId() {
    return id;
  }

  @Transient
  @Override
  public boolean isNew() {
    return null == getId();
  }

  @PreUpdate
  @PrePersist
  public void prePersist() {
    if (isNew()) {
      this.id = UUID.randomUUID();
    }
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
    UuidPersistable that = (UuidPersistable) o;
    return id != null && Objects.equals(id, that.id);
  }

  @Override
  public String toString() {
    return String.format("Entity of type %s with id: %s", this.getClass().getName(), getId());
  }
}
