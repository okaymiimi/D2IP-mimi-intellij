package org.example.model;

public class Product {
    public int id;
    public String name;
    public String price;
    public String brand;
    public String description;
    public String category;

    public Product(int id, String name, String price, String brand, String description, String category) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.brand = brand;
        this.description = description;
        this.category = category;
    }
}
