package tradeBot.invest.ordersService.sandbox;

import org.springframework.stereotype.Component;
import ru.tinkoff.piapi.contract.v1.OrderDirection;
import ru.tinkoff.piapi.contract.v1.OrderType;
import ru.tinkoff.piapi.contract.v1.Quotation;
import ru.tinkoff.piapi.core.InvestApi;
import ru.tinkoff.piapi.core.SandboxService;
import tradeBot.invest.configs.InvestConfig;

import java.util.UUID;

@Component
public class OrdersInSandboxService {

    InvestApi api;

    InvestConfig config;


    public OrdersInSandboxService(InvestConfig config){
        this.config = config;

        api = InvestApi.createSandbox(config.getSandboxToken());
    }

    public void postOrderToBuy(String figi, long quantity, Quotation price){
        SandboxService sandboxService = api.getSandboxService();

        var accId = sandboxService.getAccounts().join().get(0).getId();
        System.out.println(accId);


        var order = sandboxService.postOrder(figi, quantity, price,
                OrderDirection.ORDER_DIRECTION_BUY, accId, OrderType.ORDER_TYPE_UNSPECIFIED, UUID.randomUUID().toString()).join();

        System.out.println();
    }
}
