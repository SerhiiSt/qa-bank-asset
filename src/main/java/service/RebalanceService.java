package service;

import model.RebalanceResult;
import model.TradeAction;

public class RebalanceService {

    public RebalanceResult calculate(
            String symbol,
            double totalAsset,
            double variancePercent,
            double unitPrice) {

        if (unitPrice <= 0) {
            throw new IllegalArgumentException(
                    "Unit price must be greater than zero");
        }

        double varianceAmount =
                totalAsset * variancePercent / 100;

        int shares = (int) Math.round(
                Math.abs(varianceAmount) / unitPrice);

        TradeAction action;

        if (variancePercent < 0) {
            action = TradeAction.BUY;
        } else if (variancePercent > 0) {
            action = TradeAction.SELL;
        } else {
            action = TradeAction.HOLD;
        }

        return new RebalanceResult(
                symbol,
                action,
                shares);
    }
}
