package ru.timur.project.Hubr.services;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.timur.project.Hubr.models.Comment;
import ru.timur.project.Hubr.models.Post;
import ru.timur.project.Hubr.models.User;
import ru.timur.project.Hubr.repositoris.CommentRepository;
import ru.timur.project.Hubr.repositoris.UserRepository;
import ru.timur.project.Hubr.util.exceptions.CommentNotFoundException;
import ru.timur.project.Hubr.util.file.FileManipulation;
import ru.timur.project.Hubr.util.mail.MailSend;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
class CommentServiceTest {

    @Autowired
    private CommentService commentService;

    @MockBean
    private CommentRepository commentRepository;

    @Test
    void save() {
        Comment comment = new Comment();
        comment.setBody("someBody");
        comment.setOwner(new User());
        comment.setPost(new Post());

        commentService.save(comment);

        assertNotNull(comment.getCreatedAt());

        Mockito.verify(commentRepository, Mockito.times(1)).save(comment);
    }

    @Test
    void deleteById() {
    }
}