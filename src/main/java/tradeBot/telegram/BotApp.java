package tradeBot.telegram;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"tradeBot/analyze", "tradeBot/invest", "tradeBot/telegram", "tradeBot/visualize"})
public class BotApp {

    public static void main(String[] args) {
        SpringApplication.run(BotApp.class, args);
    }
}
