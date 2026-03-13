package com.august.identity.mapper;

import com.august.identity.dto.requests.UserCreateRequest;
import com.august.identity.dto.responses.UserResponse;
import com.august.identity.entity.UserEntity;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-06T14:23:26+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.1 (Oracle Corporation)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserResponse mapToResponse(UserEntity userEntity) {
        if ( userEntity == null ) {
            return null;
        }

        UserResponse.UserResponseBuilder userResponse = UserResponse.builder();

        userResponse.roles( mapRoles( userEntity.getRoles() ) );
        userResponse.id( userEntity.getId() );
        userResponse.keycloakId( userEntity.getKeycloakId() );
        userResponse.username( userEntity.getUsername() );
        userResponse.email( userEntity.getEmail() );
        userResponse.avatarUrl( userEntity.getAvatarUrl() );
        userResponse.createdAt( userEntity.getCreatedAt() );
        userResponse.updatedAt( userEntity.getUpdatedAt() );

        return userResponse.build();
    }

    @Override
    public UserEntity mapToEntity(UserCreateRequest request) {
        if ( request == null ) {
            return null;
        }

        UserEntity.UserEntityBuilder userEntity = UserEntity.builder();

        userEntity.username( request.getUsername() );
        userEntity.email( request.getEmail() );
        userEntity.avatarUrl( request.getAvatarUrl() );

        return userEntity.build();
    }
}
