package tradeBot.invest.ordersService;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class TradeInfo {

    private final List<Double> purchases = new ArrayList<>();

    @Setter
    private double sale;
    @Setter
    private boolean closed;


    public void addPurchase(double purchase){
        this.purchases.add(purchase);
    }
}
