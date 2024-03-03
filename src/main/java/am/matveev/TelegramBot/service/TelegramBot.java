package am.matveev.TelegramBot.service;

import am.matveev.TelegramBot.config.BotConfig;
import am.matveev.TelegramBot.model.User;
import am.matveev.TelegramBot.repository.UserRepository;
import com.vdurmont.emoji.EmojiParser;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot{


    private final UserRepository userRepository;
    final BotConfig botConfig;

    static final String HELP_TEXT = "This bot is created to demonstrate Spring capabilities.\n\n" +
            "You can execute commands from the main menu on the left or by typing a command:\n\n" +
            "Type /start to see a welcome message\n\n" +
            "Type /mydata to see data stored about yourself\n\n" +
            "Type /help to see this message again";


    @Autowired
    public TelegramBot(UserRepository userRepository, BotConfig botConfig){
        this.userRepository = userRepository;
        this.botConfig = botConfig;

        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "get a welcome message"));
        listOfCommands.add(new BotCommand("/mydata", "get your data stored"));
        listOfCommands.add(new BotCommand("/deletedata", "delete my data"));
        listOfCommands.add(new BotCommand("/help", "info how to use this bot"));
        listOfCommands.add(new BotCommand("/settings", "set your preferences"));
        try{
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        }catch(TelegramApiException e){
            log.error("Error setting bot`s command list: " + e.getMessage());
        }
    }

    @Override
    public String getBotUsername(){
        return botConfig.getName();
    }

    public String getBotToken(){
        return botConfig.getToken();
    }

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update){

        if(update.hasMessage() && update.getMessage().hasText()){
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            switch(messageText){
                case "/start":
                    registerUser(update.getMessage());
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                    break;
                case "/help":
                    sendMessage(chatId, HELP_TEXT);
                    break;
                default:
                    sendMessage(chatId, "Sorry , but this command not recognized");
            }
        }
    }

    private void registerUser(Message message){

        if(userRepository.findById(message.getChatId()).isEmpty()) {
            var chatId = message.getChatId();
            var chat = message.getChat();

            User user = new User();
            user.setChatId(chatId);
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setUsername(chat.getUserName());
            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));

            userRepository.save(user);
            log.info("user saved " + user);
        }
    }

    private void startCommandReceived(long chatId, String name) throws TelegramApiException{

        String answer = EmojiParser.parseToUnicode("Hi , " + name + " , nice to meet you!" + " \uD83D\uDE0A");//:blush:
        log.info("Replied to user " + name);

        sendMessage(chatId, answer);
    }

    private void sendMessage(long chatId, String textToSend) throws TelegramApiException{
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(textToSend);

        ReplyKeyboardMarkup keyboardMarkup = createKeyboardMarkup();

        message.setReplyMarkup(keyboardMarkup);

        try{
            execute(message);
        }catch(TelegramApiException e){
            log.info("Error occurred: " + e.getMessage());
        }
    }

    private ReplyKeyboardMarkup createKeyboardMarkup() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();

        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();

        row.add("weather");
        row.add("get random joke");

        keyboardRows.add(row);

        row = new KeyboardRow();

        row.add("register");
        row.add("check my data");
        row.add("delete my data");

        keyboardRows.add(row);

        keyboardMarkup.setKeyboard(keyboardRows);

        return keyboardMarkup;

    }
}
