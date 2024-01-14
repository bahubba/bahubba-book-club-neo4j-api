package com.bahubba.bahubbabookclub.model.mapper;

import com.bahubba.bahubbabookclub.model.dto.UserDTO;
import com.bahubba.bahubbabookclub.model.entity.User;
import com.bahubba.bahubbabookclub.model.mapper.custom.EncodeMapping;
import com.bahubba.bahubbabookclub.model.mapper.custom.PasswordEncoderMapper;
import com.bahubba.bahubbabookclub.model.payload.UserPayload;
import java.util.List;
import lombok.Generated;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Mapping logic for {@link User} entities and {@link UserDTO} DTOs */
@Mapper(componentModel = "spring", uses = PasswordEncoderMapper.class)
public interface UserMapper {
    @Generated
    @Mapping(target = "id", ignore = true) // generated
    @Mapping(target = "memberships", ignore = true) // no memberships initially
    @Mapping(target = "role", ignore = true) // defaults to USER
    @Mapping(target = "joined", ignore = true) // defaults to now
    @Mapping(target = "departed", ignore = true) // default should be null
    @Mapping(source = "password", target = "password", qualifiedBy = EncodeMapping.class)
    User payloadToEntity(UserPayload newUser);

    @Generated
    UserDTO entityToDTO(User user);

    @Generated
    List<UserDTO> entityListToDTO(List<User> users);
}
