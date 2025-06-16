package tradeBot.telegram.configs;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;

@Configuration
@Data
@Scope("singleton")
@PropertySource("tg.properties")
public class BotConfig {

    @Value("${bot.token}")
    private String token;

    @Value("${bot.name}")
    private String name;

    @Value("${bot.owner}")
    private long ownerId;
}
