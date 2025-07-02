package tradeBot.invest.ordersService;

import org.springframework.beans.factory.annotation.Autowired;
import ru.tinkoff.piapi.contract.v1.Operation;
import ru.tinkoff.piapi.contract.v1.OperationState;
import ru.tinkoff.piapi.contract.v1.OperationType;
import ru.tinkoff.piapi.core.SandboxService;
import tradeBot.invest.ApiDistributor;
import tradeBot.invest.configs.InvestConfig;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public abstract class CommonStatisticsService {

    protected ApiDistributor apiDistributor;

    protected InvestConfig config;

    public CommonStatisticsService(InvestConfig config, ApiDistributor apiDistributor){
        this.config = config;
        this.apiDistributor = apiDistributor;
    }


    public abstract double countShareProfitByTicker(String ticker);

    public abstract double countShareProfitByFigi(String figi);
}
