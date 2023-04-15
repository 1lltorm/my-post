package ru.timur.project.Hubr.util.validators;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.multipart.MultipartFile;
import ru.timur.project.Hubr.dto.TagDTO;

@Component
public class TagValidator implements Validator {
    @Override
    public boolean supports(Class<?> clazz) {
        return TagDTO.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        MultipartFile file = (MultipartFile) target;

        if (file == null || file.getOriginalFilename().isEmpty())
            errors.rejectValue("icon", "", "Select a icon for tag");
    }
}
