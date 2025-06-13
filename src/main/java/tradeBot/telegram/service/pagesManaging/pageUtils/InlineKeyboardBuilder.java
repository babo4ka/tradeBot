package tradeBot.telegram.service.pagesManaging.pageUtils;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class InlineKeyboardBuilder {

    private List<List<InlineKeyboardButton>> buttonsMatrix = new ArrayList<>();
    private List<InlineKeyboardButton> buttonsRow = new ArrayList<>();


    public InlineKeyboardBuilder addButton(
            String text, String callback
    ){
        this.buttonsRow.add(InlineKeyboardButton.builder()
                .text(text).callbackData(callback).build());

        return this;
    }

    public InlineKeyboardBuilder nextRow(){
        this.buttonsMatrix.add(this.buttonsRow);
        this.buttonsRow = new ArrayList<>();
        return this;
    }


    public InlineKeyboardBuilder reset(){
        this.buttonsMatrix = new ArrayList<>();
        this.buttonsRow = new ArrayList<>();
        return this;
    }

    public InlineKeyboardMarkup build(){
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(buttonsMatrix);
        return markup;
    }
}
