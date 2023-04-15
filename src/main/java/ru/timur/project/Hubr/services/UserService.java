package ru.timur.project.Hubr.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.util.StringUtils;
import ru.timur.project.Hubr.dto.UserDTO;
import ru.timur.project.Hubr.models.Tag;
import ru.timur.project.Hubr.models.User;
import ru.timur.project.Hubr.repositoris.UserRepository;
import ru.timur.project.Hubr.security.UserDetails;
import ru.timur.project.Hubr.util.exceptions.UserNotFoundException;
import ru.timur.project.Hubr.util.file.FileManipulation;
import ru.timur.project.Hubr.util.mail.MailSend;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;
    private final MailSend mail;
    private final PasswordEncoder passwordEncoder;
    private final FileManipulation fileManipulation;

    @Autowired
    public UserService(UserRepository userRepository, MailSend mail, PasswordEncoder passwordEncoder, FileManipulation fileManipulation) {
        this.userRepository = userRepository;
        this.mail = mail;
        this.passwordEncoder = passwordEncoder;
        this.fileManipulation = fileManipulation;
    }

    @Transactional
    public void save(User user) {
        userEnrich(user);
        userRepository.save(user);
        mail.activateUserMessage(user);
    }

    @Transactional
    public boolean activateUser(String code) {
        Optional<User> findByCode = userRepository.findByActivationCode(code);

        if (findByCode.isEmpty())
            return false;

        User user = findByCode.get();
        user.setActivationCode(null);
        user.setActive(true);
        userRepository.save(user);

        return true;
    }

    public UserDTO findByPrincipal() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            User user = ((UserDetails)principal).getUser();
            return new UserDTO(user.getId(), user.getRole(), user.isActive(), user.getAvatar(), user.getNotReadNotifications());

        } else {
            return null;
        }
    }

    public List<User> findAll() {
        return userRepository.findByActive(true);
    }

    public User findById(int id) {
        return userRepository.findById(id).orElseThrow(UserNotFoundException::new);
    }

    private void userEnrich(User user) {
        user.setNotReadNotifications(0);
        user.setActivationCode(UUID.randomUUID().toString());
        user.setCreatedAt(new Date());
        user.setActive(false);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("ROLE_USER");
        user.setAvatar("userImg/defaultAvatar.png");
    }

    @Transactional
    public void uploadProfile(int id, User uploadUser) {
        User user = findById(id);

        if (!StringUtils.isEmpty(uploadUser.getFullName()))
            user.setFullName(uploadUser.getFullName());

        if (!StringUtils.isEmpty(uploadUser.getDescription()))
            user.setDescription(uploadUser.getDescription());

        if (!StringUtils.isEmpty(uploadUser.getGender()))
            user.setGender(uploadUser.getGender());

        if (!StringUtils.isEmpty(uploadUser.getCountry()))
            user.setCountry(uploadUser.getCountry());

        userRepository.save(user);
    }

    @Transactional
    public void uploadEmail(int id, User uploadUser) {
        User user = findById(id);

        user.setSecondEmail(uploadUser.getEmail());
        user.setActivationCode(UUID.randomUUID().toString());
        mail.activateNewEmailMessage(user);

        userRepository.save(user);
    }

    @Transactional
    public String activateEmail(String code) {
        Optional<User> userByCode = userRepository.findByActivationCode(code);

        if (userByCode.isEmpty())
            return "Code is not found";

        User user = userByCode.get();
        user.setEmail(user.getSecondEmail());
        user.setSecondEmail(null);
        user.setActivationCode(null);

        userRepository.save(user);
        return "New email have been saved";
    }

    @Transactional
    public void uploadPassword(int id, User uploadUser) {
        User user = findById(id);
        user.setPassword(passwordEncoder.encode(uploadUser.getPassword()));

        refreshPrincipal();

        userRepository.save(user);
    }

    @Transactional
    public void uploadAvatar(int id, MultipartFile file) {
        User user = findById(id);
        user.setAvatar(fileManipulation.uploadFile("userImg", user.getAvatar(), file));
        refreshPrincipal();

        userRepository.save(user);
    }

    private void refreshPrincipal() {
        UserDetails userDetails = new UserDetails(findById(findByPrincipal().getId()));
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(), userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Transactional
    public void subscribe(User currentUser, User user) {
        user.getSubscribers().add(currentUser);

        userRepository.save(user);
    }

    @Transactional
    public void unsubscribe(User currentUser, User user) {
        user.getSubscribers().remove(currentUser);

        userRepository.save(user);
    }

    public List<User> findByUsernameContaining(String name) {
        return userRepository.findByUsernameContainingIgnoreCase(name);
    }

    @Transactional
    public void banUser(User user) {
        user.setActive(false);
        userRepository.save(user);

    }

    public List<User> findByTag(Tag tag) {
        return userRepository.findUsersBySubscriptionTagsContaining(tag);
    }

    @Transactional
    public void unbanUser(User user) {
        user.setActive(true);
        userRepository.save(user);

    }

    @Transactional
    public void updateUserRole(int id, User userUpload) {
        User user = findById(id);
        user.setRole(userUpload.getRole());
        userRepository.save(user);
    }

}