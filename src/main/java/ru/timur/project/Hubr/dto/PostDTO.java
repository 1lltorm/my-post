package ru.timur.project.Hubr.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import ru.timur.project.Hubr.models.Comment;
import ru.timur.project.Hubr.models.Tag;
import ru.timur.project.Hubr.models.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

@Getter
@Setter
public class PostDTO {

    private int id;

    @NotEmpty(message = "Header should be not empty")
    @Size(min = 0, max = 100, message = "Header should be is no longer then 100")
    private String header;

    @NotEmpty(message = "Body should be not empty")
    @Size(min = 0, max = 10000, message = "Body should be is no longer then 10000")
    private String body;

    private String cover;

    private int views;

    private List<Tag> tagList;

    private LocalDateTime createdAt;

    private User owner;

    public List<Comment> comments;

    public String getDateForPost() {
        long date = ChronoUnit.MINUTES.between(createdAt, LocalDateTime.now());

        if (date <= 60)
            return date + " minutes ago";

        date = ChronoUnit.HOURS.between(createdAt, LocalDateTime.now());

        if (date <= 24)
            return date + " hours ago";

        DateTimeFormatter format = DateTimeFormatter.ofPattern("d MMMM 'at' HH:mm", Locale.ENGLISH);

        return createdAt.format(format);
    }

    public String getNotFullBody() {
        if (body.length() <= 400) return body;
        return body.substring(0,400);
    }

}
