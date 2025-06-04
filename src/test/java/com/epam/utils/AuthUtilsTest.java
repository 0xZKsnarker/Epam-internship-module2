package com.epam.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;


class AuthUtilsTest {

    @Test
    @DisplayName("returns empty string when length is zero")
    void randomPasswordLengthZero() {
        assertEquals(0, AuthUtils.randomPassword(0).length());
    }

    @Test
    @DisplayName("returns string of requested length")
    void randomPasswordLengthMatchesRequest() {
        int requested = 12;
        assertEquals(requested, AuthUtils.randomPassword(requested).length());
    }

    @Test
    @DisplayName("contains only alpha-numeric characters")
    void randomPasswordContainsOnlyAllowedChars() {
        String allowed = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        String pw = AuthUtils.randomPassword(24);
        pw.chars()
                .mapToObj(c -> (char) c)
                .forEach(ch ->
                        assertTrue(allowed.indexOf(ch) >= 0,
                                () -> "Found disallowed char: " + ch));
    }

    @Test
    @DisplayName("successive invocations yield different values (basic randomness)")
    void randomPasswordProducesDifferentValues() {
        String first  = AuthUtils.randomPassword(10);
        String second = AuthUtils.randomPassword(10);

        assertNotEquals(first, second,
                "Two back-to-back passwords should not be identical");
    }


    @Test
    @DisplayName("returns base name when it is not taken")
    void generateUsernameBaseFree() {
        Predicate<String> nobodyTaken = name -> false;

        String result = AuthUtils.generateUsername("John", "Smith", nobodyTaken);

        assertEquals("John.Smith", result);
    }

    @Test
    @DisplayName("appends '.1' when base name already exists")
    void generateUsernameAppendsFirstSuffix() {
        Predicate<String> taken = "John.Smith"::equals;

        String result = AuthUtils.generateUsername("John", "Smith", taken);

        assertEquals("John.Smith.1", result);
    }

    @Test
    @DisplayName("increments suffix until an unused name is found")
    void generateUsernameFindsGapInSequence() {
        Set<String> reserved = Set.of("John.Smith", "John.Smith.1", "John.Smith.2");
        Predicate<String> exists = reserved::contains;

        String result = AuthUtils.generateUsername("John", "Smith", exists);

        assertEquals("John.Smith.3", result);
    }

    @Test
    @DisplayName("stops querying predicate once free name discovered")
    void generateUsernameMinimalPredicateCalls() {
        Set<String> reserved = new HashSet<>();
        reserved.add("Jane.Doe");

        final int[] counter = {0};
        Predicate<String> exists = name -> {
            counter[0]++;
            return reserved.contains(name);
        };

        String result = AuthUtils.generateUsername("Jane", "Doe", exists);

        assertEquals("Jane.Doe.1", result, "Expected first free suffix");
        assertEquals(2, counter[0],
                "Predicate should be called exactly twice (base + '.1')");
    }
}
