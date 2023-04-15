package ru.timur.project.Hubr.services;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.timur.project.Hubr.models.User;
import ru.timur.project.Hubr.repositoris.UserRepository;
import ru.timur.project.Hubr.security.UserDetails;

import java.util.Optional;

@Service
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

    private final UserRepository repository;

    public UserDetailsService(UserRepository repository) {
        this.repository = repository;
    }


    @Override
    public org.springframework.security.core.userdetails.UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> found = repository.findByUsername(username);

        if (found.isEmpty()) {
            throw new UsernameNotFoundException("User not found!");
        }
        return new UserDetails(found.get());
    }
}
