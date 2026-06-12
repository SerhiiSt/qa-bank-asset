package unit;

import model.RebalanceResult;
import model.TradeAction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import service.RebalanceService;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Validates the core application output: the number of shares to BUY or SELL
 * for each security, per the README "Expected Output" table for Account ABC.
 *
 * | Security | Action | Shares |
 * |----------|--------|--------|
 * | IBM      | BUY    | 67     |
 * | MSFT     | NONE   | 0      |
 * | ORCL     | SELL   | 45     |
 * | AAPL     | NONE   | 0      |
 * | HD       | NONE   | 0      |
 */
@DisplayName("Rebalance Output - Share Quantity Validation")
class RebalanceServiceOutputTest {

    private final RebalanceService service = new RebalanceService();

    // -----------------------------------------------------------------------
    // Account ABC — exact expected output table from README
    // -----------------------------------------------------------------------

    @ParameterizedTest(name = "{0}: expected action={3}, expected shares={4}")
    @CsvSource({
            "IBM,  -10, 150, BUY,  67",
            "MSFT,   0,  90, HOLD,  0",
            "ORCL,  10, 220, SELL, 45",
            "AAPL,   0, 450, HOLD,  0",
            "HD,     0,  70, HOLD,  0"
    })
    @DisplayName("Account ABC: each security's output matches the README expected output table")
    void shouldMatchExpectedOutputTable(
            String symbol,
            double variancePercent,
            double unitPrice,
            TradeAction expectedAction,
            int expectedShares) {

        RebalanceResult result = service.calculate(symbol, 100_000, variancePercent, unitPrice);

        assertAll(
                () -> assertEquals(expectedAction, result.action(),
                        () -> symbol + ": expected action " + expectedAction
                                + " but got " + result.action()),
                () -> assertEquals(expectedShares, result.shares(),
                        () -> symbol + ": expected " + expectedShares
                                + " shares but got " + result.shares())
        );
    }

    // -----------------------------------------------------------------------
    // Total BUY shares across the portfolio should equal IBM's 67
    // (only IBM has a BUY action in Account ABC)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Total BUY shares for Account ABC portfolio should equal 67 (IBM only)")
    void totalBuyShares_shouldEqual67() {
        int totalBuyShares = 0;

        totalBuyShares += sharesIfAction(service.calculate("IBM",  100_000, -10, 150), TradeAction.BUY);
        totalBuyShares += sharesIfAction(service.calculate("MSFT", 100_000,   0,  90), TradeAction.BUY);
        totalBuyShares += sharesIfAction(service.calculate("ORCL", 100_000,  10, 220), TradeAction.BUY);
        totalBuyShares += sharesIfAction(service.calculate("AAPL", 100_000,   0, 450), TradeAction.BUY);
        totalBuyShares += sharesIfAction(service.calculate("HD",   100_000,   0,  70), TradeAction.BUY);

        assertEquals(67, totalBuyShares);
    }

    // -----------------------------------------------------------------------
    // Total SELL shares across the portfolio should equal ORCL's 45
    // (only ORCL has a SELL action in Account ABC)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Total SELL shares for Account ABC portfolio should equal 45 (ORCL only)")
    void totalSellShares_shouldEqual45() {
        int totalSellShares = 0;

        totalSellShares += sharesIfAction(service.calculate("IBM",  100_000, -10, 150), TradeAction.SELL);
        totalSellShares += sharesIfAction(service.calculate("MSFT", 100_000,   0,  90), TradeAction.SELL);
        totalSellShares += sharesIfAction(service.calculate("ORCL", 100_000,  10, 220), TradeAction.SELL);
        totalSellShares += sharesIfAction(service.calculate("AAPL", 100_000,   0, 450), TradeAction.SELL);
        totalSellShares += sharesIfAction(service.calculate("HD",   100_000,   0,  70), TradeAction.SELL);

        assertEquals(45, totalSellShares);
    }

    // -----------------------------------------------------------------------
    // Securities with HOLD action must always report 0 shares
    // -----------------------------------------------------------------------

    @ParameterizedTest(name = "{0} with zero variance → 0 shares")
    @CsvSource({
            "MSFT, 90",
            "AAPL, 450",
            "HD,   70"
    })
    @DisplayName("HOLD securities should always report 0 shares to buy or sell")
    void holdSecurities_shouldReportZeroShares(String symbol, double unitPrice) {
        RebalanceResult result = service.calculate(symbol, 100_000, 0, unitPrice);

        assertEquals(TradeAction.HOLD, result.action());
        assertEquals(0, result.shares());
    }

    // -----------------------------------------------------------------------
    // Share count must never be negative, regardless of BUY/SELL direction
    // -----------------------------------------------------------------------

    @ParameterizedTest(name = "variance={0}% → shares should be >= 0")
    @CsvSource({
            "-10",
            "10",
            "-50",
            "50",
            "0"
    })
    @DisplayName("Share count should always be non-negative regardless of variance sign")
    void shareCount_shouldNeverBeNegative(double variancePercent) {
        RebalanceResult result = service.calculate("TEST", 100_000, variancePercent, 100);

        assertTrue(result.shares() >= 0,
                "Shares should never be negative, got: " + result.shares());
    }

    // -----------------------------------------------------------------------
    // Helper
    // -----------------------------------------------------------------------

    private int sharesIfAction(RebalanceResult result, TradeAction action) {
        return result.action() == action ? result.shares() : 0;
    }

    private static void assertTrue(boolean condition, String message) {
        org.junit.jupiter.api.Assertions.assertTrue(condition, message);
    }
}