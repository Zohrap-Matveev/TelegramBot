package am.matveev.TelegramBot.service;

import am.matveev.TelegramBot.button.SmileyButtonCreator;
import am.matveev.TelegramBot.config.BotConfig;
import am.matveev.TelegramBot.model.Ads;
import am.matveev.TelegramBot.model.User;
import am.matveev.TelegramBot.repository.AdsRepository;
import am.matveev.TelegramBot.repository.UserRepository;
import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot{

    private UserRepository userRepository;
    private AdsRepository adsRepository;
    final BotConfig config;
    private final WeatherService weatherService;

    static final String HELP_TEXT = "This bot is created to demonstrate Spring capabilities.\n\n" +
            "You can execute commands from the main menu on the left or by typing a command:\n\n" +
            "Type /start to see a welcome message\n\n" +
            "Type /help to see this message again\n\n" +
            "Type /Register if you want to register";

    static final String YES_BUTTON = "YES_BUTTON";
    static final String NO_BUTTON = "NO_BUTTON";
    static final String ERROR_TEXT = "Error occurred: ";

    public TelegramBot(UserRepository userRepository, AdsRepository adsRepository, BotConfig config){
        this.userRepository = userRepository;
        this.adsRepository = adsRepository;
        this.config = config;
        this.weatherService = new WeatherService(config);
        List<BotCommand> listofCommands = new ArrayList<>();
        listofCommands.add(new BotCommand("/start", "get a welcome message"));
        listofCommands.add(new BotCommand("/help", "info how to use this bot"));
        listofCommands.add(new BotCommand("/settings", "set your preferences"));
        listofCommands.add(new BotCommand("/smile", "choose a smiley"));

        try{
            this.execute(new SetMyCommands(listofCommands, new BotCommandScopeDefault(), null));
        }catch(TelegramApiException e){
            log.error("Error setting bot's command list: " + e.getMessage());
        }
    }

    @Override
    public String getBotUsername(){
        return config.getName();
    }

    @Override
    public String getBotToken(){
        return config.getToken();
    }

    private void sendSmileMenu(long chatId){
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Choose a smiley:");

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = SmileyButtonCreator.createSmileyButtons();

        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);

        executeMessage(message);
    }
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            handleMessageUpdate(update.getMessage());
        } else {
            handleCallbackQuery(update.getCallbackQuery());
        }
    }

    private void handleMessageUpdate(Message message) {
        String messageText = message.getText();
        long chatId = message.getChatId();

        if (messageText.contains("/send") && config.getOwnerId() == chatId) {
            var textToSend = EmojiParser.parseToUnicode(messageText.substring(messageText.indexOf(" ")));
            var users = userRepository.findAll();
            for (User user : users) {
                prepareAndSendMessage(user.getChatId(), textToSend);
            }
        } else {
            switch (messageText) {
                case "/start":
                    startCommandReceived(chatId, message.getChat().getFirstName());
                    break;
                case "/help":
                    prepareAndSendMessage(chatId, HELP_TEXT);
                    break;
                case "/Register":
                    registerUser(message);
                    register(chatId);
                    break;
                case "/smile":
                    sendSmileMenu(chatId);
                    break;
                case "/Weather in Yerevan":
                    String weatherInfo = weatherService.getWeatherInYerevan();
                    prepareAndSendMessage(chatId, weatherInfo);
                    break;
                case "/Bitcoin info":
                    getBitcoinInfoButtonPressed(chatId);
                    break;
                default:
                    log.info("Unrecognized command: [{}]", messageText);
                    prepareAndSendMessage(chatId, "Sorry, command was not recognized");
            }
        }
    }

    private void handleCallbackQuery(CallbackQuery callbackQuery) {
        String callbackData = callbackQuery.getData();
        long messageId = callbackQuery.getMessage().getMessageId();
        long chatId = callbackQuery.getMessage().getChatId();

        if (callbackData.equals(YES_BUTTON)) {
            String text = "You pressed YES button";
            executeEditMessageText(text, chatId, messageId);
        } else if (callbackData.equals(NO_BUTTON)) {
            String text = "You pressed NO button";
            executeEditMessageText(text, chatId, messageId);
        } else if (callbackData.startsWith("smiley:")) {
            String selectedSmiley = callbackData.substring("smiley:".length());
            prepareAndSendMessage(chatId, selectedSmiley);
        }
    }

    private void register(long chatId){

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Do you really want to register?");

        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();
        var yesButton = new InlineKeyboardButton();

        yesButton.setText("Yes");
        yesButton.setCallbackData(YES_BUTTON);

        var noButton = new InlineKeyboardButton();

        noButton.setText("No");
        noButton.setCallbackData(NO_BUTTON);

        rowInLine.add(yesButton);
        rowInLine.add(noButton);

        rowsInLine.add(rowInLine);

        markupInLine.setKeyboard(rowsInLine);
        message.setReplyMarkup(markupInLine);

        executeMessage(message);
    }

    private void registerUser(Message msg){

        if(userRepository.findById(msg.getChatId()).isEmpty()){

            var chatId = msg.getChatId();
            var chat = msg.getChat();

            User user = new User();

            user.setChatId(chatId);
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setUsername(chat.getUserName());
            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));

            userRepository.save(user);
            log.info("user saved: " + user);
        }
    }

    private void startCommandReceived(long chatId, String name){

        String answer = EmojiParser.parseToUnicode("Hi, " + name + ", nice to meet you!" + " :blush:");
        log.info("Replied to user " + name);

        sendMessage(chatId, answer);
    }

    private void sendMessage(long chatId, String textToSend){
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();

        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();

        row.add("/Weather in Yerevan");
        row.add("/Bitcoin info");
        row.add("/Register");

        keyboardRows.add(row);

        keyboardMarkup.setKeyboard(keyboardRows);

        message.setReplyMarkup(keyboardMarkup);

        executeMessage(message);
    }

    private void executeEditMessageText(String text, long chatId, long messageId){
        EditMessageText message = new EditMessageText();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        message.setMessageId((int)messageId);

        try{
            execute(message);
        }catch(TelegramApiException e){
            log.error(ERROR_TEXT + e.getMessage());
        }
    }

    private void executeMessage(SendMessage message){
        try{
            execute(message);
        }catch(TelegramApiException e){
            log.error(ERROR_TEXT + e.getMessage());
        }
    }

    private void prepareAndSendMessage(long chatId, String textToSend){
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        executeMessage(message);
    }

    @Scheduled(cron = "${cron.scheduler}")
    private void sendAds(){

        var ads = adsRepository.findAll();
        var users = userRepository.findAll();

        for(Ads ad : ads){
            for(User user : users){
                prepareAndSendMessage(user.getChatId(), ad.getAd());
            }
        }
    }
    private String getBitcoinInfo() {
        try {
            String apiUrl = "https://api.coinranking.com/v2/coin/Qwsogvtv82FCd?referenceCurrencyUuid=yhjMzLPhuIDl&timePeriod=24h";
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                String jsonResponse = response.toString();
                JSONObject jsonObject = new JSONObject(jsonResponse);

                if (jsonObject.has("data") && jsonObject.getJSONObject("data").has("coin")) {
                    JSONObject data = jsonObject.getJSONObject("data");
                    JSONObject coin = data.getJSONObject("coin");

                    double price = Double.parseDouble(coin.getString("price"));
                    double change = Double.parseDouble(coin.getString("change"));
                    double volume24h = Double.parseDouble(coin.getString("24hVolume"));
                    double marketCap = Double.parseDouble(coin.getString("marketCap"));

                    return String.format("Price: $%.2f\nChange (24h): %.2f%%\n24h Volume: $%.2f\nMarket Cap: $%.2f", price, change, volume24h, marketCap);
                } else {
                    return "Error: Unexpected response structure from coinranking.com";
                }
            } else {
                return "Error fetching Bitcoin information. Response code: " + responseCode;
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return "Error fetching Bitcoin information";
        }
    }

    private void getBitcoinInfoButtonPressed(long chatId) {
        String bitcoinInfo = getBitcoinInfo();
        prepareAndSendMessage(chatId, bitcoinInfo);
    }

}
