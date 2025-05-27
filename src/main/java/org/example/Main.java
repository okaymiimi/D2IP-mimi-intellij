// === Main.java ===
package org.example;

import com.opencsv.CSVReader;
import org.example.model.Pair;
import org.example.model.Product;

import java.io.*;
import java.util.*;

public class Main {

    public static List<Product> loadProducts(String filePath) throws Exception {
        List<Product> products = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", 2); // only split on first comma
                if (parts.length < 2) continue;
                try {
                    products.add(new Product(Integer.parseInt(parts[0].trim()), parts[1].trim()));
                } catch (NumberFormatException e) {
                    // skip malformed line
                }
            }
        }
        return products;
    }

    public static List<Pair> loadGroundTruth(String filePath) throws Exception {
        List<Pair> gt = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            reader.readNext(); // Skip header
            String[] line;
            while ((line = reader.readNext()) != null) {
                int id1 = Integer.parseInt(line[0]);
                int id2 = Integer.parseInt(line[1]);
                if (id1 != id2) {
                    id1 = Math.min(id1, id2);
                    id2 = Math.max(id1, id2);
                    gt.add(new Pair(id1, id2));
                }
            }
        }
        return gt;
    }

    public static void main(String[] args) throws Exception {
        String productFile = "src/main/resources/Z1_update.csv";
        String groundTruthFile = "src/main/resources/ZY1_update.csv";

        List<Product> products = loadProducts(productFile);
        List<Pair> groundTruth = loadGroundTruth(groundTruthFile);

        Map<String, List<Integer>> blocks = Blocker.createBlocks(products);

        System.out.println("------------- BEST COMBINED EVALUATION (Multi-Key Blocking) -------------");
        long start = System.currentTimeMillis();
        List<Pair> combinedMatches = Matcher.generateMatches(blocks, products, 0.63, Matcher.SimilarityType.COMBINED);
        long end = System.currentTimeMillis();

        System.out.printf("Runtime: %.2f seconds\n", (end - start) / 1000.0);
        Evaluator.evaluate(combinedMatches, groundTruth);

        // === Second Run with Alternative Blocking ===
        System.out.println("\n--- Second Run with Alternative Blocking on unmatched products ---");
        Set<Pair> matchSet = new HashSet<>(combinedMatches);
        Set<Integer> matchedIds = new HashSet<>();
        for (Pair p : combinedMatches) {
            matchedIds.add(p.getId1());
            matchedIds.add(p.getId2());
        }

        List<Product> unmatchedProducts = new ArrayList<>();
        for (Product p : products) {
            if (!matchedIds.contains(p.id)) {
                unmatchedProducts.add(p);
            }
        }

        long altStart = System.currentTimeMillis();
        Map<String, List<Integer>> altBlocks = Blocker.createAlternativeBlocks(unmatchedProducts);
        List<Pair> secondMatches = Matcher.generateMatches(altBlocks, unmatchedProducts, 0.6, Matcher.SimilarityType.COMBINED);
        long altEnd = System.currentTimeMillis();

        System.out.printf("Alternative matching runtime: %.2f seconds\n", (altEnd - altStart) / 1000.0);
        System.out.println("Alternative matches found: " + secondMatches.size());

        matchSet.addAll(secondMatches);
        List<Pair> finalMatches = new ArrayList<>(matchSet);

        System.out.println("\n--- Final Evaluation inkl. Second Run ---");
        Evaluator.evaluate(finalMatches, groundTruth);

        // === Export finaler Matches ===
        try (PrintWriter writer = new PrintWriter(new FileWriter("src/main/resources/matched_pairs.csv"))) {
            writer.println("id1,id2");
            for (Pair pair : finalMatches) {
                writer.printf("%d,%d%n", pair.getId1(), pair.getId2());
            }
            System.out.println("✅ Matches wurden exportiert nach src/main/resources/matched_pairs.csv");
        } catch (Exception e) {
            System.err.println("❌ Fehler beim Schreiben der CSV: " + e.getMessage());
        }
    }
}
