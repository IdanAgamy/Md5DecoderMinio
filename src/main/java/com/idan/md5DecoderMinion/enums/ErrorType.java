package com.idan.md5DecoderMinion.enums;

public enum ErrorType {
    INCORRECT_VALIDATION(701),
    HTTP_REQUEST_ERROR(702),
    SYSTEM_ERROR(703);

    private int number;

    private ErrorType(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }
}
