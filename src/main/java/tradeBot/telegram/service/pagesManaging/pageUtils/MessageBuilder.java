package tradeBot.telegram.service.pagesManaging.pageUtils;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

public class MessageBuilder {

    public SendMessage createTextMessage(
            ReplyKeyboard keyboard,
            long chatId,
            String text
    ){
        SendMessage sm = new SendMessage();
        sm.enableMarkdown(true);
        sm.setChatId(chatId);
        sm.setText(text);
        sm.setReplyMarkup(keyboard);

        return sm;
    }


    public SendPhoto createPhotoMessage(
            ReplyKeyboard keyboard,
            long chatId,
            String text,
            InputFile photo
    ){
        SendPhoto sp = new SendPhoto();
        sp.setChatId(chatId);
        sp.setCaption(text);
        sp.setReplyMarkup(keyboard);
        sp.setPhoto(photo);

        return sp;
    }
}
