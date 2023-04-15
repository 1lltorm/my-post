package ru.timur.project.Hubr.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "usr")
@Getter
@Setter
public class User {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "username")
    private String username;

    @Column(name = "email")
    private String email;

    @Column(name = "second_email")
    private String secondEmail;

    @Column(name = "password")
    private String password;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "description")
    private String description;

    @Column(name = "gender")
    private String gender;

    @Column(name = "avatar")
    private String avatar;

    @Column(name = "country")
    private String country;

    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Column(name = "role")
    private String role;

    @Column(name = "activation_code")
    private String activationCode;

    @Column(name = "active")
    private boolean active;

    @OneToMany(mappedBy = "owner",cascade = CascadeType.MERGE, fetch = FetchType.LAZY) //
    private List<Post> postList;

    @ManyToMany(mappedBy = "subscribedUsers",cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    private List<Tag> subscriptionTags;

    @OneToMany(mappedBy = "tagOwner", fetch = FetchType.LAZY)
    private List<Tag> tagsList;

    @ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @JoinTable (
            name = "chanel_subscriber",
            joinColumns = { @JoinColumn(name = "chanel_id") },
            inverseJoinColumns = { @JoinColumn(name =  "subscriber_id") })
    private List<User> subscribers;

    @ManyToMany(mappedBy = "subscribers",cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    private List<User> subscriptions;

    public boolean isAdmin() {
        return role.equals("ROLE_ADMIN");
    }

    @Column(name = "not_read_notifications")
    private int notReadNotifications;

    @ManyToMany(mappedBy = "whoHaveThisMessage",cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private List<Notification> userNotifications;

    public User() {}

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }
}
