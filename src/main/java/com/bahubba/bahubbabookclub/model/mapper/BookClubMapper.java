package com.bahubba.bahubbabookclub.model.mapper;

import com.bahubba.bahubbabookclub.model.dto.BookClubDTO;
import com.bahubba.bahubbabookclub.model.entity.BookClub;
import com.bahubba.bahubbabookclub.model.payload.BookClubPayload;
import java.util.List;
import lombok.Generated;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Mapping logic for {@link BookClub} entities and {@link BookClubDTO} DTOs */
@Mapper(componentModel = "spring")
public interface BookClubMapper {
    @Generated
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "members", ignore = true)
    @Mapping(target = "created", ignore = true)
    @Mapping(target = "disbanded", ignore = true)
    BookClub payloadToEntity(BookClubPayload payload);

    @Generated
    @Mapping(target = "image.fileName", source = "imageFileName")
    @Mapping(target = "image.url", ignore = true)
    BookClubDTO entityToDTO(BookClub bookClub);

    @Generated
    @Mapping(target = "id", source = "entity.id")
    @Mapping(target = "name", source = "dto.name")
    @Mapping(target = "imageFileName", source = "dto.image.fileName")
    @Mapping(target = "description", source = "dto.description")
    @Mapping(target = "publicity", source = "dto.publicity")
    @Mapping(target = "members", source = "entity.members")
    @Mapping(target = "created", source = "entity.created")
    @Mapping(target = "disbanded", source = "entity.disbanded")
    BookClub mergeDTOToEntity(BookClubDTO dto, BookClub entity);

    @Generated
    List<BookClubDTO> entityListToDTO(List<BookClub> bookClubs);
}
