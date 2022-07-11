package com.example.reward.exception;

import java.util.Arrays;

public enum ExceptionCode {
    NOT_FOUND("REWARD_001", "No data found for given ID", false),
    MAX_THREE_MONTH_LIMIT("REWARD_002", "You cannot get total points for more than 3 months", false),
    INCORRECT_CUSTOMER_ID("REWARD_003", "Customer ID cannot be blank", false);

    private final String message;
    private final String code;
    private final boolean pattern;

    ExceptionCode(String code, String message, boolean pattern) {
        this.code = code;
        this.message = message;
        this.pattern = pattern;
    }

    public String getMessage() {
        return message;
    }

    public String getCode() {
        return code;
    }

    public boolean isPattern() {
        return pattern;
    }

    public static ExceptionCode fromCode(String code) {
        return Arrays.stream(ExceptionCode.values())
                .filter(area -> (area.getCode().equals(code) || area.name().equals(code)))
                .findFirst().orElseThrow(() -> new IllegalArgumentException(code));
    }

    public String getMessage(Object... params) {
        if (pattern) {
            return String.format(message, params);
        }
        return message;
    }
}
