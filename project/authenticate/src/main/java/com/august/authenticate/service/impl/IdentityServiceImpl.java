package com.august.authenticate.service.impl;

import com.august.authenticate.constant.DefaultRoles;
import com.august.authenticate.dto.requests.UserCreateRequest;
import com.august.authenticate.dto.responses.UserResponse;
import com.august.authenticate.entity.RoleEntity;
import com.august.authenticate.entity.UserEntity;
import com.august.authenticate.mapper.UserMapper;
import com.august.authenticate.repository.RoleRepository;
import com.august.authenticate.repository.UserRepository;
import com.august.authenticate.service.IdentityService;
import com.august.protocol.profile.CreateProfileRequest;
import com.august.protocol.profile.CreateProfileResponse;
import com.august.protocol.profile.ProfileServiceGrpc;
import com.august.shared.enums.ErrorCode;
import com.august.shared.exception.AppCustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import net.devh.boot.grpc.client.inject.GrpcClient;
import java.util.HashSet;
import java.util.Set;


@Service
@RequiredArgsConstructor
@Slf4j
public class IdentityServiceImpl implements IdentityService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;

    @GrpcClient("profile-service-grpc")
    private ProfileServiceGrpc.ProfileServiceBlockingStub profileServiceBlockingStub;

    @Override
    public UserResponse createUser(UserCreateRequest request) {
        boolean existedEmail = userRepository.existsByEmail(request.getEmail());
        boolean existedUsername = userRepository.existsByUsername(request.getUsername());
        if (Boolean.TRUE.equals(existedEmail)){
            throw new AppCustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        if (Boolean.TRUE.equals(existedUsername)) throw new AppCustomException(ErrorCode.USERNAME_ALREADY_EXISTS);

        Set<RoleEntity> roleEntities = new HashSet<>();

        roleRepository.findById(DefaultRoles.USER_ROLE).ifPresent(roleEntities::add);
        roleRepository.saveAll(roleEntities);

        UserEntity user = userMapper.mapToEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(roleEntities);
        userRepository.save(user);

        CreateProfileRequest profileRequest = CreateProfileRequest.newBuilder()
                .setUserId(user.getId())
                .setUsername(user.getUsername())
                .setEmail(user.getEmail())
                .setAvatarUrl(user.getAvatarUrl())
                .build();

        try{
            CreateProfileResponse profileResponse = profileServiceBlockingStub.createProfile(profileRequest);
            log.info("User profile id: {}", profileResponse.getProfileId());
            log.info("Message: {}", profileResponse.getMessage());

        } catch (Exception e) {
            throw new AppCustomException(ErrorCode.DO_NOT_CONNECT_TO_GRPC);
        }

        return userMapper.mapToResponse(user);
    }
}
