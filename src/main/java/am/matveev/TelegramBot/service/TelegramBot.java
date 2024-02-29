package am.matveev.TelegramBot.service;

import am.matveev.TelegramBot.config.BotConfig;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


@Component
@RequiredArgsConstructor
public class TelegramBot extends TelegramLongPollingBot{

    private final BotConfig botConfig;

    @Override
    public String getBotUsername(){
        return botConfig.getName();
    }

    public String getBotToken() {
        return botConfig.getToken();
    }

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update){

        if(update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            switch(messageText) {
                case "/start" :
                startCommandReceived(chatId ,update.getMessage().getChat().getFirstName());
                break;

                default:
                    sendMessage(chatId , "Sorry , but this command not recognized");
            }
        }
    }

    private void startCommandReceived (long chatId,String name) throws TelegramApiException{
         String answer = "Hi , " + name + " , nice to meet you!";

         sendMessage(chatId,answer);
    }

    private void sendMessage(long chatId,String textToSend) throws TelegramApiException{
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        execute(message);
    }
}
