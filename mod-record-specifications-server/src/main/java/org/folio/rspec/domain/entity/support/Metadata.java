package org.folio.rspec.domain.entity.support;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.sql.Timestamp;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

@Getter
@Setter
@Embeddable
public class Metadata {

  public static final String CREATED_DATE_COLUMN = "created_date";
  public static final String UPDATED_DATE_COLUMN = "updated_date";
  public static final String CREATED_BY_USER_COLUMN = "created_by_user_id";
  public static final String UPDATED_BY_USER_COLUMN = "updated_by_user_id";

  @CreatedBy
  @Column(name = CREATED_BY_USER_COLUMN, nullable = false, updatable = false)
  private UUID createdByUserId;

  @CreatedDate
  @Column(name = CREATED_DATE_COLUMN, nullable = false, updatable = false)
  private Timestamp createdDate;

  @LastModifiedBy
  @Column(name = UPDATED_BY_USER_COLUMN, nullable = false)
  private UUID updatedByUserId;

  @LastModifiedDate
  @Column(name = UPDATED_DATE_COLUMN, nullable = false)
  private Timestamp updatedDate;

}
