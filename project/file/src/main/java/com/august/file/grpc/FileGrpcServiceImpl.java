package com.august.file.grpc;

import com.august.file.dto.FileResponse;
import com.august.file.service.FileService;
import com.august.protocol.file.*;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
public class FileGrpcServiceImpl extends FileServiceGrpc.FileServiceImplBase {
    private final FileService fileService;

    @Override
    public void uploadFile(UploadFileRequest request, StreamObserver<UploadFileResponse> responseObserver) {
        FileResponse fileResponse = fileService.uploadFileFromGrpc(request);

        try{
        UploadFileResponse uploadFileResponse = UploadFileResponse.newBuilder()
                .setFileId(fileResponse.getId())
                .setUrl(fileResponse.getFilePath())
                .setPurpose(request.getPurpose())
                .setStatus(UploadStatus.SUCCESS)
                .build();

        responseObserver.onNext(uploadFileResponse);
        responseObserver.onCompleted();
        } catch (Exception e) {
            UploadFileResponse uploadFileResponse = UploadFileResponse.newBuilder()
                    .setPurpose(request.getPurpose())
                    .setUrl(fileResponse.getFilePath())
                    .setStatus(UploadStatus.FAILED)
                    .build();

            responseObserver.onNext(uploadFileResponse);
            responseObserver.onCompleted();
        }
    }
}
