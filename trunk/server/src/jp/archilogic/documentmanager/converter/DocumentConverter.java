package jp.archilogic.documentmanager.converter;

import jp.archilogic.documentmanager.dto.DocumentResDto;
import jp.archilogic.documentmanager.entity.Document;

import org.springframework.stereotype.Component;

@Component
public class DocumentConverter implements IEntityToDtoConverter< Document , DocumentResDto > {
    @Override
    public DocumentResDto toDto( Document entity ) {
        DocumentResDto dto = new DocumentResDto();

        dto.id = entity.id;
        dto.name = entity.name;

        return dto;
    }
}
