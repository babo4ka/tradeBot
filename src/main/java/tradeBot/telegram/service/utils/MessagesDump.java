package tradeBot.telegram.service.utils;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import tradeBot.telegram.configs.BotConfig;

import java.util.ArrayList;
import java.util.List;

@Getter
@Component
public class MessagesDump {

    @Autowired
    BotConfig config;

    private final List<DeleteMessage> messagesToDelete = new ArrayList<>();

    public void clearDump(){
        messagesToDelete.clear();
    }

    public void addMessage(Message message){
        messagesToDelete.add(DeleteMessage.builder().messageId(message.getMessageId()).chatId(config.getOwnerId()).build());
    }
}
