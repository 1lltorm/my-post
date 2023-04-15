package ru.timur.project.Hubr.services;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.timur.project.Hubr.models.Tag;
import ru.timur.project.Hubr.models.User;
import ru.timur.project.Hubr.repositoris.TagRepository;
import ru.timur.project.Hubr.util.file.FileManipulation;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@AutoConfigureMockMvc
class TagServiceTest {

    @Autowired
    private TagService tagService;

    @MockBean
    private TagRepository tagRepository;

    @MockBean
    private FileManipulation fileManipulation;

    @MockBean
    private NotificationService notificationService;

    @Test
    void subscribe() {
        Tag tagToSubscribe = new Tag();
        tagToSubscribe.setSubscribedUsers(new ArrayList<>());
        User user = new User();
        user.setSubscriptionTags(new ArrayList<>());

        when(tagRepository.findById(0)).thenReturn(Optional.of(tagToSubscribe));

        tagService.subscribe(user, 0);

        assertTrue(tagToSubscribe.getSubscribedUsers().contains(user));

    }

}