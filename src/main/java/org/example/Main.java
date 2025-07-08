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
            String line = reader.readLine(); // header
            if (line == null) return products;

            // Expecting header: id,name,price,brand,description,category
            while ((line = reader.readLine()) != null) {
                // split into at most 6 parts, preserving empty strings
                String[] parts = line.split(",", -1);
                if (parts.length < 6) {
                    // malformed row, skip
                    continue;
                }

                try {
                    int id = Integer.parseInt(parts[0].trim());
                    String name        = parts[1].trim();
                    String price       = parts[2].trim();
                    String brand       = parts[3].trim();
                    String description = parts[4].trim();
                    String category    = parts[5].trim();

                    products.add(new Product(
                            id,
                            name,
                            price,
                            brand,
                            description,
                            category
                    ));
                } catch (NumberFormatException nfe) {

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
        String productFile = "src/main/resources/Z2.csv";
        String groundTruthFile = "src/main/resources/ZY2.csv";

        List<Product> products = loadProducts(productFile);
        List<Pair> groundTruth = loadGroundTruth(groundTruthFile);

        System.out.println("------------- COMBINED BLOCKING + MATCHING -------------");
        long start = System.currentTimeMillis();

        Map<String, List<Integer>> combinedBlocks = Blocker.createCombinedBlocks(products);
        List<Pair> matches = Matcher.generateMatches(combinedBlocks, products, 0.5, Matcher.SimilarityType.COMBINED);

        long end = System.currentTimeMillis();
        System.out.printf("Runtime: %.2f seconds\n", (end - start) / 1000.0);

        Evaluator.evaluate(matches, groundTruth);

        // Export matches
        try (PrintWriter writer = new PrintWriter(new FileWriter("src/main/resources/matched_pairs.csv"))) {
            writer.println("id1,id2");
            for (Pair pair : matches) {
                writer.printf("%d,%d%n", pair.getId1(), pair.getId2());
            }
        } catch (Exception e) {
            System.err.println("Fehler beim Schreiben der CSV: " + e.getMessage());
        }
    }
}
