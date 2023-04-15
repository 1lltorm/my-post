package ru.timur.project.Hubr.util.validators;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import ru.timur.project.Hubr.dto.UserDTO;
import ru.timur.project.Hubr.models.User;
import ru.timur.project.Hubr.repositoris.UserRepository;

import java.util.Optional;

@Component
public class UserValidator implements Validator {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserValidator(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return UserDTO.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        UserDTO userDTO = (UserDTO) target;

        if (!userDTO.getPassword().equals(userDTO.getPasswordConf()) && !userDTO.getPasswordConf().equals("")) {
            errors.rejectValue("passwordConf", "", "Password confirmation should be correct");
        }
        Optional<User> foundEmail = userRepository.findByEmail(userDTO.getEmail());
        Optional<User> foundUsername = userRepository.findByUsername(userDTO.getUsername());

        if (foundEmail.isPresent()) {
            errors.rejectValue("email", "", "This email is already taken");
        }

        if (foundUsername.isPresent()) {
            errors.rejectValue("username", "", "This name is already taken");
        }
    }


    public void validatePassword(Object target, Errors errors) {
        UserDTO userDTO = (UserDTO) target;
        User user = userRepository.findById(userDTO.getId()).get(); // todo
        boolean comparePasswords = !passwordEncoder.matches(userDTO.getCurrentPassword(), user.getPassword());

        if (!userDTO.getCurrentPassword().equals("") && comparePasswords)
            errors.rejectValue("currentPassword", "", "Current password should be correct");

        if (!userDTO.getPasswordConf().equals("") && !userDTO.getPassword().equals(userDTO.getPasswordConf()))
            errors.rejectValue("passwordConf", "", "Password confirmation should be correct");

    }

    public void validateEmail(Object target, Errors errors) {
        UserDTO userDTO = (UserDTO) target;
        Optional<User> found = userRepository.findByEmail(userDTO.getEmail());

        if (found.isPresent())
            errors.rejectValue("email", "", "This email is already taken");

        if (!userDTO.getEmail().equals(userDTO.getEmailConf()))
            errors.rejectValue("emailConf", "", "Email confirmation should be correct");

    }

}

