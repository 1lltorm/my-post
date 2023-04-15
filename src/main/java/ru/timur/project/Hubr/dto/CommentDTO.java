package ru.timur.project.Hubr.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import ru.timur.project.Hubr.models.Post;
import ru.timur.project.Hubr.models.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

@Getter
@Setter
public class CommentDTO {

    public int id;

    public User owner;

    @NotEmpty(message = "message should be not empty")
    public String body;

    private LocalDateTime createdAt;

    public Post post;

    public String getDateForComment() {
        long date = ChronoUnit.MINUTES.between(createdAt, LocalDateTime.now());

        if (date <= 60)
            return date + " minutes ago";

        date = ChronoUnit.HOURS.between(createdAt, LocalDateTime.now());

        if (date <= 24)
            return date + " hours ago";

        DateTimeFormatter format = DateTimeFormatter.ofPattern("d MMMM 'at' HH:mm", Locale.ENGLISH);

        return createdAt.format(format);
    }

}
