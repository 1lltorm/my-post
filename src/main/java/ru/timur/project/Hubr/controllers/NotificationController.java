package ru.timur.project.Hubr.controllers;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.timur.project.Hubr.dto.NotificationDTO;
import ru.timur.project.Hubr.models.Notification;
import ru.timur.project.Hubr.services.NotificationService;
import ru.timur.project.Hubr.services.UserService;

import java.util.List;

@Controller
@PreAuthorize("isAuthenticated()")
@RequestMapping("/notifications")
public class NotificationController {
    private final UserService userService;
    private final NotificationService notificationService;
    private final ModelMapper mapper;

    @Autowired
    public NotificationController(UserService userService, NotificationService notificationService, ModelMapper mapper) {
        this.userService = userService;
        this.notificationService = notificationService;
        this.mapper = mapper;
    }

    @GetMapping
    public String notifications(Model model) {
        List<Notification> notificationList = userService.findById(userService.findByPrincipal().getId()).getUserNotifications();
        model.addAttribute("notifications", notificationList.stream().map(this::convertNotificationDTO).toList());
        model.addAttribute("currentUser", userService.findByPrincipal());

        return "notification/userNotifications";
    }

    @GetMapping("/deleteAll")
    public String deleteAllNotifications() {
        notificationService.deleteAllNotifications(userService.findById(userService.findByPrincipal().getId()));

        return "redirect:/notifications";
    }

    @GetMapping("/read-all")
    public String readAllNotifications() {
        notificationService.readAllNotifications(userService.findById(userService.findByPrincipal().getId()));

        return "redirect:/notifications";
    }

    private NotificationDTO convertNotificationDTO(Notification notification) {
        return mapper.map(notification, NotificationDTO.class);
    }

}
