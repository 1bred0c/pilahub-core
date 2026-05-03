package fpt.edu.sep490.pilahub.exception;

public class WalletNotActiveException extends RuntimeException {
    public WalletNotActiveException(String message) {
        super(message);
    }
}
