package com.august.shared.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    INTERNAL_SERVER_ERROR("E000", "An unexpected error occurred on the server.", HttpStatus.INTERNAL_SERVER_ERROR),
    USER_NOT_FOUND("E001","User not found", HttpStatus.NOT_FOUND),
    USERNAME_ALREADY_EXISTS("E002","User identity already exists in our system.", HttpStatus.NOT_FOUND),
    EMAIL_ALREADY_EXISTS("E003","This email address is already registered.", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED_PASSWORD_INVALID("E004","Invalid credentials, please check your password.", HttpStatus.UNAUTHORIZED),
    PAYLOAD_TOO_LARGE("E005", "The provided data size exceeds the allowed limit.", HttpStatus.REQUEST_HEADER_FIELDS_TOO_LARGE),
    METHOD_REQUEST_INVALID("E006", "Request validation failed. Please check your request.", HttpStatus.BAD_REQUEST),
    ACCESS_DENIED("E007", "You do not have permission to access this resource.", HttpStatus.FORBIDDEN),
    UPLOAD_FAILED("E008", "Something went wrong when you upload file/images.", HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED("E008", "Full authentication is required to access this resource.", HttpStatus.UNAUTHORIZED),
    ACCOUNT_TEMPORARILY_LOCKED("E008",
            "Unfortunately! You must wait five minutes cause that system is locked temporarily.", HttpStatus.FORBIDDEN),
    UPLOAD_FILE_ERROR("E009", "An unexpected error occurred on the server.", HttpStatus.INTERNAL_SERVER_ERROR),
    EMAIL_IS_EMPTY("E0010", "Email empty in body.", HttpStatus.BAD_REQUEST),
    JSON_INVALID("E0011", "Invalid Json", HttpStatus.BAD_REQUEST),
    BODY_IS_BEING_MISSED("E0012", "Body is being missed.", HttpStatus.BAD_REQUEST),
    TOO_MANY_REQUEST("E0013", "Rate limit exceeded. Please try again in %d seconds.", HttpStatus.TOO_MANY_REQUESTS),
    DATA_RESPONSE_SUCCESSFULLY("E0014", "Data response successfully", HttpStatus.OK),
    INVALID_TOKEN("E0015", "Invalid Token", HttpStatus.BAD_REQUEST),
    DO_NOT_CONNECT_TO_GRPC("E0016", "Do no connect to gRPC", HttpStatus.BAD_REQUEST),
    EMAIL_NOT_FOUND("E0017", "Email not found", HttpStatus.NOT_FOUND),
    INPUT_REQUIREMENT("E0018", "Input requirement", HttpStatus.BAD_REQUEST),
    DO_NOT_CONNECT_TO_ELASTICSEARCH("E0019", "Don't connect to ElasticSearch", HttpStatus.BAD_REQUEST),
    CATEGORY_NOT_FOUND("E0020", "Category not found", HttpStatus.NOT_FOUND),
    NOT_FOUND_POST_BY_SLUG("E0021", "Post not found by slug", HttpStatus.NOT_FOUND);

    private final String code;
    private final String message;
    private final HttpStatus statusCode;

    ErrorCode(String code, String message, HttpStatus statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

}