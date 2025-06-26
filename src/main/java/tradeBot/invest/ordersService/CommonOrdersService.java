package tradeBot.invest.ordersService;

import ru.tinkoff.piapi.contract.v1.Quotation;
import ru.tinkoff.piapi.core.InvestApi;
import tradeBot.invest.ApiDistributor;
import tradeBot.invest.configs.InvestConfig;

public abstract class CommonOrdersService {

    protected ApiDistributor apiDistributor;

    InvestConfig config;

    public CommonOrdersService(InvestConfig config, ApiDistributor apiDistributor){
        this.config = config;
        this.apiDistributor = apiDistributor;
    }

    public abstract void postOrderToBuy(String figi, long quantity, Quotation price);

    public abstract void postOrderToSell(String figi, Quotation price);
}
