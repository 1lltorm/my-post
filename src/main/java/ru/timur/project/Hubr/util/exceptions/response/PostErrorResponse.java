package ru.timur.project.Hubr.util.exceptions.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostErrorResponse {
    private String message;
    private long timestamp;

    public PostErrorResponse() {}

    public PostErrorResponse(String message, long timestamp) {
        this.message = message;
        this.timestamp = timestamp;
    }
}
