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

    public static List<Pair> matchTopKLevenshteinFallback(List<Product> products, Set<Pair> existingMatches, double threshold, int topK, int maxProducts) {
        Set<Integer> matchedIds = new HashSet<>();
        for (Pair p : existingMatches) {
            matchedIds.add(p.getId1());
            matchedIds.add(p.getId2());
        }

        List<Product> unmatched = new ArrayList<>();
        for (Product p : products) {
            if (!matchedIds.contains(p.id)) {
                unmatched.add(p);
            }
        }

        Collections.shuffle(unmatched);
        unmatched = unmatched.subList(0, Math.min(unmatched.size(), maxProducts));

        List<Pair> results = new ArrayList<>();
        LevenshteinDistance ld = new LevenshteinDistance();
        Set<Pair> seen = new HashSet<>();

        for (Product pivot : unmatched) {
            PriorityQueue<Product> pq = new PriorityQueue<>(Comparator.comparingDouble(p ->
                    -levenshteinSim(pivot.title, p.title, ld)));

            for (Product other : products) {
                if (pivot.id == other.id || matchedIds.contains(other.id)) continue;
                pq.offer(other);
            }

            int count = 0;
            while (!pq.isEmpty() && count < topK) {
                Product candidate = pq.poll();
                double sim = levenshteinSim(pivot.title, candidate.title, ld);
                if (sim >= threshold) {
                    int id1 = Math.min(pivot.id, candidate.id);
                    int id2 = Math.max(pivot.id, candidate.id);
                    Pair pair = new Pair(id1, id2);
                    if (!existingMatches.contains(pair) && seen.add(pair)) {
                        results.add(pair);
                    }
                }
                count++;
            }
        }

        return results;
    }

    private static double levenshteinSim(String s1, String s2, LevenshteinDistance ld) {
        int distance = ld.apply(s1.toLowerCase(), s2.toLowerCase());
        int maxLen = Math.max(s1.length(), s2.length());
        return maxLen == 0 ? 1.0 : 1.0 - ((double) distance / maxLen);
    }
}  