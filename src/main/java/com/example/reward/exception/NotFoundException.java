package com.example.reward.exception;

public class NotFoundException extends RuntimeException {
    private static final long serialVersionUID = 7064922183931621882L;
    private ExceptionCode exceptionCode;
    private Object[] messageParams;

    public NotFoundException(ExceptionCode code) {
        this(code.getMessage(), code);
    }

    public NotFoundException(ExceptionCode code, Object... messageParams) {
        this(code.getMessage(), code);
        this.messageParams = messageParams;
    }

    private NotFoundException(String message, ExceptionCode code) {
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
