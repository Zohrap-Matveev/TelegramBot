package am.matveev.TelegramBot.button;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class SmileyButtonCreator {

        public static List<List<InlineKeyboardButton>> createSmileyButtons() {
            List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

            List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
            InlineKeyboardButton smileyButton1 = new InlineKeyboardButton();
            smileyButton1.setText("😊");
            smileyButton1.setCallbackData("smiley:😊");
            rowInline1.add(smileyButton1);

            InlineKeyboardButton heartButton = new InlineKeyboardButton();
            heartButton.setText("❤️");
            heartButton.setCallbackData("smiley:❤️");
            rowInline1.add(heartButton);

            List<InlineKeyboardButton> rowInline2 = new ArrayList<>();
            InlineKeyboardButton smileyButton2 = new InlineKeyboardButton();
            smileyButton2.setText("😄");
            smileyButton2.setCallbackData("smiley:😄");
            rowInline2.add(smileyButton2);

            InlineKeyboardButton heartButton2 = new InlineKeyboardButton();
            heartButton2.setText("💖");
            heartButton2.setCallbackData("smiley:💖");
            rowInline2.add(heartButton2);

            List<InlineKeyboardButton> rowInline3 = new ArrayList<>();
            InlineKeyboardButton smileyButton3 = new InlineKeyboardButton();
            smileyButton3.setText("😍");
            smileyButton3.setCallbackData("smiley:😍");
            rowInline3.add(smileyButton3);

            InlineKeyboardButton heartButton3 = new InlineKeyboardButton();
            heartButton3.setText("💕");
            heartButton3.setCallbackData("smiley:💕");
            rowInline3.add(heartButton3);

            rowsInline.add(rowInline1);
            rowsInline.add(rowInline2);
            rowsInline.add(rowInline3);

            return rowsInline;
        }
    }
