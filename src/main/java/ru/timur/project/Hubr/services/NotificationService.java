package ru.timur.project.Hubr.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.timur.project.Hubr.dto.NotificationDTO;
import ru.timur.project.Hubr.models.Notification;
import ru.timur.project.Hubr.models.User;
import ru.timur.project.Hubr.repositoris.NotificationRepository;
import ru.timur.project.Hubr.repositoris.UserRepository;
import ru.timur.project.Hubr.security.UserDetails;
import ru.timur.project.Hubr.util.exceptions.NotificationNotFoundException;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    protected final UserService userService;

    @Autowired
    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository, UserService userService) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    public List<Notification> findAll() {
        return notificationRepository.findAll();
    }

    public Notification findById(int id) {
        return notificationRepository.findById(id).orElseThrow(NotificationNotFoundException::new);
    }

    @Transactional
    public void save(Notification notification) {
        notificationRepository.save(notification);
    }

    @Transactional
    public void deleteAllNotifications(User user) {
        List<Notification> notifications = user.getUserNotifications();

        for (Notification notification : notifications) {
            notification.getWhoHaveThisMessage().remove(user);
            save(notification);

            if (notification.getWhoHaveThisMessage().isEmpty())
                notificationRepository.deleteById(notification.getId());

        }

        readAllNotifications(user);
    }

    @Transactional
    public void readAllNotifications(User user) {
        user.setNotReadNotifications(0);
        userRepository.save(user);
        refreshPrincipal();
    }

    @Transactional
    public void notifySubscribers(User user, int postId) {
        if (user.getSubscribers() == null || user.getSubscribers().isEmpty())
            return;

        String link = "/posts/" + postId;
        String message = "User: " + user.getUsername() + " published new post";
        String type = "publication";

        List<User> subscribers = user.getSubscribers();
        Notification notification = NotificationDTO.sendNotification(user, link, message, type);

        for (int i = 0; i < subscribers.size(); i++) {
            notification.getWhoHaveThisMessage().add(subscribers.get(i));
            subscribers.get(i).setNotReadNotifications(subscribers.get(i).getNotReadNotifications() + 1);
        }

        notificationRepository.save(notification);

    }


    @Transactional
    public void saveAndSendNotification(User owner, Notification notification) {
        owner.setNotReadNotifications(owner.getNotReadNotifications() + 1);

        List<Integer> arr = new ArrayList<>();

        notification.getWhoHaveThisMessage().add(owner);

        notificationRepository.save(notification);
        refreshPrincipal();
    }

    private void refreshPrincipal() {
        UserDetails userDetails = new UserDetails(userService.findById(userService.findByPrincipal().getId()));
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(), userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

}
