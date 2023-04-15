package ru.timur.project.Hubr.services;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.timur.project.Hubr.dto.NotificationDTO;
import ru.timur.project.Hubr.dto.UserDTO;
import ru.timur.project.Hubr.models.Notification;
import ru.timur.project.Hubr.models.Post;
import ru.timur.project.Hubr.models.User;
import ru.timur.project.Hubr.repositoris.PostRepository;
import ru.timur.project.Hubr.security.UserDetails;
import ru.timur.project.Hubr.util.exceptions.PostNotFoundException;
import ru.timur.project.Hubr.util.file.FileManipulation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class PostService {
    private final PostRepository postRepository;
    private final FileManipulation fileManipulation;
    private final UserService userService;
    private final NotificationService notificationService;

    @Autowired
    public PostService(PostRepository postRepository, FileManipulation fileManipulation, UserService userService, NotificationService notificationService) {
        this.postRepository = postRepository;
        this.fileManipulation = fileManipulation;
        this.userService = userService;
        this.notificationService = notificationService;
    }

    public List<Post> findAll() {
        return postRepository.findPostsByActive(true);
    }

    public List<Post> findNoActivePost() {
        return postRepository.findPostsByActive(false);
    }

    public Post findById(int id) {
        return postRepository.findById(id).orElseThrow(PostNotFoundException::new);
    }

    @Transactional
    public void save(Post post, MultipartFile file) {
        UserDTO user = userService.findByPrincipal();

        post.setOwner(userService.findById(user.getId()));
        enrich(post);
        post.setCover(fileManipulation.saveToFile("postCover", file));
        post.setBody(markdownToHTML(post.getBody()));

        postRepository.save(post);
    }

    //Markdown
    private String markdownToHTML(String markdown) {
        Parser parser = Parser.builder().build();

        Node document = parser.parse(markdown);
        HtmlRenderer renderer = HtmlRenderer.builder().build();

        return renderer.render(document);
    }

    private void enrich(Post post) {
        post.setCreatedAt(LocalDateTime.now());
        post.setActive(false);
    }

    public ArrayList<Post> userFeed(int id) {
        ArrayList<Post> result = new ArrayList<>();
        ArrayList<Post> temp;
        User user = userService.findById(id);

        for (int i = 0; i < user.getSubscriptionTags().size(); i++) {
            temp = postRepository.findPostsByTagListContaining(user.getSubscriptionTags().get(i));
            if (temp != null && !temp.isEmpty())
                result.addAll(temp);

        }

        return new ArrayList<Post>(new HashSet<Post>(result));

    }

    @Transactional
    public void increaseView(Post post) {
        post.setViews(post.getViews() + 1);
        postRepository.save(post);
    }

    @Transactional
    public void allowPublishing(Post post) {
        post.setActive(true);
        postRepository.save(post);
        notificationService.notifySubscribers(post.getOwner(), post.getId());

        String message = "Your post has been approved for publication";
        String type = "Allow publishing";
        String link = "posts/" + post.getId();
        Notification notification = NotificationDTO.sendNotification(post.getOwner(), link, message, type);

        notificationService.saveAndSendNotification(post.getOwner(), notification);
        refreshPrincipal();
    }

    private void refreshPrincipal() {
        UserDetails userDetails = new UserDetails(userService.findById(userService.findByPrincipal().getId()));
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(), userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Transactional
    public void notAllowPublishing(Post post) {
        postRepository.deleteById(post.getId());

        fileManipulation.deleteFile(post.getCover());
        String message = "Your post has been not approved for publication";
        String type = "Not allow post publishing";

        Notification notification = NotificationDTO.sendNotification(post.getOwner(), null, message, type);

        notificationService.saveAndSendNotification(post.getOwner(), notification);
        refreshPrincipal();
    }

}



