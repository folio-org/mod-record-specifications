package org.folio.rspec.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.rspec.domain.dto.Scope;
import org.folio.rspec.domain.dto.SubfieldChangeDto;
import org.folio.rspec.domain.dto.SubfieldDto;
import org.folio.rspec.domain.dto.SubfieldDtoCollection;
import org.folio.rspec.domain.entity.Field;
import org.folio.rspec.domain.repository.SubfieldRepository;
import org.folio.rspec.service.mapper.SubfieldMapper;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class SubfieldService {

  private final SubfieldRepository subfieldRepository;
  private final SubfieldMapper subfieldMapper;

  public SubfieldDtoCollection findFieldSubfields(UUID fieldId) {
    log.debug("findFieldSubfields::fieldId={}", fieldId);
    var subfieldDtos = subfieldRepository.findByFieldId(fieldId).stream()
      .map(subfieldMapper::toDto)
      .toList();
    return new SubfieldDtoCollection().subfields(subfieldDtos).totalRecords(subfieldDtos.size());
  }

  public SubfieldDto createLocalSubfield(Field field, SubfieldChangeDto createDto) {
    log.info("createLocalSubfield::fieldId={}, dto={}", field.getId(), createDto);
    var entity = subfieldMapper.toEntity(createDto);
    entity.setField(field);
    entity.setScope(Scope.LOCAL);
    var created = subfieldRepository.save(entity);
    return subfieldMapper.toDto(created);
  }
}