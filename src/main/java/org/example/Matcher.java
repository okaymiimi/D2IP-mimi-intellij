// === Matcher.java ===
package org.example;

import org.apache.commons.text.similarity.LevenshteinDistance;
import org.example.model.Pair;
import org.example.model.Product;

import java.text.Normalizer;
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
                        String text1 = (p1.name + " " + p1.price + " " + p1.brand + " " + p1.description +" "+ p1.category).toLowerCase(Locale.ROOT);
                        String text2 = (p2.name + " " + p2.price + " " + p2.brand + " " + p2.description +" "+ p2.category).toLowerCase(Locale.ROOT);
                        double sim = switch (simType) {
                            case LEVENSHTEIN -> levenshteinSimilarity(text1, text2);
                            case COMBINED -> 0.7 * jaccardSimilarity(text1, text2)
                                    + 0.3 * levenshteinSimilarity(text1, text2);
                            default -> jaccardSimilarity(text1, text2);
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
        s1 = normalize(s1);
        s2 = normalize(s2);

        LevenshteinDistance ld = new LevenshteinDistance();
        int distance = ld.apply(s1, s2);
        int maxLen = Math.max(s1.length(), s2.length());
        return maxLen == 0 ? 1.0 : 1.0 - ((double) distance / maxLen);
    }


    private static String normalize(String s) {
        return Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "") // é -> e
                .replaceAll("[^a-z0-9 ]", " ")                         // nur a-z, 0-9 und Leerzeichen
                .replaceAll("\\s+", " ")                               // mehrfach-Whitespace reduzieren
                .toLowerCase()
                .trim();
    }
}
