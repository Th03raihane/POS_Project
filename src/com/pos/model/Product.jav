package com.pos.model;

public class Product {
    public int id;
    public String barcode;
    public String name;
    public double price;         // Prix de vente
    public double purchasePrice; // Prix d'achat (Nouveau)
    public int stock;
    public int categoryId;       // ID Catégorie (Nouveau)

    // Le constructeur doit accepter TOUS les paramètres maintenant
    public Product(int id, String barcode, String name, double price, double purchasePrice, int stock, int categoryId) {
        this.id = id;
        this.barcode = barcode;
        this.name = name;
        this.price = price;
        this.purchasePrice = purchasePrice;
        this.stock = stock;
        this.categoryId = categoryId;
    }
}
