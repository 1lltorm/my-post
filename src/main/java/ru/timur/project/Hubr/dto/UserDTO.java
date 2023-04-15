package ru.timur.project.Hubr.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import ru.timur.project.Hubr.models.Notification;
import ru.timur.project.Hubr.models.Post;
import ru.timur.project.Hubr.models.Tag;
import ru.timur.project.Hubr.models.User;

import java.util.List;

@Getter
@Setter
public class UserDTO {

    private int id;

    //TODO Полностью настроить валидацию

    @NotEmpty(message = "Username should be is not empty")
    private String username;

    @NotEmpty(message = "Email should be is not empty")
    private String email;

    @NotEmpty(message = "Email confirmation should be is not empty")
    private String emailConf;

    @NotEmpty(message = "Password should be is not empty")
    private String password;

    @NotEmpty(message = "Password confirmation should be is not empty")
    private String passwordConf;

    @NotEmpty(message = "Current should be is not empty")
    private String currentPassword;

    @Size(min = 0, max = 40, message = "Full name should be between no longer then 40")
    private String fullName;

    @Size(min = 0, max = 50, message = "About yourself name should be no longer then 50")
    private String description;

    private String gender;

    private String avatar;

    private String country;

    private String role;

    private List<Tag> subscriptionTags;

    private boolean active;

    private List<Post> postList;

    private List<User> subscribers;

    private List<User> subscriptions;

    private int notReadNotifications;

    private List<Notification> userNotifications;

    public boolean isAdmin() {
        return role.equals("ROLE_ADMIN");
    }

    public UserDTO() {}

    public UserDTO(int id, String role, boolean active, String avatar, int notReadNotifications) {
        this.id = id;
        this.role = role;
        this.active = active;
        this.avatar = avatar;
        this.notReadNotifications = notReadNotifications;
    }
}
