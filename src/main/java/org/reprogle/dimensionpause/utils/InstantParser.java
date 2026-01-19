package org.reprogle.dimensionpause.utils;

import java.time.Instant;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InstantParser {
    private static final Pattern DURATION_PART =
            Pattern.compile("(\\d+)([wdhms])", Pattern.CASE_INSENSITIVE);

    private static final List<Character> ORDER = List.of('w', 'd', 'h', 'm', 's');

    public static Instant parseFutureInstant(String input) {
        input = input.toLowerCase().trim();

        Matcher matcher = DURATION_PART.matcher(input);
        int lastOrderIndex = -1;
        long totalSeconds = 0;
        int matchedLength = 0;

        while (matcher.find()) {
            long value = Long.parseLong(matcher.group(1));
            char unit = matcher.group(2).charAt(0);

            int orderIndex = ORDER.indexOf(unit);
            if (orderIndex == -1)
                throw new IllegalArgumentException("Invalid time unit: " + unit);

            if (orderIndex <= lastOrderIndex)
                throw new IllegalArgumentException("Invalid duration order");

            lastOrderIndex = orderIndex;
            matchedLength += matcher.group(0).length();

            totalSeconds += switch (unit) {
                case 'w' -> value * 7 * 24 * 60 * 60;
                case 'd' -> value * 24 * 60 * 60;
                case 'h' -> value * 60 * 60;
                case 'm' -> value * 60;
                case 's' -> value;
                default -> 0;
            };
        }

        if (matchedLength != input.length())
            throw new IllegalArgumentException("Invalid duration format");

        if (totalSeconds <= 0)
            throw new IllegalArgumentException("Duration must be greater than zero");

        return Instant.now().plusSeconds(totalSeconds);
    }

}
