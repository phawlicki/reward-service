package com.example.reward.exception;

public class ValidationException extends RuntimeException {
    private static final long serialVersionUID = -4756676200544514832L;
    private ExceptionCode exceptionCode;
    private Object[] messageParams;

    public ValidationException(ExceptionCode code) {
        this(code.getMessage(), code);
    }

    public ValidationException(ExceptionCode code, Object... messageParams) {
        this(code.getMessage(), code);
        this.messageParams = messageParams;
    }

    private ValidationException(String message, ExceptionCode code) {
        super(message);
        exceptionCode = code;
    }

    public ExceptionCode getExceptionCode() {
        return exceptionCode;
    }

    @Override
    public String getMessage() {
        if (messageParams != null) {
            return String.format(exceptionCode.getMessage(), messageParams);
        }
        return exceptionCode.getMessage();
    }
}
