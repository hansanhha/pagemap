package com.bintage.pagemap.storage.infrastructure.web.restful.dto;


import java.util.Map;

public class ResponseMessage {

    public static final String MESSAGE_NAME = "message";
    public static final String SUCCESS = "success";

    public static Map<String, String> success() {
        return Map.of(MESSAGE_NAME, SUCCESS);
    }
}
