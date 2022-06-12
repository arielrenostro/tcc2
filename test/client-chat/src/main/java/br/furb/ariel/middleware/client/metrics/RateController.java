package br.furb.ariel.middleware.client.metrics;

import br.furb.ariel.middleware.client.list.LinkedLimitedList;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;

public class RateController {

    private final LinkedLimitedList<Long> items = new LinkedLimitedList<>(100);
    private final String tag;

    public RateController(String tag) {
        this.tag = tag;
    }

    public void newMessage() {
        this.items.add(System.currentTimeMillis());
    }

    public void generateRate() {
        int count = 0;
        long sum = 0;
        long last = -1;

        for (int i = this.items.size() - 1; i >= 0; i--) {
            long item = this.items.get(i);
            if (item != -1 && last != -1) {
                long diff = last - item;
                sum += diff;
                count++;
            }
            last = item;
        }

        if (count < 1) {
            return;
        }

        BigDecimal bigSum = BigDecimal.valueOf(Duration.ofMillis(sum).toSeconds());
        BigDecimal bigCount = BigDecimal.valueOf(count);
        BigDecimal rate;
        try {
            // msg/s
            rate = bigCount.divide(bigSum, 10, RoundingMode.HALF_UP);
            // msg/min
            rate = rate.multiply(BigDecimal.valueOf(60));
        } catch (Exception ignored) {
            rate = BigDecimal.ZERO;
        }

        MetricsService.getInstance().publish(new RateMetric(rate, this.tag));
    }
}
