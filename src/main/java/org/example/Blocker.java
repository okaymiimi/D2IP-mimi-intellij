// === Blocker.java ===
package org.example;

import org.example.model.Product;

import java.text.Normalizer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Blocker {

    // Extrahiert alphanumerische Tokens (mit Ziffern)
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

    // Extrahiert alphabetische Tokens (nur Buchstaben, ≥4 Zeichen)
    public static String generateAlternativeKey(String title) {
        if (title == null || title.isBlank()) return "";

        String normalized = normalize(title.toLowerCase());

        return Arrays.stream(normalized.split("\\s+"))
                .filter(token -> token.matches("[a-z]{4,}")) // nur alphabetisch, ≥ 4 Buchstaben
                .distinct()
                .sorted()
                .collect(Collectors.joining(" "));
    }

    // Hilfsmethode zur Entfernung von Akzenten (é → e, ü → u, etc.)
    public static String normalize(String input) {
        return Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}", "")
                .replaceAll("[^\\p{IsAlphabetic}\\s]", " "); // alles Nicht-Alphabetische raus
    }

    /*public static String generateLooseAlphabeticKey(String title) {
        String normalized = title.toLowerCase().replaceAll("[^a-z0-9 ]", " ").replaceAll("\\s+", " ").trim();
        List<String> parts = new ArrayList<>();

        for (String token : normalized.split(" ")) {
            if (token.matches("^[a-z]{2,}$")) { // mind. 2 Buchstaben, nur Buchstaben
                parts.add(token);
            }
        }

        Collections.sort(parts);
        return String.join(" ", parts);
    }*/

    // Kombiniert beide Blocking Keys (alphanumerisch & alphabetisch)
    public static Map<String, List<Integer>> createCombinedBlocks(List<Product> products) {
        Map<String, List<Integer>> blocks = new HashMap<>();

        for (int i = 0; i < products.size(); i++) {
            Set<String> keys = new HashSet<>();
            Product p1 = products.get(i);
            String text1 = (p1.name + " " + p1.price + " " + p1.brand + " " + p1.description +" "+ p1.category).toLowerCase(Locale.ROOT);

            String key1 = generateBlockingKey(text1);
            String key2 = generateAlternativeKey(text1);
            //String key3 = generateLooseAlphabeticKey(text1); // << hier

            if (!key1.isEmpty()) keys.add(key1);
            if (!key2.isEmpty()) keys.add(key2);
            //if (!key3.isEmpty()) keys.add(key3); // << hinzufügen


            for (String key : keys) {
                blocks.computeIfAbsent(key, k -> new ArrayList<>()).add(i);
            }
        }
        return blocks;
    }
}
