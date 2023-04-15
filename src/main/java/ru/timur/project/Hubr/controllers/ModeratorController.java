package ru.timur.project.Hubr.controllers;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.timur.project.Hubr.dto.PostDTO;
import ru.timur.project.Hubr.dto.TagDTO;
import ru.timur.project.Hubr.dto.UserDTO;
import ru.timur.project.Hubr.enums.Role;
import ru.timur.project.Hubr.models.Post;
import ru.timur.project.Hubr.models.Tag;
import ru.timur.project.Hubr.models.User;
import ru.timur.project.Hubr.services.PostService;
import ru.timur.project.Hubr.services.TagService;
import ru.timur.project.Hubr.services.UserService;

import java.util.List;

@Controller
@PreAuthorize("hasRole('ROLE_ADMIN')")
@RequestMapping("/moderation")
public class ModeratorController {
    private final UserService userService;
    private final TagService tagService;
    private final PostService postService;
    private final ModelMapper mapper;

    @Autowired
    public ModeratorController(UserService userService, TagService tagService, PostService postService, ModelMapper mapper) {
        this.userService = userService;
        this.tagService = tagService;
        this.postService = postService;
        this.mapper = mapper;
    }

    @GetMapping
    public String mainPage(
            Model model,
            @PageableDefault(sort = {"id"}, direction = Sort.Direction.DESC) Pageable pageable) {
        Page<PostDTO> page = convertToPagePost(pageable, postService.findAll());

        model.addAttribute("posts", page);
        model.addAttribute("currentUser", userService.findByPrincipal());

        return "moderation/moderation";
    }

    @GetMapping("/posts/{id}")
    public String post(@PathVariable("id") int id, Model model) {
        model.addAttribute("currentUser", userService.findByPrincipal());
        model.addAttribute("post", convertToPostDTO(postService.findById(id)));

        return "moderation/post";
    }

    @PatchMapping("/posts/{id}")
    public String allowPublishing(@PathVariable("id") int id) {
        postService.allowPublishing(postService.findById(id));

        return "redirect:/moderation/offers/posts";
    }

    @DeleteMapping("/posts/{id}")
    public String notAllowPublishing(@PathVariable("id") int id) {
        postService.notAllowPublishing(postService.findById(id));

        return "redirect:/moderation/offers/posts";
    }

    @GetMapping("/users")
    public String users(
            Model model,
            @PageableDefault(sort = {"id"}, direction = Sort.Direction.DESC) Pageable pageable) {
        Page<UserDTO> page = convertToPageUser(pageable, userService.findAll());

        model.addAttribute("users", page);
        model.addAttribute("currentUser", userService.findByPrincipal());

        return "moderation/users";
    }

    @GetMapping("/users/{id}")
    public String user(@PathVariable("id") int id, Model model) {
        model.addAttribute("currentUser", userService.findByPrincipal());
        model.addAttribute("user", convertToUserDTO(userService.findById(id)));
        model.addAttribute("roles", Role.values());

        return "moderation/user";
    }

    @PatchMapping("/ban-user/{id}")
    public String banUser(@PathVariable("id") int id, Model model) {
        userService.banUser(userService.findById(id));
        model.addAttribute("currentUser", userService.findByPrincipal());
        model.addAttribute("user", convertToUserDTO(userService.findById(id)));
        model.addAttribute("message", "User has been baned");

        return "moderation/user";
    }

    @PatchMapping("/{id}/set-role")
    public String setRole(
            @PathVariable("id") int id,
            @ModelAttribute("user") UserDTO userDTO) {

        userService.updateUserRole(id, convertToUser(userDTO));
        return "redirect:/moderation/users/" + id;
    }

    @PatchMapping("/unban-user/{id}")
    public String unbanUser(@PathVariable("id") int id, Model model) {
        userService.unbanUser(userService.findById(id));

        model.addAttribute("currentUser", userService.findByPrincipal());
        model.addAttribute("user", convertToUserDTO(userService.findById(id)));
        model.addAttribute("message", "User has been unban");

        return "moderation/user";
    }

