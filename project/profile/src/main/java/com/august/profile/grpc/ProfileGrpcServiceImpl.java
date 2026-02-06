package com.august.profile.grpc;

import com.august.profile.entity.UserProfile;
import com.august.profile.mapper.ProfileMapper;
import com.august.profile.repository.ProfileRepository;
import com.august.protocol.profile.CreateProfileRequest;
import com.august.protocol.profile.CreateProfileResponse;
import com.august.protocol.profile.ProfileServiceGrpc;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
public class ProfileGrpcServiceImpl extends ProfileServiceGrpc.ProfileServiceImplBase {

    private final ProfileRepository profileRepository;
    private final ProfileMapper profileMapper;

    @Override
    public void createProfile(CreateProfileRequest request, StreamObserver<CreateProfileResponse> responseObserver) {
        UserProfile userProfile = profileMapper.mapToProfileEntity(request);

        UserProfile saveUserProfile = profileRepository.save(userProfile);

        CreateProfileResponse profileResponse = CreateProfileResponse.newBuilder()
                .setProfileId(saveUserProfile.getId())
                .setMessage(true)
                .build();

        responseObserver.onNext(profileResponse);
        responseObserver.onCompleted();

    }
}