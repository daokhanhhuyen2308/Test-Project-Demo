package com.august.profile.grpc;

import com.august.profile.dto.ProfileResponse;
import com.august.profile.service.UserProfileService;
import com.august.protocol.profile.CreateProfileRequest;
import com.august.protocol.profile.CreateProfileResponse;
import com.august.protocol.profile.ProfileServiceGrpc;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
public class ProfileGrpcServiceImpl extends ProfileServiceGrpc.ProfileServiceImplBase {

    private final UserProfileService userProfileService;

    @Override
    public void createProfile(CreateProfileRequest request, StreamObserver<CreateProfileResponse> responseObserver) {

        ProfileResponse response = userProfileService.createProfile(request);

        CreateProfileResponse profileResponse = CreateProfileResponse.newBuilder()
                .setProfileId(response.getProfileId())
                .setMessage(true)
                .build();

        responseObserver.onNext(profileResponse);
        responseObserver.onCompleted();

    }
}