// === Matcher.java ===
package org.example;

import org.apache.commons.text.similarity.LevenshteinDistance;
import org.example.model.Pair;
import org.example.model.Product;

import java.util.*;

public class Matcher {

    public enum SimilarityType {
        JACCARD,
        LEVENSHTEIN,
        COMBINED
    }

    public static List<Pair> generateMatches(Map<String, List<Integer>> blocks, List<Product> products, double threshold, SimilarityType simType) {
        List<Pair> candidatePairs = new ArrayList<>();
        Set<Pair> seenPairs = new HashSet<>();

        for (List<Integer> rowIds : blocks.values()) {
            if (rowIds.size() < 100) {
                for (int i = 0; i < rowIds.size(); i++) {
                    for (int j = i + 1; j < rowIds.size(); j++) {
                        Product p1 = products.get(rowIds.get(i));
                        Product p2 = products.get(rowIds.get(j));

                        double sim = switch (simType) {
                            case LEVENSHTEIN -> levenshteinSimilarity(p1.title, p2.title);
                            case COMBINED -> 0.7 * jaccardSimilarity(p1.title, p2.title)
                                    + 0.3 * levenshteinSimilarity(p1.title, p2.title);
                            default -> jaccardSimilarity(p1.title, p2.title);
                        };

                        if (sim >= threshold) {
                            int id1 = Math.min(p1.id, p2.id);
                            int id2 = Math.max(p1.id, p2.id);
                            Pair pair = new Pair(id1, id2);
                            if (seenPairs.add(pair)) {
                                candidatePairs.add(pair);
                            }
                        }
                    }
                }
            }
        }
        return candidatePairs;
    }

    public static double jaccardSimilarity(String s1, String s2) {
        Set<String> set1 = new HashSet<>(Arrays.asList(normalize(s1).split(" ")));
        Set<String> set2 = new HashSet<>(Arrays.asList(normalize(s2).split(" ")));

        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);

        return union.isEmpty() ? 0 : (double) intersection.size() / union.size();
    }

    public static double levenshteinSimilarity(String s1, String s2) {
        LevenshteinDistance ld = new LevenshteinDistance();
        int distance = ld.apply(s1.toLowerCase(), s2.toLowerCase());
        int maxLen = Math.max(s1.length(), s2.length());
        return maxLen == 0 ? 1.0 : 1.0 - ((double) distance / maxLen);
    }

    private static String normalize(String s) {
        return s.toLowerCase().replaceAll("[^a-z0-9 ]", " ").replaceAll("\\s+", " ").trim();
    }
}