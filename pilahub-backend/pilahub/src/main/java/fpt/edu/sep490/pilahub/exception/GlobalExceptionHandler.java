package fpt.edu.sep490.pilahub.exception;

import fpt.edu.sep490.pilahub.dto.response.APIResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateAccountException.class)
    public ResponseEntity<APIResponse<Void>> handleDuplicateAccount(DuplicateAccountException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(APIResponse.error(ex.getMessage(), "DUPLICATE_ACCOUNT"));
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<APIResponse<Void>> handleAccountNotFound(AccountNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(APIResponse.error(ex.getMessage(), "ACCOUNT_NOT_FOUND"));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<APIResponse<Void>> handleInvalidCredentials(InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(APIResponse.error(ex.getMessage(), "INVALID_CREDENTIALS"));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<APIResponse<Void>> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(APIResponse.error("Invalid email or password", "INVALID_CREDENTIALS"));
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<APIResponse<Void>> handleAccessDenied(org.springframework.security.access.AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(APIResponse.error("Access denied. You don't have permission to access this resource.", "ACCESS_DENIED"));
    }

    @ExceptionHandler(fpt.edu.sep490.pilahub.exception.AccessDeniedException.class)
    public ResponseEntity<APIResponse<Void>> handleCustomAccessDenied(fpt.edu.sep490.pilahub.exception.AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(APIResponse.error(ex.getMessage(), "ACCESS_DENIED"));
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<APIResponse<Void>> handleInvalidRequest(InvalidRequestException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(APIResponse.error(ex.getMessage(), "INVALID_REQUEST"));
    }

    @ExceptionHandler(InvalidOtpException.class)
    public ResponseEntity<APIResponse<Void>> handleInvalidOtp(InvalidOtpException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(APIResponse.error(ex.getMessage(), "INVALID_OTP"));
    }

    @ExceptionHandler(AccountNotVerifiedException.class)
    public ResponseEntity<APIResponse<Void>> handleAccountNotVerified(AccountNotVerifiedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(APIResponse.error(ex.getMessage(), "ACCOUNT_NOT_VERIFIED"));
    }

    @ExceptionHandler(TraineeNotFoundException.class)
    public ResponseEntity<APIResponse<Void>> handleTraineeNotFound(TraineeNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(APIResponse.error(ex.getMessage(), "TRAINEE_NOT_FOUND"));
    }

    @ExceptionHandler(HealthProfileNotFoundException.class)
    public ResponseEntity<APIResponse<Void>> handleHealthProfileNotFound(HealthProfileNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(APIResponse.error(ex.getMessage(), "HEALTH_PROFILE_NOT_FOUND"));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<APIResponse<Void>> handleResourceNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(APIResponse.error(ex.getMessage(), "RESOURCE_NOT_FOUND"));
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<APIResponse<Void>> handleDuplicateResource(DuplicateResourceException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(APIResponse.error(ex.getMessage(), "DUPLICATE_RESOURCE"));
    }

    @ExceptionHandler(IotDeviceNotFoundException.class)
    public ResponseEntity<APIResponse<Void>> handleIotDeviceNotFound(IotDeviceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(APIResponse.error(ex.getMessage(), "IOT_DEVICE_NOT_FOUND"));
    }

    @ExceptionHandler(FitnessGoalNotFoundException.class)
    public ResponseEntity<APIResponse<Void>> handleFitnessGoalNotFound(FitnessGoalNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(APIResponse.error(ex.getMessage(), "FITNESS_GOAL_NOT_FOUND"));
    }

    @ExceptionHandler(DuplicateIotDeviceException.class)
    public ResponseEntity<APIResponse<Void>> handleDuplicateIotDevice(DuplicateIotDeviceException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(APIResponse.error(ex.getMessage(), "DUPLICATE_IOT_DEVICE"));
    }

    @ExceptionHandler(WalletNotFoundException.class)
    public ResponseEntity<APIResponse<Void>> handleWalletNotFound(WalletNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(APIResponse.error(ex.getMessage(), "WALLET_NOT_FOUND"));
    }

    @ExceptionHandler(WalletAlreadyExistsException.class)
    public ResponseEntity<APIResponse<Void>> handleWalletAlreadyExists(WalletAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(APIResponse.error(ex.getMessage(), "WALLET_ALREADY_EXISTS"));
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<APIResponse<Void>> handleInsufficientBalance(InsufficientBalanceException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(APIResponse.error(ex.getMessage(), "INSUFFICIENT_BALANCE"));
    }

    @ExceptionHandler(WalletNotActiveException.class)
    public ResponseEntity<APIResponse<Void>> handleWalletNotActive(WalletNotActiveException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(APIResponse.error(ex.getMessage(), "WALLET_NOT_ACTIVE"));
    }

    @ExceptionHandler(TransactionNotFoundException.class)
    public ResponseEntity<APIResponse<Void>> handleTransactionNotFound(TransactionNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(APIResponse.error(ex.getMessage(), "TRANSACTION_NOT_FOUND"));
    }

    @ExceptionHandler(NotificationNotFoundException.class)
    public ResponseEntity<APIResponse<Void>> handleNotificationNotFound(NotificationNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(APIResponse.error(ex.getMessage(), "NOTIFICATION_NOT_FOUND"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<APIResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(APIResponse.<Map<String, String>>builder()
                        .success(false)
                        .message("Validation failed")
                        .errorCode("VALIDATION_ERROR")
                        .data(errors)
                        .build());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<APIResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(APIResponse.error(ex.getMessage(), "INVALID_ARGUMENT"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<APIResponse<Void>> handleGeneralException(Exception ex) {
        ex.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(APIResponse.error("An unexpected error occurred", "INTERNAL_SERVER_ERROR"));
    }
}
