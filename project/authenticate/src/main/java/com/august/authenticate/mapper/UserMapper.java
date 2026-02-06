package com.august.authenticate.mapper;

import com.august.authenticate.dto.requests.UserCreateRequest;
import com.august.authenticate.dto.responses.UserResponse;
import com.august.authenticate.entity.RoleEntity;
import com.august.authenticate.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(source = "roles", target = "roles", qualifiedByName = "mapRoles")
    UserResponse mapToResponse(UserEntity userEntity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "roles", ignore = true)
    UserEntity mapToEntity(UserCreateRequest request);

    @Named("mapRoles")
    default Set<String> mapRoles(Set<RoleEntity> roles){
        if (roles == null){
            return Collections.emptySet();
        }
        return roles.stream().map(RoleEntity::getName).collect(Collectors.toSet());
    }
}
