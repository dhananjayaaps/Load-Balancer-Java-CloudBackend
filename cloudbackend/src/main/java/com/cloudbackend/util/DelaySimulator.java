package com.cloudbackend.util;

import java.util.concurrent.ThreadLocalRandom;

public class DelaySimulator {

    public static void applyArtificialDelay() {
        try {
            // Generate a random delay between 30 and 90 seconds
            int delayInSeconds = ThreadLocalRandom.current().nextInt(30, 91);
            System.out.println("Applying artificial delay of " + delayInSeconds + " seconds.");
            Thread.sleep(delayInSeconds * 1000L); // Convert seconds to milliseconds
        } catch (InterruptedException e) {
            System.err.println("Artificial delay interrupted: " + e.getMessage());
        }
    }
}
