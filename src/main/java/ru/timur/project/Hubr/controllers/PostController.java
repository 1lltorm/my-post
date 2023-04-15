package ru.timur.project.Hubr.controllers;

import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.timur.project.Hubr.dto.CommentDTO;
import ru.timur.project.Hubr.dto.PostDTO;
import ru.timur.project.Hubr.models.Comment;
import ru.timur.project.Hubr.models.Post;
import ru.timur.project.Hubr.services.PostService;
import ru.timur.project.Hubr.services.TagService;
import ru.timur.project.Hubr.services.UserService;
import ru.timur.project.Hubr.util.exceptions.PostNotFoundException;
import ru.timur.project.Hubr.util.exceptions.response.PostErrorResponse;

import java.util.List;

@Controller
@PreAuthorize("isAuthenticated()")
@RequestMapping("/posts")
public class PostController {
    private final ModelMapper mapper;
    private final TagService tagService;
    private final PostService postService;
    private final UserService userService;

    @Autowired
    public PostController(ModelMapper mapper, TagService tagService, PostService postService, UserService userService) {
        this.mapper = mapper;
        this.tagService = tagService;
        this.postService = postService;
        this.userService = userService;
    }

    @PreAuthorize("permitAll()")
    @GetMapping("/{id}")
    public String post(
            @PathVariable("id") int id,
            Model model,
            @ModelAttribute("comment") CommentDTO commentDTO) {
        Post post = postService.findById(id);

        model.addAttribute("currentUser", userService.findByPrincipal());
        model.addAttribute("post", convertToPostDTO(post));
        model.addAttribute("comments", post.getComments().stream().map(this::convertToCommentDTO).toList());
        postService.increaseView(post);

        return "post/post";
    }

    @GetMapping("/publication")
    public String publication(
            @ModelAttribute("post") PostDTO postDTO,
            Model model) {
        model.addAttribute("currentUser", userService.findByPrincipal());
        model.addAttribute("tags", tagService.findAll());
        return "post/publication";
    }

    @PostMapping()
    public String save(
            @ModelAttribute("post") @Valid PostDTO postDTO,
            BindingResult bindingResult,
            Model model,
            MultipartFile file){
        if (bindingResult.hasErrors()) {
            model.addAttribute("currentUser", userService.findByPrincipal());
            model.addAttribute("tags", tagService.findAll());
            return "post/publication";
        }
        postService.save(convertToPost(postDTO), file);
        return "redirect:/posts/all";
    }

    @PreAuthorize("permitAll()")
    @GetMapping("/all")
    public String postsPage(
            Model model,
            @PageableDefault(sort = {"id"}, direction = Sort.Direction.DESC) Pageable pageable) {

        Page<PostDTO> page = convertToPageDTO(pageable, postService.findAll());

        model.addAttribute("currentUser", userService.findByPrincipal());
        model.addAttribute("posts", page);
        model.addAttribute("url", "/posts/all");
        return "post/posts";
    }

    @GetMapping("/feed")
    public String feed(
            Model model,
            @PageableDefault(sort = {"id"}, direction = Sort.Direction.DESC) Pageable pageable) {
        List<Post> posts = postService.userFeed(userService.findByPrincipal().getId());

        if (posts.isEmpty())
            return "redirect:/tags/feed-settings";

        Page<PostDTO> page = convertToPageDTO(pageable, posts);

        model.addAttribute("url", "/posts/feed");
        model.addAttribute("currentUser", userService.findByPrincipal());
        model.addAttribute("posts", page);

        return "post/feed";
    }

    private Page<PostDTO> convertToPageDTO(Pageable pageable, List<Post> posts) {
        List<PostDTO> postsDTO = posts.stream()
                .sorted((p1,p2) -> p2.getId() - p1.getId())
                .map(this::convertToPostDTO).toList();

        final int start = (int)pageable.getOffset();
        final int end = Math.min((start + pageable.getPageSize()), postsDTO.size());
        return new PageImpl<>(postsDTO.subList(start, end), pageable, postsDTO.size());
    }


    @ExceptionHandler
    private ResponseEntity<PostErrorResponse> handlerException(PostNotFoundException e) {
        PostErrorResponse postErrorResponse = new
                PostErrorResponse("Post with this id wasn't found", System.currentTimeMillis());

        return new ResponseEntity<>(postErrorResponse, HttpStatus.NOT_FOUND); // 404 статус
    }

    private Post convertToPost(PostDTO postDTO) {
        return mapper.map(postDTO, Post.class);
    }

    private PostDTO convertToPostDTO(Post post) {
        return mapper.map(post, PostDTO.class);
    }

    private CommentDTO convertToCommentDTO(Comment comment) {
        return mapper.map(comment, CommentDTO.class);
    }

}
