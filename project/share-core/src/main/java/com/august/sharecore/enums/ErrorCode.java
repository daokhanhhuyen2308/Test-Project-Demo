package com.august.sharecore.enums;

import lombok.Getter;

@Getter
public enum ErrorCode {
    INTERNAL_SERVER_ERROR("E000", "An unexpected error occurred on the server.", 500),
    USER_NOT_FOUND("E001","User not found", 404),
    USERNAME_ALREADY_EXISTS("E002","User identity already exists in our system.", 409),
    EMAIL_ALREADY_EXISTS("E003","This email address is already registered.", 409),
    UNAUTHORIZED_PASSWORD_INVALID("E004","Invalid credentials, please check your password.", 401),
    PAYLOAD_TOO_LARGE("E005", "The provided data size exceeds the allowed limit.", 431),
    METHOD_REQUEST_INVALID("E006", "Request validation failed. Please check your request.", 400),
    ACCESS_DENIED("E007", "You do not have permission to access this resource.", 403),
    UPLOAD_FAILED("E008", "Something went wrong when you upload file/images.", 400),
    UNAUTHENTICATED("E009", "Full authentication is required to access this resource.", 401),
    ACCOUNT_TEMPORARILY_LOCKED("E0010",
            "Unfortunately! You must wait five minutes cause that system is locked temporarily.", 403),
    UPLOAD_FILE_ERROR("E0011", "An unexpected error occurred on the server.", 500),
    EMAIL_IS_EMPTY("E0012", "Email empty in body.", 400),
    JSON_INVALID("E0013", "Invalid Json", 400),
    BODY_IS_BEING_MISSED("E0014", "Body is being missed.", 400),
    TOO_MANY_REQUEST("E0015", "Rate limit exceeded. Please try again in %d seconds.", 429),
    DATA_RESPONSE_SUCCESSFULLY("E0016", "Data response successfully", 200),
    INVALID_TOKEN("E0017", "Invalid Token", 400),
    DO_NOT_CONNECT_TO_GRPC("E0018", "Do no connect to gRPC", 400),
    EMAIL_NOT_FOUND("E0019", "Email not found", 404),
    INPUT_REQUIREMENT("E0020", "Input requirement", 400),
    DO_NOT_CONNECT_TO_ELASTICSEARCH("E0021", "Don't connect to ElasticSearch", 400),
    CATEGORY_NOT_FOUND("E0022", "Category not found", 404),
    NOT_FOUND_POST_BY_SLUG("E0023", "Post not found by slug", 404),
    INVALID_INPUT("E0024", "Invalid input", 400),
    SLUG_IS_REQUIRED("E0025", "Slug is required", 400),
    BAD_WORD_DETECTED("E0026", "Bad word found", 400),
    SPAM_LINK_DETECTED("E0027", "No links allowed! Spam detected.", 400),
    CAN_NOT_CONNECT_KEYCLOAK("E0028", "Cannot create user in Keycloak.", 400),
    USER_ALREADY_EXISTS("E0029", "User already exists.", 400),
    KEYCLOAK_FORBIDDEN("E0029", "User already exists.", 400),
    AVATAR_FAIL_CONTENT_TYPE("E0030", "Avatar must be an image content type.", 400),
    THUMBNAIL_FAIL_CONTENT_TYPE("E0031", "Thumbnail must be an image content type.", 400),
    ATTACHMENT_IS_EMPTY("E0032", "Attach file is empty", 400),
    AVATAR_FAIL_TOO_LARGE("E0033", "AVATAR file is too large. max=2MB.", 400),
    THUMBNAIL_FAIL_TOO_LARGE("E0034", "THUMBNAIL file is too large. max=5MB.", 400),
    ATTACHMENT_FAIL_TOO_LARGE("E0035", "ATTACHMENT file is too large. max=20MB.", 400),
    INVALID_FILE_PURPOSE("E0036", "Invalid file purpose.", 400),
    SEND_EMAIL_FAILED("E0037", "Send email failed.", 400),
    EMAIL_DELIVERY_FAILED("E0037", "Email delivery failed, triggering retry mechanism...", 400),
    CANNOT_SERIALIZE_EVENT("E0038", "Can not serialize event.", 400),
    CANNOT_DESERIALIZE_EVENT("E0038", "Can not deserialize event.", 400),
    POST_NOT_FOUND_BY_ID("E0039", "Could not be found post by post id.", 404),
    UNAUTHORIZED_UPDATE_POST("E0040", "Only the post owner is authorized to update this post.", 403),
    SUCCESS("0000", "Success", 200);

    private final String code;
    private final String message;
    private final int statusCode;

    ErrorCode(String code, String message, int statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

}