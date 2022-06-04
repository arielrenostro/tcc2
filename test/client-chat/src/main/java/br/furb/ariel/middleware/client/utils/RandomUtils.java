package br.furb.ariel.middleware.client.utils;

import lombok.experimental.UtilityClass;

import java.util.Random;

@UtilityClass
public class RandomUtils {

    public int range(Random random, int min, int max) {
        return min + (int) Math.floor(random.nextDouble() * (max - min));
    }
}
