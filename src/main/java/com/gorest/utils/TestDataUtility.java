package com.gorest.utils;

import com.gorest.constants.FrameworkConstants;
import com.gorest.pojo.Post;
import com.gorest.pojo.User;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Builds unique test data. GoREST rejects a duplicate email with 422, so every
 * generated user gets a fresh one.
 */
public final class TestDataUtility {

    private static final List<String> FIRST_NAMES =
            List.of("Aarav", "Priya", "Rahul", "Neha", "Vikram", "Ananya", "Karan", "Meera");
    private static final List<String> LAST_NAMES =
            List.of("Sharma", "Patel", "Iyer", "Nair", "Singh", "Reddy", "Gupta", "Joshi");

    private static final Random RANDOM = new Random();

    private TestDataUtility() {
    }

    public static String randomName() {
        return pick(FIRST_NAMES) + " " + pick(LAST_NAMES);
    }

    /** Unique by construction: timestamp + random suffix. */
    public static String uniqueEmail() {
        return "qa.auto." + System.currentTimeMillis() + "." + ThreadLocalRandom.current().nextInt(1000, 9999)
                + "@testmail.com";
    }

    public static String randomGender() {
        return RANDOM.nextBoolean() ? FrameworkConstants.GENDER_MALE : FrameworkConstants.GENDER_FEMALE;
    }

    /** A valid, active user ready to be POSTed. */
    public static User randomUser() {
        return new User(randomName(), uniqueEmail(), randomGender(), FrameworkConstants.STATUS_ACTIVE);
    }

    public static User randomUser(String status) {
        return randomUser().setStatus(status);
    }

    public static Post randomPost() {
        return new Post("Automation post " + System.currentTimeMillis(),
                "Post body created by the GoREST REST Assured framework.");
    }

    /** An id that cannot exist, for 404 scenarios. */
    public static int nonExistingUserId() {
        return ThreadLocalRandom.current().nextInt(1, 1000);
    }

    private static String pick(List<String> values) {
        return values.get(RANDOM.nextInt(values.size()));
    }
}
