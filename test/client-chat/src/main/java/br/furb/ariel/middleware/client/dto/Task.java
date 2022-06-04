package br.furb.ariel.middleware.client.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Data
@NoArgsConstructor
public class Task {

    private int clients;
    private Rate rate;

    @Data
    @NoArgsConstructor
    public static class Rate {

        private TimeUnit unit;
        private int value;

        public Duration getTimeRate() {
            long unitInMillis = this.unit.toMillis(1);
            return Duration.ofMillis(unitInMillis / this.value);
        }

        public String toString() {
            return this.value + " per " + this.unit;
        }
    }
}
