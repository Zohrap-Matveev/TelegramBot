package am.matveev.TelegramBot.repository;

import am.matveev.TelegramBot.model.Ads;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdsRepository extends JpaRepository<Ads,Long>{
}
