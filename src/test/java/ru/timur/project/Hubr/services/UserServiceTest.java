package ru.timur.project.Hubr.services;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.timur.project.Hubr.models.User;
import ru.timur.project.Hubr.repositoris.UserRepository;
import ru.timur.project.Hubr.util.file.FileManipulation;
import ru.timur.project.Hubr.util.mail.MailSend;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserServiceTest {

    @Autowired
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private MailSend mailSend;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private FileManipulation fileManipulation;

    @Test
    void save() {
        String username = "vadim";
        String password = "password1";
        String email = "test@test.com";

        User user = new User(username, email, password);

        userService.save(user);

        assertThat(user.getUsername()).isEqualTo(username);
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getRole()).isEqualTo("ROLE_USER");
        assertThat(user.getNotReadNotifications()).isEqualTo(0);

        assertNotNull(user.getActivationCode());
        assertNotNull(user.getAvatar());
        assertNotNull(user.getCreatedAt());

        assertFalse(user.isActive());

        Mockito.verify(userRepository, Mockito.times(1)).save(user);
        Mockito.verify(mailSend, Mockito.times(1)).activateUserMessage(user);

    }


    @Test
    void activateUser() {
        String activationCode = "someActivationCode";

        User user = new User();
        user.setActivationCode(activationCode);

        Mockito.doReturn(Optional.of(user))
                .when(userRepository)
                .findByActivationCode(activationCode);


        boolean isUserActivated = userService.activateUser(activationCode);

        assertTrue(isUserActivated);
        assertTrue(user.isActive());
        assertNull(user.getActivationCode());
    }

    @Test
    void activateUserFail() {
        String activationCode = "someActivationCode";

        User user = new User();
        user.setActivationCode(activationCode);

        Mockito.doReturn(Optional.of(user))
                .when(userRepository)
                .findByActivationCode(activationCode);


        boolean isUserActivated = userService.activateUser("otherCode");

        assertFalse(isUserActivated);
        assertFalse(user.isActive());
        assertNotNull(user.getActivationCode());
    }

    @Test
    public void uploadProfile() {
        String gender = "testGender";
        String fullName = "testFullName";
        String country = "testCountry";
        String description = "testDescription";

        User user = new User();
        User uploadUser = new User();
        uploadUser.setGender(gender);
        uploadUser.setFullName(fullName);
        uploadUser.setCountry(country);
        uploadUser.setDescription(description);

        Mockito.when(userRepository.findById(0)).thenReturn(Optional.of(user));
        userService.uploadProfile(0, uploadUser);

        assertThat(user.getGender()).isEqualTo(gender);
        assertThat(user.getFullName()).isEqualTo(fullName);
        assertThat(user.getCountry()).isEqualTo(country);
        assertThat(user.getDescription()).isEqualTo(description);
    }



}