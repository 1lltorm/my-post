package ru.timur.project.Hubr.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import ru.timur.project.Hubr.models.Post;
import ru.timur.project.Hubr.models.User;

import java.util.List;

@Getter
@Setter
public class TagDTO {

    private int id;

    @NotEmpty(message = "Tag name should be is not empty")
    @Size(min = 0, max = 30, message = "Tag name should be no longer then 30")
    private String name;

    @NotEmpty(message = "Description should be is not empty")
    @Size(min = 0, max = 80, message = "Description should be no longer then 80")
    private String description;

    private String icon;

    private boolean active;

    private List<Post> postList;

    private List<User> subscribedUsers;

}
