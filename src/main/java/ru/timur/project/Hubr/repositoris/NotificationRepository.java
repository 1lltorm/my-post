package ru.timur.project.Hubr.repositoris;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.timur.project.Hubr.models.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {

}
