package com.bahubba.bahubbabookclub.model.mapper;

import com.bahubba.bahubbabookclub.model.dto.BookClubMembershipDTO;
import com.bahubba.bahubbabookclub.model.entity.BookClubMembership;
import java.util.List;
import lombok.Generated;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Mapping logic for {@link BookClubMembership} entities and {@link BookClubMembershipDTO} DTOs */
@Mapper(componentModel = "spring")
public interface BookClubMembershipMapper {
    @Mapping(target = "isOwner", source = "owner")
    @Generated
    BookClubMembershipDTO entityToDTO(BookClubMembership bookClubMembership);

    @Generated
    List<BookClubMembershipDTO> entityListToDTOList(List<BookClubMembership> bookClubMemberships);
}
