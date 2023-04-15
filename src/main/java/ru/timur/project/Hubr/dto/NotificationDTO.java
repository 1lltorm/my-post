package ru.timur.project.Hubr.dto;

import lombok.Getter;
import lombok.Setter;
import ru.timur.project.Hubr.models.Notification;
import ru.timur.project.Hubr.models.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class NotificationDTO {

    private int id;

    private String message;

    private String type;

    private LocalDateTime createdAt;

    private String link;

    private List<User> whoHaveThisMessage;

    private User owner;

    public static Notification sendNotification(User owner, String link, String message, String type) {
        Notification notification = new Notification(LocalDateTime.now(), owner);
        notification.setLink(link);
        notification.setMessage(message);
        notification.setType(type);
        notification.setWhoHaveThisMessage(new ArrayList<User>());

        return notification;
    }

}
