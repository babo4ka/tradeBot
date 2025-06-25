package tradeBot.invest.ordersService;

import ru.tinkoff.piapi.contract.v1.Quotation;
import ru.tinkoff.piapi.core.InvestApi;
import tradeBot.invest.configs.InvestConfig;

public abstract class CommonOrdersService {

    protected InvestApi api;

    InvestConfig config;

    public CommonOrdersService(InvestConfig config){
        this.config = config;
    }

    public abstract void postOrderToBuy(String figi, long quantity, Quotation price);

    public abstract void postOrderToSell(String figi, Quotation price);
}