    @GetMapping("/tags")
    public String tags(
            Model model,
            @PageableDefault(sort = {"id"}, direction = Sort.Direction.DESC) Pageable pageable) {
        Page<TagDTO> page = convertToPageTag(pageable, tagService.findAll());

        model.addAttribute("currentUser", userService.findByPrincipal());
        model.addAttribute("tags", page);

        return "moderation/tags";
    }

    @GetMapping("/tags/{id}")
    public String findTag(@PathVariable("id") int id,
                          Model model) {
        model.addAttribute("currentUser", userService.findByPrincipal());
        model.addAttribute("tag", convertToTagDTO(tagService.findById(id)));

        return "moderation/tag";
    }

    @PatchMapping("/tags/{id}")
    public String postTag(@PathVariable("id") int id) {
        tagService.setActiveType(id);
        return "redirect:/moderation/offers/tags";
    }

    @DeleteMapping("/tags/{id}")
    public String deleteTag(@PathVariable("id") int id) {
        tagService.delete(tagService.findById(id));
        return "redirect:/moderation/offers/tags";
    }

    @GetMapping("/offers/posts")
    public String postOffers(
            Model model,
            @PageableDefault(sort = {"id"}, direction = Sort.Direction.DESC) Pageable pageable) {
        Page<PostDTO> page = convertToPagePost(pageable, postService.findNoActivePost());



        model.addAttribute("posts", page);
        model.addAttribute("currentUser", userService.findByPrincipal());

        return "moderation/offers/posts";
    }

    @GetMapping("/offers/tags")
    public String tagOffers(
            Model model,
            @PageableDefault(sort = {"id"}, direction = Sort.Direction.DESC) Pageable pageable) {
        Page<TagDTO> page = convertToPageTag(pageable, tagService.findNoActiveTags());

        model.addAttribute("tags", page);
        model.addAttribute("currentUser", userService.findByPrincipal());

        return "moderation/offers/tags";
    }

    private Page<PostDTO> convertToPagePost(Pageable pageable, List<Post> posts) {
        List<PostDTO> postsDTO = posts.stream()
                .sorted((p1, p2) -> p2.getId() - p1.getId())
                .map(this::convertToPostDTO).toList();

        final int start = (int) pageable.getOffset();
        final int end = Math.min((start + pageable.getPageSize()), postsDTO.size());
        return new PageImpl<>(postsDTO.subList(start, end), pageable, postsDTO.size());
    }

    private Page<TagDTO> convertToPageTag(Pageable pageable, List<Tag> tags) {
        List<TagDTO> tagDTO = tags.stream()
                .sorted((p1, p2) -> p2.getId() - p1.getId())
                .map(this::convertToTagDTO).toList();

        final int start = (int) pageable.getOffset();
        final int end = Math.min((start + pageable.getPageSize()), tagDTO.size());
        return new PageImpl<>(tagDTO.subList(start, end), pageable, tagDTO.size());
    }

    private Page<UserDTO> convertToPageUser(Pageable pageable, List<User> users) {
        List<UserDTO> usersDTO = users.stream()
                .sorted((p1, p2) -> p2.getId() - p1.getId())
                .map(this::convertToUserDTO).toList();

        final int start = (int) pageable.getOffset();
        final int end = Math.min((start + pageable.getPageSize()), usersDTO.size());
        return new PageImpl<>(usersDTO.subList(start, end), pageable, usersDTO.size());
    }

    private UserDTO convertToUserDTO(User user) {
        return mapper.map(user, UserDTO.class);
    }

    private TagDTO convertToTagDTO(Tag tag) {
        return mapper.map(tag, TagDTO.class);
    }

    private PostDTO convertToPostDTO(Post post) {
        return mapper.map(post, PostDTO.class);
    }

    private User convertToUser(UserDTO userDTO) {
        return mapper.map(userDTO, User.class);
    }
}
