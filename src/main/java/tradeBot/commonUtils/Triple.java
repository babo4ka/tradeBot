package tradeBot.commonUtils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter @Setter
public class Triple <T1, T2, T3>{

    private T1 first;
    private T2 second;
    private T3 third;
}
