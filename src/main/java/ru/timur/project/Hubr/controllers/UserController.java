package ru.timur.project.Hubr.controllers;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.timur.project.Hubr.dto.PostDTO;
import ru.timur.project.Hubr.dto.UserDTO;
import ru.timur.project.Hubr.models.Post;
import ru.timur.project.Hubr.models.User;
import ru.timur.project.Hubr.services.UserService;
import ru.timur.project.Hubr.util.exceptions.UserNotFoundException;
import ru.timur.project.Hubr.util.exceptions.response.UserErrorResponse;

import java.util.List;

@Controller
@RequestMapping("/users")
@PreAuthorize("isAuthenticated()")
public class UserController {
    private final UserService userService;
    private final ModelMapper mapper;

    public UserController(UserService userService, ModelMapper mapper) {
        this.userService = userService;
        this.mapper = mapper;
    }

    @PreAuthorize("permitAll()")
    @GetMapping("/{id}")
    public String user(
            @PathVariable("id") int id,
            Model model) {
        User user = userService.findById(id);
        if (!user.isActive())
            return "util/banUser";

        setProfile(id, model);

        setProfile(id, model);

        model.addAttribute("users",
                convertToUserDTO(user).getSubscribers().stream().map(this::convertToUserDTO).toList());

        return "user/profile/profile";
    }

    @PreAuthorize("permitAll()")
    @GetMapping("/{id}/posts")
    public String posts(
            @PathVariable("id") int id,
            Model model,
            @PageableDefault(sort = {"id"}, direction = Sort.Direction.DESC) Pageable pageable) {

        Page<PostDTO> posts = new PageImpl<>(
                userService.findById(id).getPostList().stream().map(this::convertToPostDTO).toList(), pageable, 10
        );
        User user = userService.findById(id);

        if (!user.isActive())
            return "util/banUser";

        setProfile(id, model);

        model.addAttribute("posts", posts);

        return "user/profile/posts";
    }

    @PreAuthorize("permitAll()")
    @GetMapping("/{id}/subscribers")
    public String subscribers(@PathVariable("id") int id, Model model) {
        User user = userService.findById(id);

        if (!user.isActive())
            return "util/banUser";

        setProfile(id, model);

        model.addAttribute("users", user.getSubscribers().stream().map(this::convertToUserDTO).toList());
        return "user/profile/sub";
    }

    @PreAuthorize("permitAll()")
    @GetMapping("/{id}/subscriptions")
    public String subscriptions(@PathVariable("id") int id, Model model) {
        User user = userService.findById(id);

        if (!user.isActive())
            return "util/banUser";

        setProfile(id, model);

        model.addAttribute("users", user.getSubscriptions().stream().map(this::convertToUserDTO).toList());
        return "user/profile/sub";
    }


    @PreAuthorize("permitAll()")
    @GetMapping("/all")
    public String users(
            Model model,
            String filter,
            @PageableDefault(sort = {"id"}, direction = Sort.Direction.DESC) Pageable pageable) {
        Page<UserDTO> page = convertToPageDTO(pageable, userService.findAll());

        if (filter != null && !filter.isEmpty())
            page = convertToPageDTO(pageable, userService.findByUsernameContaining(filter));


        model.addAttribute("url", "/users/all");
        model.addAttribute("currentUser", userService.findByPrincipal());
        model.addAttribute("users", page);

        return "user/users";
    }

    @GetMapping("/subscribe/{id}")
    public String subscribe(@PathVariable("id") int id) {
        userService.subscribe(userService.findById(userService.findByPrincipal().getId()), userService.findById(id));

        return "redirect:/users/" + id;
    }

    @GetMapping("/unsubscribe/{id}")
    public String unsubscribe(@PathVariable("id") int id) {
        userService.unsubscribe(userService.findById(userService.findByPrincipal().getId()), userService.findById(id));

        return "redirect:/users/" + id;
    }

    @ExceptionHandler
    private ResponseEntity<UserErrorResponse> handlerException(UserNotFoundException e) {
        UserErrorResponse userErrorResponse = new
                UserErrorResponse("User with this id wasn't found", System.currentTimeMillis());

        return new ResponseEntity<>(userErrorResponse, HttpStatus.NOT_FOUND); // 404 статус
    }

    private void setProfile(int id, Model model) {
        User user = userService.findById(id);
        UserDTO principal = userService.findByPrincipal();
        User currentUser = (principal == null) ? null : userService.findById(principal.getId());

        model.addAttribute("user", user);
        model.addAttribute("currentUser", currentUser);
    }

    private Page<UserDTO> convertToPageDTO(Pageable pageable, List<User> users) {
        List<UserDTO> userDTO = users.stream()
                .sorted((p1, p2) -> p2.getId() - p1.getId())
                .map(this::convertToUserDTO).toList();

        final int start = (int) pageable.getOffset();
        final int end = Math.min((start + pageable.getPageSize()), userDTO.size());
        return new PageImpl<>(userDTO.subList(start, end), pageable, userDTO.size());
    }

    private UserDTO convertToUserDTO(User user) {
        return mapper.map(user, UserDTO.class);
    }

    private PostDTO convertToPostDTO(Post post) {
        return mapper.map(post, PostDTO.class);
    }

}
