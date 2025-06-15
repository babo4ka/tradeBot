package tradeBot.commonUtils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter @Setter
public class Pair <T1, T2>{
    private T1 first;
    private T2 second;
}
