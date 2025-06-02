// === Blocker.java ===
package org.example;

import org.example.model.Product;
import org.example.model.Pair;
import org.apache.commons.text.similarity.LevenshteinDistance;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Blocker {

    public static String generateBlockingKey(String title) {
        Pattern pattern = Pattern.compile("\\b\\w*\\d+\\w*\\b");
        Matcher matcher = pattern.matcher(title);
        List<String> parts = new ArrayList<>();

        while (matcher.find()) {
            parts.add(matcher.group().toLowerCase());
        }

        Collections.sort(parts);
        return String.join(" ", parts);
    }

    public static Map<String, List<Integer>> createBlocks(List<Product> products) {
        Map<String, List<Integer>> blocks = new HashMap<>();
        for (int i = 0; i < products.size(); i++) {
            String key = generateBlockingKey(products.get(i).title);
            if (!key.isEmpty()) {
                blocks.computeIfAbsent(key, k -> new ArrayList<>()).add(i);
            }
        }
        return blocks;
    }

    // Alternative Blocking: alphabetische Tokens (>= 3 Buchstaben)
    public static String generateAlternativeKey(String title) {
        String normalized = title.toLowerCase().replaceAll("[^a-z0-9 ]", " ").replaceAll("\\s+", " ").trim();
        List<String> parts = new ArrayList<>();

        for (String token : normalized.split(" ")) {
            if (token.matches("^[a-z]{4,}$")) {
                parts.add(token);
            }
        }

        Collections.sort(parts);
        return String.join(" ", parts);
    }

    public static Map<String, List<Integer>> createAlternativeBlocks(List<Product> products) {
        Map<String, List<Integer>> blocks = new HashMap<>();
        for (int i = 0; i < products.size(); i++) {
            String key = generateAlternativeKey(products.get(i).title);
            if (!key.isEmpty()) {
                blocks.computeIfAbsent(key, k -> new ArrayList<>()).add(i);
            }
        }
        return blocks;
    }

    private static double levenshteinSim(String s1, String s2, LevenshteinDistance ld) {
        int distance = ld.apply(s1.toLowerCase(), s2.toLowerCase());
        int maxLen = Math.max(s1.length(), s2.length());
        return maxLen == 0 ? 1.0 : 1.0 - ((double) distance / maxLen);
    }
} 