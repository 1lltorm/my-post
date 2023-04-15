package ru.timur.project.Hubr.repositoris;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.timur.project.Hubr.models.Tag;
import ru.timur.project.Hubr.models.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByUsername(String name);


    Optional<User> findByActivationCode(String code);


    Optional<User> findByEmail(String email);

    List<User> findByUsernameContainingIgnoreCase(String username);


    List<User> findUsersBySubscriptionTagsContaining(Tag tag);

    List<User> findByActive(boolean b);
}
