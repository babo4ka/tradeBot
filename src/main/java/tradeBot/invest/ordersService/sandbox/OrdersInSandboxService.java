package tradeBot.invest.ordersService.sandbox;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.tinkoff.piapi.contract.v1.OrderDirection;
import ru.tinkoff.piapi.contract.v1.OrderType;
import ru.tinkoff.piapi.contract.v1.Quotation;
import ru.tinkoff.piapi.core.InvestApi;
import ru.tinkoff.piapi.core.SandboxService;
import tradeBot.invest.configs.InvestConfig;
import tradeBot.invest.ordersService.CommonOrdersService;
import tradeBot.invest.shares.SharesDataLoader;

import java.util.UUID;

@Component
public class OrdersInSandboxService extends CommonOrdersService {

    public OrdersInSandboxService(InvestConfig config){
        super(config);
        api = InvestApi.createSandbox(config.getSandboxToken());
    }

    public void postOrderToBuy(String figi, long quantity, Quotation price){
        SandboxService sandboxService = api.getSandboxService();

        var accId = sandboxService.getAccounts().join().get(0).getId();
        System.out.println(accId);



        var order = sandboxService.postOrder(figi, quantity, price,
                OrderDirection.ORDER_DIRECTION_BUY, accId, OrderType.ORDER_TYPE_LIMIT, UUID.randomUUID().toString()).join();
    }


    public void postOrderToSell(String figi, Quotation price){
        SandboxService sandboxService = api.getSandboxService();

        var accId = sandboxService.getAccounts().join().get(0).getId();
        System.out.println(accId);

        var quantity = sandboxService
                .getPortfolio(accId).join()
                .getPositionsList().stream()
                .filter(pos -> pos.getFigi().equals(figi)).toList().get(0).getQuantity().getUnits();

        quantity /= api.getInstrumentsService().findInstrument(figi).join().get(0).getLot();

        var order = sandboxService.postOrder(figi, quantity, price,
                OrderDirection.ORDER_DIRECTION_SELL, accId, OrderType.ORDER_TYPE_LIMIT, UUID.randomUUID().toString()).join();
    }

}
