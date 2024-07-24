package com.bintage.pagemap.auth.application.dto;

public record DeleteAccountDto(String accountIdStr,
                               int cause,
                               String feedback) {

    public static DeleteAccountDto of(String accountIdStr, int cause, String feedback) {
        return new DeleteAccountDto(accountIdStr, cause, feedback);
    }
}
