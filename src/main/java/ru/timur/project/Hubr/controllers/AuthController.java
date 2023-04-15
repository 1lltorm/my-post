package ru.timur.project.Hubr.controllers;

import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import ru.timur.project.Hubr.dto.CaptchaResponseDTO;
import ru.timur.project.Hubr.dto.UserDTO;
import ru.timur.project.Hubr.enums.Country;
import ru.timur.project.Hubr.enums.Gender;
import ru.timur.project.Hubr.models.User;
import ru.timur.project.Hubr.services.UserService;
import ru.timur.project.Hubr.util.validators.UserValidator;

import java.util.Collections;

@Controller
@RequestMapping("/auth")
@PreAuthorize("isAuthenticated()")
public class AuthController {
    @Value("${google.recaptcha.key.secret}")
    private String secret;
    private final static String CAPTCHA_URL = "https://www.google.com/recaptcha/api/siteverify?secret=%s&response=%s";
    private final UserService userService;
    private final UserValidator userValidator;
    private final RestTemplate restTemplate;
    private final ModelMapper mapper;

    @Autowired
    public AuthController(UserService userService, UserValidator userValidator, RestTemplate restTemplate, ModelMapper mapper) {
        this.userService = userService;
        this.userValidator = userValidator;
        this.restTemplate = restTemplate;
        this.mapper = mapper;
    }

    @PreAuthorize("permitAll()")
    @GetMapping("/reg")
    public String registrationPage(@ModelAttribute("userDTO") UserDTO userDTO, Model model) {
        model.addAttribute("currentUser", userService.findByPrincipal());
        return "auth/reg";
    }

    @PreAuthorize("permitAll()")
    @PostMapping("/reg")
    public String doRegistration(
            @RequestParam("g-recaptcha-response") String captchaResponse,
            @ModelAttribute("userDTO") @Valid UserDTO userDTO,
            BindingResult bindingResult,
            Model model) {
        userValidator.validate(userDTO, bindingResult);

        String url = String.format(CAPTCHA_URL, secret, captchaResponse);
        CaptchaResponseDTO response = restTemplate.postForObject(url, Collections.emptyList(), CaptchaResponseDTO.class);

        if (!response.isSuccess()) {
            model.addAttribute("captchaError", "Please fill captcha");
        }

        if (bindingResult.hasFieldErrors("username")|| bindingResult.hasFieldErrors("email")||
                bindingResult.hasFieldErrors("password")|| bindingResult.hasFieldErrors("passwordConf") ||!response.isSuccess()) {
            return "auth/reg";
        } else {

            userService.save(convertToUser(userDTO));
            model.addAttribute("newMailMessage", "Visit your mail");
            return "auth/login";
        }
    }

    @PreAuthorize("permitAll()")
    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("currentUser", userService.findByPrincipal());
        return "auth/login";
    }

    @PreAuthorize("permitAll()")
    @GetMapping("/activate/{code}")
    public String activate(Model model, @PathVariable("code") String code) {
        boolean isActivated = userService.activateUser(code);
        model.addAttribute("currentUser", userService.findByPrincipal());
        if (isActivated)
            model.addAttribute("message", "User successfully activated");

        else
            model.addAttribute("message", "User code is not found");

        return "auth/login";
    }


    @GetMapping("/settings/profile")
    public String settingsProfile(
            Model model) {
        UserDTO currentUser = userService.findByPrincipal();
        UserDTO user = convertToUserDTO(userService.findById(userService.findByPrincipal().getId()));

        if (currentUser.getId() != user.getId()) return "util/err";

        model.addAttribute("user", user);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("genders", Gender.values());
        model.addAttribute("countries", Country.values());

        return "auth/profile/profileSettings";
    }

    @PatchMapping("/settings/profile/{id}")
    public String doSettingsProfile(
            @PathVariable("id") int id,
            @ModelAttribute("user") @Valid UserDTO userDTO,
            BindingResult bindingResult,
            Model model) {
        model.addAttribute("currentUser", userService.findByPrincipal());

        if (bindingResult.hasFieldErrors("fullName") || bindingResult.hasFieldErrors("description")) {
            model.addAttribute("genders", Gender.values());
            model.addAttribute("countries", Country.values());
            return "auth/profile/profileSettings";
        }
        model.addAttribute("message", "Changes have been saved");

        model.addAttribute("genders", Gender.values());
        model.addAttribute("countries", Country.values());
        userService.uploadProfile(id, convertToUser(userDTO));
        return "auth/profile/profileSettings";
    }

    @GetMapping("/settings/email")
    public String settingsEmail(
            Model model) {
        UserDTO currentUser = userService.findByPrincipal();
        UserDTO user = convertToUserDTO(userService.findById(userService.findByPrincipal().getId()));

        if (currentUser.getId() != user.getId()) return "util/err";

        model.addAttribute("user", user);
        model.addAttribute("currentUser", currentUser);

        return "auth/profile/email";
    }

    @PatchMapping("/settings/email/{id}")
    public String doSettingsEmail(
            @PathVariable("id") int id,
            @ModelAttribute("user") @Valid UserDTO userDTO,
            BindingResult bindingResult,
            Model model) {
        userValidator.validateEmail(userDTO,bindingResult);
        model.addAttribute("currentUser", userService.findByPrincipal());

        if (bindingResult.hasFieldErrors("emailConf") || bindingResult.hasFieldErrors("email")) {
            return "auth/profile/email";
        }
        model.addAttribute("message", "To confirm your new email address, please visit the mail");

        userService.uploadEmail(id, convertToUser(userDTO));
        return "auth/profile/email";
    }

    @GetMapping("/settings/password")
    public String settingsPassword(
            Model model) {
        UserDTO currentUser = userService.findByPrincipal();
        UserDTO user = convertToUserDTO(userService.findById(userService.findByPrincipal().getId()));

        if (currentUser.getId() != user.getId()) return "util/err";

        model.addAttribute("user", user);
        model.addAttribute("currentUser", currentUser);
        return "auth/profile/password";
    }

    @PatchMapping("/settings/password/{id}")
    public String doSettingsPassword(
            @PathVariable("id") int id,
            @ModelAttribute("user") @Valid UserDTO userDTO,
            BindingResult bindingResult,
            Model model) {
        userValidator.validatePassword(userDTO, bindingResult);

        model.addAttribute("currentUser", userService.findByPrincipal());
        if (bindingResult.hasFieldErrors("password") || bindingResult.hasFieldErrors("passwordConf") ||  bindingResult.hasFieldErrors("currentPassword"))
            return "auth/profile/password";
        model.addAttribute("message", "New password have been saved");

        userService.uploadPassword(id, convertToUser(userDTO));
        return "auth/profile/password";
    }

    @GetMapping("/activate/new-email/{code}/{id}")
    public String activateNewEmail(
            @PathVariable("code") String code,
            @PathVariable("id") int id,
            Model model) {
        model.addAttribute("user", convertToUserDTO(userService.findById(id)));
        model.addAttribute("newEmailMessage", userService.activateEmail(code));
        model.addAttribute("currentUser", userService.findByPrincipal());

        return "auth/profile/email";
    }

    @PatchMapping("/settings/avatar/{id}")
    public String doSettingsAvatar(
            @PathVariable("id") int id,
            MultipartFile file) {
        userService.uploadAvatar(id, file);
        return "redirect:/auth/settings/profile";
    }

    private User convertToUser(UserDTO userDTO) {
        return mapper.map(userDTO, User.class);
    }

    private UserDTO convertToUserDTO(User user) {
        return mapper.map(user, UserDTO.class);
    }

}



