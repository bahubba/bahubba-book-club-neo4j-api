package com.bahubba.bahubbabookclub.model.mapper;

import com.bahubba.bahubbabookclub.model.dto.MembershipRequestDTO;
import com.bahubba.bahubbabookclub.model.entity.MembershipRequest;
import java.util.List;
import lombok.Generated;
import org.mapstruct.Mapper;

/** Mapping logic for {@link MembershipRequest} entities and {@link MembershipRequestDTO} DTOs */
@Mapper(componentModel = "spring")
public interface MembershipRequestMapper {
    @Generated
    MembershipRequestDTO entityToDTO(MembershipRequest membershipRequest);

    @Generated
    List<MembershipRequestDTO> entityListToDTO(List<MembershipRequest> membershipRequests);
}
