package am.matveev.TelegramBot.repository;

import am.matveev.TelegramBot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Long>{
}
