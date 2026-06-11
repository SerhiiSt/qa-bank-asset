package unit;

import model.RebalanceResult;
import model.TradeAction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import service.RebalanceService;

import static org.junit.jupiter.api.Assertions.assertEquals;


@DisplayName("Rebalance Calculation Unit Tests")
    class RebalanceServiceTest {

    private final RebalanceService service =
            new RebalanceService();

    @Test
    void shouldCalculateBuyTrade() {

        RebalanceResult result =
                service.calculate(
                        "IBM",
                        100000,
                        -10,
                        150);

        assertEquals(67, result.shares());
        assertEquals(TradeAction.BUY, result.action());
    }

    @Test
    void shouldCalculateSellTrade() {
        RebalanceResult result =
                service.calculate(
                        "ORCL",
                        100000,
                        10,
                        220);

        assertEquals(45, result.shares());
        assertEquals(TradeAction.SELL, result.action());
    }

    @Test
    void shouldHoldMSTFTrade() {
        RebalanceResult result =
                service.calculate(
                        "MSTF",
                        100000,
                        0,
                        90);

        assertEquals(0, result.shares());
        assertEquals(TradeAction.HOLD, result.action());

    }


}

