package ru.timur.project.Hubr.controllers;

import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.timur.project.Hubr.dto.CommentDTO;
import ru.timur.project.Hubr.models.Comment;
import ru.timur.project.Hubr.services.CommentService;
import ru.timur.project.Hubr.services.PostService;
import ru.timur.project.Hubr.services.UserService;

@Controller
@RequestMapping("/comments")
public class CommentController {
    private final CommentService commentService;
    private final ModelMapper mapper;
    private final PostService postService;
    private final UserService userService;

    @Autowired
    public CommentController(CommentService commentService, ModelMapper mapper, PostService postService, UserService userService) {
        this.commentService = commentService;
        this.mapper = mapper;
        this.postService = postService;
        this.userService = userService;
    }

    @PostMapping("/{postId}")
    public String save(
            @Valid CommentDTO commentDTO,
            BindingResult bindingResult,
            @PathVariable("postId") int id) {

        if (bindingResult.hasErrors())
            return "post/post";

        commentDTO.setOwner(userService.findById(userService.findByPrincipal().getId()));
        commentDTO.setPost(postService.findById(id));
        commentService.save(convertToComment(commentDTO));

        return "redirect:/posts/" + id + "#comments";
    }

    @DeleteMapping("/{id}")
    public String deleteComment(@PathVariable("id") int id) {
        int postId = commentService.findById(id).getPost().getId();
        commentService.deleteById(id);
        return "redirect:/posts/" + postId + "#comments";
    }

    private Comment convertToComment(CommentDTO commentDTO) {
        return mapper.map(commentDTO, Comment.class);
    }


}
