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
import ru.timur.project.Hubr.dto.PostDTO;
import ru.timur.project.Hubr.dto.TagDTO;
import ru.timur.project.Hubr.models.Post;
import ru.timur.project.Hubr.models.Tag;
import ru.timur.project.Hubr.services.TagService;
import ru.timur.project.Hubr.services.UserService;
import ru.timur.project.Hubr.util.exceptions.TagNotFoundException;
import ru.timur.project.Hubr.util.exceptions.response.TagErrorResponse;
import ru.timur.project.Hubr.util.validators.TagValidator;

import java.util.List;

@Controller
@PreAuthorize("isAuthenticated()")
@RequestMapping("/tags")
public class TagController {
    private final ModelMapper mapper;
    private final TagService tagService;
    private final TagValidator tagValidator;
    private final UserService userService;

    @Autowired
    public TagController(ModelMapper mapper, TagService tagService, TagValidator tagValidator, UserService userService) {
        this.mapper = mapper;
        this.tagService = tagService;
        this.tagValidator = tagValidator;
        this.userService = userService;
    }

    @PreAuthorize("permitAll()")
    @GetMapping()
    public String tags(
            Model model,
            String filter,
            @RequestParam(required = false) String sort,
            @PageableDefault(sort = {"id"}, direction = Sort.Direction.DESC) Pageable pageable) {
        Page<TagDTO> page = convertToPageDTO(pageable, tagService.findAll().stream().map(this::convertToTagDTO).toList(),true);

        if (filter != null) {
            page = convertToPageDTO(pageable, tagService.findByNameContaining(filter).stream().map(this::convertToTagDTO).toList(), true);
        }

        if (sort != null && sort.equals("posts")) {
            page = convertToPageDTO(pageable,
                    page.stream().sorted((p1, p2) -> p2.getPostList().size() - p1.getPostList().size()).toList(),false);
        }

        if (sort != null && sort.equals("subscribers")) {
            page = convertToPageDTO(pageable,
                    page.stream().sorted((p1, p2) -> p2.getSubscribedUsers().size() - p1.getSubscribedUsers().size()).toList(),false);
        }

        model.addAttribute("currentUser", userService.findByPrincipal());
        model.addAttribute("tags", page);
        model.addAttribute("url", "/tags");

        return "tag/tags";
    }

    @PreAuthorize("permitAll()")
    @GetMapping("/{id}")
    public String tagPage(@PathVariable("id") int id, Model model) {
        Tag tag = tagService.findById(id);

        if (userService.findByPrincipal() != null)
            model.addAttribute("currentUser", userService.findById(userService.findByPrincipal().getId()));

        model.addAttribute("tag", convertToTagDTO(tag));
        model.addAttribute("posts", tagService.getActivePostsByTag(tag).stream()
                .map(this::convertToPostDTO).toList());

        return "tag/tag";
    }

    @GetMapping("/new")
    public String newTagPage(@ModelAttribute("tag") TagDTO tagDTO, Model model) {
        model.addAttribute("currentUser", userService.findByPrincipal());
        return "tag/addTag";
    }

    @PostMapping()
    public String save(
            @ModelAttribute("tag") @Valid TagDTO tagDTO,
            BindingResult bindingResult,
            MultipartFile file,
            Model model) {
        tagValidator.validate(file, bindingResult);
        if (bindingResult.hasErrors()) {
            model.addAttribute("currentUser", userService.findByPrincipal());
            return "tag/addTag";
        }
        tagService.save(convertToTag(tagDTO), file, userService.findById(userService.findByPrincipal().getId()));
        return "redirect:/tags";
    }

    @PreAuthorize("permitAll()")
    @GetMapping("/{id}/subscribers")
    public String tagSubscribers(@PathVariable("id") int id, Model model) {
        model.addAttribute("tag", tagService.findById(id));
        model.addAttribute("users", userService.findByTag(tagService.findById(id)));
        model.addAttribute("currentUser", userService.findByPrincipal());

        return "tag/subscribers";
    }

    @GetMapping("/feed-settings")
    public String feedSettings(
            Model model,
            @PageableDefault(sort = {"id"}, direction = Sort.Direction.DESC) Pageable pageable) {
        model.addAttribute("currentUser", userService.findById(userService.findByPrincipal().getId()));

        Page<TagDTO> page = convertToPageDTO(pageable, tagService.findAll().stream().map(this::convertToTagDTO).toList(), false);

        model.addAttribute("url", "/tags/feed-settings");
        model.addAttribute("tags", page);

        return "post/feedSettings";
    }

    @GetMapping("/subscribe/{id}/{url}")
    public String subscribe(@PathVariable("id") int tagId, @PathVariable("url") String url) {
        tagService.subscribe(userService.findById(userService.findByPrincipal().getId()), tagId);

        return (url.equals("tag")) ? "redirect:/tags/" + tagId : "redirect:/tags/feed-settings";
    }

    @GetMapping("/unsubscribe/{id}/{url}")
    public String unsubscribe(@PathVariable("id") int tagId, @PathVariable("url") String url) {
        tagService.unsubscribe(userService.findById(userService.findByPrincipal().getId()), tagId);

        return (url.equals("tag")) ? "redirect:/tags/" + tagId : "redirect:/tags/feed-settings";
    }

    @ExceptionHandler
    private ResponseEntity<TagErrorResponse> handlerException(TagNotFoundException e) {
        TagErrorResponse tagErrorResponse = new
                TagErrorResponse("Tag with this id wasn't found", System.currentTimeMillis());

        return new ResponseEntity<>(tagErrorResponse, HttpStatus.NOT_FOUND); // 404 статус
    }

    private Tag convertToTag(TagDTO tagDTO) {
        return mapper.map(tagDTO, Tag.class);
    }

    private TagDTO convertToTagDTO(Tag tag) {
        return mapper.map(tag, TagDTO.class);
    }

    private PostDTO convertToPostDTO(Post post) {
        return mapper.map(post, PostDTO.class);
    }

    private Page<TagDTO> convertToPageDTO(Pageable pageable, List<TagDTO> tags, boolean sort) {
        List<TagDTO> tagDTO = tags;

        if (sort) {
            tagDTO = tags.stream()
                    .sorted((p1,p2) -> p2.getId() - p1.getId()).toList();
        }

        final int start = (int)pageable.getOffset();
        final int end = Math.min((start + pageable.getPageSize()), tagDTO.size());
        return new PageImpl<>(tagDTO.subList(start, end), pageable, tagDTO.size());
    }

}
