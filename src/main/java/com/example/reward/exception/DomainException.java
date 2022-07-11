package com.example.reward.exception;

public class DomainException extends RuntimeException {
    private static final long serialVersionUID = 2730998519474441772L;
    private ExceptionCode domainCode;
    private Object[] messageParams;

    public DomainException(ExceptionCode code) {
        this(code.getMessage(), code);
    }

    public DomainException(ExceptionCode code, Object... messageParams) {
        this(code.getMessage(), code);
        this.messageParams = messageParams;
    }

    private DomainException(String message, ExceptionCode code) {
        super(message);
        domainCode = code;
    }

    public ExceptionCode getDomainCode() {
        return domainCode;
    }

    @Override
    public String getMessage() {
        if (messageParams != null) {
            return String.format(domainCode.getMessage(), messageParams);
        }
        return domainCode.getMessage();
    }

}
