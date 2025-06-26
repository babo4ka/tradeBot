package tradeBot.invest;


import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.tinkoff.piapi.core.InvestApi;
import tradeBot.invest.configs.InvestConfig;

@Getter
@Scope("singleton")
@Component
public class ApiDistributor {

    InvestApi api;

    public ApiDistributor(InvestConfig config){
        api = config.isSandbox()?InvestApi.createSandbox(config.getSandboxToken()):InvestApi.create(config.getUsualToken());
    }
}
