package tradeBot.invest.ordersService.sandbox;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.tinkoff.piapi.contract.v1.OrderDirection;
import ru.tinkoff.piapi.contract.v1.OrderType;
import ru.tinkoff.piapi.contract.v1.Quotation;
import ru.tinkoff.piapi.contract.v1.TradesStreamRequest;
import ru.tinkoff.piapi.core.InvestApi;
import ru.tinkoff.piapi.core.SandboxService;
import tradeBot.invest.ApiDistributor;
import tradeBot.invest.configs.InvestConfig;
import tradeBot.invest.ordersService.CommonOrdersService;
import tradeBot.invest.shares.SharesDataLoader;

import java.util.List;
import java.util.UUID;

@Component
@Qualifier("OrdersInSandboxService")
public class OrdersInSandboxService extends CommonOrdersService {

    public OrdersInSandboxService(InvestConfig config, ApiDistributor apiDistributor){
        super(config, apiDistributor);
    }


    public void postOrderToBuy(String figi, long quantity, Quotation price){
        SandboxService sandboxService = apiDistributor.getApi().getSandboxService();

        var accId = config.getSandboxAcc();


//        var stream = apiDistributor.getApi().getOrdersStreamService().subscribeTrades(
//                response -> {
//                    System.out.println("Sandbox Order Update: " + response);
//                    // Здесь можно обрабатывать изменения статуса заявки
//                },
//                throwable -> System.err.println("Stream error: " + throwable),
//                List.of(accId)
//        );


        var order = sandboxService.postOrder(figi, quantity, price,
                OrderDirection.ORDER_DIRECTION_BUY, accId, OrderType.ORDER_TYPE_LIMIT, UUID.randomUUID().toString()).join();


        System.out.println(order.getDirection());


    }


    public void postOrderToSell(String figi, Quotation price){
        SandboxService sandboxService = apiDistributor.getApi().getSandboxService();

        var accId = config.getSandboxAcc();

        var quantity = sandboxService
                .getPortfolio(accId).join()
                .getPositionsList().stream()
                .filter(pos -> pos.getFigi().equals(figi)).toList().get(0).getQuantity().getUnits();

        quantity /= apiDistributor.getApi().getInstrumentsService().findInstrument(figi).join().get(0).getLot();

        var order = sandboxService.postOrder(figi, quantity, price,
                OrderDirection.ORDER_DIRECTION_SELL, accId, OrderType.ORDER_TYPE_LIMIT, UUID.randomUUID().toString()).join();
    }

}
