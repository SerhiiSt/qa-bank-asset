package model;

public record RebalanceResult(
        String symbol,
        TradeAction action,
        int shares
) {

}
