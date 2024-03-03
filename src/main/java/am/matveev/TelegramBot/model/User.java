package am.matveev.TelegramBot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;


import java.sql.Timestamp;

@Entity(name = "userDataTable")
@Data
public class User{

    @Id
    private long chatId;
    private String firstName;
    private String lastName;
    private String username;
    private Timestamp registeredAt;


}
