package tradeBot.telegram.service.pagesManaging.pageUtils;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

public class ReplyKeyboardBuilder {

    private List<KeyboardRow> keyboard = new ArrayList<>();

    private KeyboardRow row = new KeyboardRow();


    public ReplyKeyboardBuilder addButton(String text){
        row.add(new KeyboardButton(text));
        return this;
    }

    public ReplyKeyboardBuilder nextRow(){
        keyboard.add(row);
        row = new KeyboardRow();
        return this;
    }

    public ReplyKeyboardBuilder reset(){
        keyboard = new ArrayList<>();
        row = new KeyboardRow();
        return this;
    }

    public ReplyKeyboardMarkup build(){
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setResizeKeyboard(true);
        keyboard.setKeyboard(this.keyboard);
        return keyboard;
    }
}
