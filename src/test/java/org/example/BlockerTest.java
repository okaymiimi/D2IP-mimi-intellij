package org.example;

import org.example.model.Product;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class BlockerTest {

    @Test
    void testGenerateBlockingKey_alphanumericPattern() {
        String title = "SanDisk Ultra 32GB SDHC UHS-I";
        String result = Blocker.generateBlockingKey(title);
        assertEquals("32gb", result); // erwartet Tokens mit mindestens einer Ziffer
    }

    @Test
    void testGenerateBlockingKey_emptyInput() {
        String title = "";
        String result = Blocker.generateBlockingKey(title);
        assertEquals("", result);
    }

    @Test
    void testCreateBlocks_singleProduct() {
        List<Product> products = List.of(new Product(0, "SanDisk Ultra 32GB SDHC UHS-I"));
        Map<String, List<Integer>> blocks = Blocker.createBlocks(products);
        assertFalse(blocks.isEmpty());
        for (List<Integer> list : blocks.values()) {
            assertTrue(list.contains(0));
        }
    }

    @Test
    void testGenerateAlternativeKey_filtersOnlyAlphaTokens() {
        String title = "USB3 64GB SanDisk Ultra SDHC";
        String result = Blocker.generateAlternativeKey(title);
        assertEquals("sandisk sdhc ultra", result); // nur alphabetisch ≥ 4 Buchstaben
    }

    @Test
    void testGenerateAlternativeKey_containsKingston() {
        String key = Blocker.generateAlternativeKey("Kingston DataTraveler 128GB USB 3.0");
        assertTrue(key.contains("kingston"));
    }

    @Test
    void testCreateAlternativeBlocks_combinedKey() {
        Product p1 = new Product(0, "Lexar USB Stick 128GB");
        Product p2 = new Product(1, "Kingston USB Drive 64GB");

        List<Product> products = List.of(p1, p2);
        Map<String, List<Integer>> blocks = Blocker.createAlternativeBlocks(products);

        // Erwarteter kombinierter Key (alphabetisch sortiert, ≥4 Buchstaben, keine Zahlen)
        String expectedKey1 = "lexar stick";      // "usb" wird gefiltert, da nur 3 Buchstaben
        String expectedKey2 = "drive kingston";   // "usb" auch hier raus

        assertTrue(blocks.containsKey(expectedKey1));
        assertTrue(blocks.get(expectedKey1).contains(0));

        assertTrue(blocks.containsKey(expectedKey2));
        assertTrue(blocks.get(expectedKey2).contains(1));
    }



}
