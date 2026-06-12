package unit;

import model.RebalanceResult;
import model.TradeAction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import service.RebalanceService;

import static org.junit.jupiter.api.Assertions.*;


@DisplayName("Rebalance Calculation Unit Tests")
    class RebalanceServiceTest {

    private final RebalanceService service =
            new RebalanceService();


    // -----------------------------------------------------------------------
    // TC-001: Buy Shares
    // IBM: variance -10%, price $150, totalAssets $100k → BUY 67 shares
    // 100000 * 10% = 10000; 10000 / 150 = 66.67 → rounded to 67
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("TC-001: IBM variance -10%, price $150 → BUY 67 shares")
    void tc001_shouldBuyIbmShares() {
        RebalanceResult result = service.calculate("IBM", 100_000, -10, 150);

        assertEquals(TradeAction.BUY, result.action());
        assertEquals(67, result.shares());
        assertEquals("IBM", result.symbol());
    }

    // -----------------------------------------------------------------------
    // TC-002: Sell Shares
    // ORCL: variance +10%, price $220, totalAssets $100k → SELL 45 shares
    // 100000 * 10% = 10000; 10000 / 220 = 45.45 → rounded to 45
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("TC-002: ORCL variance +10%, price $220 → SELL 45 shares")
    void tc002_shouldSellOrclShares() {
        RebalanceResult result = service.calculate("ORCL", 100_000, 10, 220);

        assertEquals(TradeAction.SELL, result.action());
        assertEquals(45, result.shares());
        assertEquals("ORCL", result.symbol());
    }

    // -----------------------------------------------------------------------
    // TC-003: No Action Required
    // MSFT, AAPL, HD — variance = 0 → HOLD, 0 shares
    // -----------------------------------------------------------------------

    @ParameterizedTest(name = "TC-003: {0} variance=0%, price=${1} → HOLD, 0 shares")
    @CsvSource({
            "MSFT, 90",
            "AAPL, 450",
            "HD,   70"
    })
    @DisplayName("TC-003: Zero variance → HOLD and 0 shares for MSFT, AAPL, HD")
    void tc003_shouldHoldWhenVarianceIsZero(String symbol, double unitPrice) {
        RebalanceResult result = service.calculate(symbol, 100_000, 0, unitPrice);

        assertEquals(TradeAction.HOLD, result.action());
        assertEquals(0, result.shares());
    }

    // -----------------------------------------------------------------------
    // TC-004: Multiple Securities — full portfolio
    // -----------------------------------------------------------------------

    @ParameterizedTest(name = "TC-004: {0} variance={2}%, price=${3} → {4}, {5} shares")
    @CsvSource({
            "IBM,  100000, -10, 150,  BUY,  67",
            "MSFT, 100000,   0,  90, HOLD,   0",
            "ORCL, 100000,  10, 220, SELL,  45",
            "AAPL, 100000,   0, 450, HOLD,   0",
            "HD,   100000,   0,  70, HOLD,   0"
    })
    @DisplayName("TC-004: Full portfolio — all 5 securities produce correct action and shares")
    void tc004_shouldRebalanceFullPortfolio(
            String symbol,
            double totalAsset,
            double variancePercent,
            double unitPrice,
            TradeAction expectedAction,
            int expectedShares) {

        RebalanceResult result = service.calculate(symbol, totalAsset, variancePercent, unitPrice);

        assertAll(
                () -> assertEquals(expectedAction, result.action(),
                        symbol + " — wrong action"),
                () -> assertEquals(expectedShares, result.shares(),
                        symbol + " — wrong share count")
        );
    }

    // -----------------------------------------------------------------------
    // TC-005: Decimal Share Calculation
    // totalAsset=100000, variance=-3%, price=$37
    // 100000 * 3% = 3000; 3000 / 37 = 81.08 → rounded to 81
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("TC-005: variance -3%, price $37 → BUY 81 shares (3000/37=81.08 rounds down)")
    void tc005_shouldRoundDecimalSharesCorrectly() {
        RebalanceResult result = service.calculate("XYZ", 100_000, -3, 37);

        assertEquals(TradeAction.BUY, result.action());
        assertEquals(81, result.shares());
    }

    // -----------------------------------------------------------------------
    // TC-006: Zero Price → Validation Error
    // Expected: IllegalArgumentException "Price must be greater than zero"
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("TC-006: price=0 → throws IllegalArgumentException")
    void tc006_shouldThrowExceptionForZeroPrice() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.calculate("IBM", 100_000, -10, 0)
        );
        assertTrue(
                ex.getMessage().toLowerCase().contains("price"),
                "Exception message should mention 'price', was: " + ex.getMessage()
        );
    }

    // -----------------------------------------------------------------------
    // TC-007: Negative Price → Validation Error
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("TC-007: price=-100 → throws IllegalArgumentException")
    void tc007_shouldThrowExceptionForNegativePrice() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.calculate("IBM", 100_000, -10, -100)
        );
    }

    // -----------------------------------------------------------------------
    // TC-008: Empty / blank symbol input
    //
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("TC-008: blank symbol — service either validates or returns result with blank symbol")
    void tc008_blankSymbol_shouldBeHandled() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.calculate("", 100_000, -10, 150)
        );
    }

    @Test
    @DisplayName("TC-008: null symbol — service either validates or returns result with null symbol")
    void tc008_nullSymbol_shouldBeHandled() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.calculate(null, 100_000, -10, 150)
        );
    }

    // -----------------------------------------------------------------------
    // TC-009: Large Portfolio — no arithmetic overflow
    // totalAsset = $1,000,000,000; variance -10%, price $150
    // 1_000_000_000 * 10% = 100_000_000; 100_000_000 / 150 = 666_666.67 → 666_667
    // NOTE: result.shares() is int — max int is ~2.1B, so this specific case fits,
    // but document the risk for callers using extreme inputs.
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("TC-009: totalAsset=$1B, variance -10%, price $150 → BUY 666667 shares (no overflow)")
    void tc009_shouldHandleLargePortfolioWithoutOverflow() {
        RebalanceResult result = service.calculate("IBM", 1_000_000_000, -10, 150);

        assertEquals(TradeAction.BUY, result.action());
        assertEquals(666_667, result.shares());
    }
}

