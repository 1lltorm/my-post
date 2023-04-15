package ru.timur.project.Hubr.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.timur.project.Hubr.dto.NotificationDTO;
import ru.timur.project.Hubr.models.Notification;
import ru.timur.project.Hubr.models.Post;
import ru.timur.project.Hubr.models.Tag;
import ru.timur.project.Hubr.models.User;
import ru.timur.project.Hubr.repositoris.TagRepository;
import ru.timur.project.Hubr.util.exceptions.TagNotFoundException;
import ru.timur.project.Hubr.util.file.FileManipulation;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class TagService {
    private final TagRepository tagRepository;
    private final FileManipulation fileManipulation;
    private final NotificationService notificationService;

    @Autowired
    public TagService(TagRepository tagRepository, FileManipulation fileManipulation, NotificationService notificationService) {
        this.tagRepository = tagRepository;
        this.fileManipulation = fileManipulation;
        this.notificationService = notificationService;
    }

    public List<Tag> findAll() {
        return tagRepository.findByActive(true);
    }

    public Tag findById(int id) {
        return tagRepository.findById(id).orElseThrow(TagNotFoundException::new);
    }

    public List<Tag> findNoActiveTags() {
        return tagRepository.findByActive(false);
    }

    @Transactional
    public void save(Tag tag, MultipartFile file, User owner) {
        tag.setIcon(fileManipulation.saveToFile("tagIcon", file));
        tag.setTagOwner(owner);
        enrich(tag);
        tagRepository.save(tag);
    }

    private void enrich(Tag tag) {
        tag.setCreatedAt(new Date());
        tag.setActive(false);
    }

    @Transactional
    public void subscribe(User user, int tagId) {
        Tag tag = findById(tagId);

        tag.getSubscribedUsers().add(user);
        tagRepository.save(tag);
    }

    @Transactional
    public void unsubscribe(User user, int tagId) {
        Tag tag = findById(tagId);

        tag.getSubscribedUsers().remove(user);
        tagRepository.save(tag);
    }
    public List<Tag> findByNameContaining(String name) {
        return tagRepository.findByNameContainingIgnoreCase(name);
    }

    @Transactional
    public void setActiveType(int id) {
        Tag tag = findById(id);
        tag.setActive(true);
        tagRepository.save(tag);

        String message = "Your tag has been approved for publication";
        String type =  "Allow Tag publishing";
        String link = "/tags/" + id;

        Notification notification = NotificationDTO.sendNotification(tag.getTagOwner(), link, message, type);
        notificationService.saveAndSendNotification(tag.getTagOwner(), notification);

    }

    public List<Post> getActivePostsByTag(Tag tag) {
        List<Post> posts = tag.getPostList();
        List<Post> result = new ArrayList<>();

        for (Post p: posts) {
            if (p.isActive())
                result.add(p);
        }
        return result;
    }

    @Transactional
    public void delete(Tag tag) {
        User owner = tag.getTagOwner();
        owner.getTagsList().remove(tag);
        tagRepository.delete(tag);
        fileManipulation.deleteFile(tag.getIcon());

        String message = "Your tag has been not approved for publication";
        String type =  "Not allow Tag publishing";

        Notification notification = NotificationDTO.sendNotification(owner, null, message, type);
        notificationService.saveAndSendNotification(owner, notification);
    }
}
