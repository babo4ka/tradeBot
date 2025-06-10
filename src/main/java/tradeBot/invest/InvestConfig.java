package tradeBot.invest;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Data
@PropertySource("invest.properties")
public class InvestConfig {

    @Value("${usual_token}")
    private String usualToken;
    @Value("${sandbox_token}")
    private String sandboxToken;

}
