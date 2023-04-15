package ru.timur.project.Hubr.util.exceptions.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TagErrorResponse {
    private String message;
    private long timestamp;

    public TagErrorResponse() {}

    public TagErrorResponse(String message, long timestamp) {
        this.message = message;
        this.timestamp = timestamp;
    }

}
