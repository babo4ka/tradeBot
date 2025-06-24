package tradeBot.invest.ordersService.sandbox;


import org.springframework.stereotype.Component;
import ru.tinkoff.piapi.contract.v1.Operation;
import ru.tinkoff.piapi.contract.v1.OperationState;
import ru.tinkoff.piapi.contract.v1.OperationType;
import ru.tinkoff.piapi.core.InvestApi;
import ru.tinkoff.piapi.core.SandboxService;
import tradeBot.invest.configs.InvestConfig;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class StatisticsInSandbox {

    InvestApi api;

    InvestConfig config;


    public StatisticsInSandbox(InvestConfig config){
        this.config = config;

        api = InvestApi.createSandbox(config.getSandboxToken());
    }


    public double countShareProfitByTicker(String ticker){
        String figi = api.getInstrumentsService().findInstrument(ticker)
                .join().stream()
                .filter(i->i.getFigi().startsWith("BBG00"))
                .filter(i->i.getInstrumentType().equals("share"))
                .findFirst().orElseThrow().getFigi();

       return countShareProfitByFigi(figi);
    }


    public double countShareProfitByFigi(String figi){
        SandboxService sandboxService = api.getSandboxService();
        var accId = sandboxService.getAccounts().join().get(0).getId();

        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime from = now.minusYears(1);


        var operations = sandboxService
                .getOperations(accId, from.toInstant(), now.toInstant(), OperationState.OPERATION_STATE_EXECUTED, figi).join()
                .stream().filter(operation -> operation.getOperationType().equals(OperationType.OPERATION_TYPE_BUY) || operation.getOperationType().equals(OperationType.OPERATION_TYPE_SELL)).toList();

        double profit = 0;


        if(operations.stream().noneMatch(operation -> operation.getOperationType().equals(OperationType.OPERATION_TYPE_SELL))) return -1;

        int index = 0;

        while(index < operations.size()-1){
            Operation closeOperation;
            List<Operation> subList = new ArrayList<>();
            subList.add(operations.get(index));
            int subIndex = index + 1;

            while(operations.get(subIndex).getOperationType().equals(OperationType.OPERATION_TYPE_BUY)){
                subList.add(operations.get(subIndex));
                subIndex++;

                if(subIndex == operations.size()) break;
            }

            if(subIndex == operations.size()) break;

            closeOperation = operations.get(subIndex);

            double buysSum = subList.stream().mapToDouble(op -> (op.getPrice().getUnits() + op.getPrice().getNano()/1e9)).sum();

            double sellsSum = closeOperation.getPrice().getUnits() + closeOperation.getPrice().getNano()/1e9;

            profit += sellsSum - buysSum;

            index += subIndex + 1;
        }

        return profit;
    }
}
