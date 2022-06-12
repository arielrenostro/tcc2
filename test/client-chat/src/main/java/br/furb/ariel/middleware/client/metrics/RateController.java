package br.furb.ariel.middleware.client.metrics;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.LinkedList;

public class RateController {

    private final LinkedList<Long> items = new LinkedList<>();

    public RateController() {
    }

    public void newMessage() {
        this.items.add(System.currentTimeMillis());
        if (this.items.size() > 100) {
            this.items.removeFirst();
        }
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

        MetricsService.getInstance().publish(new RateMetric(rate));
    }
}
