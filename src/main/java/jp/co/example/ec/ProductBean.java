package jp.co.example.ec;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductBean implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private List<Product> products;

    public ProductBean() {
        initializeProducts();
    }

    private void initializeProducts() {
        products = new ArrayList<>();
        products.add(new Product(1L, "Laptop", "High-performance laptop", new BigDecimal("999.99"), 10));
        products.add(new Product(2L, "Mouse", "Wireless mouse", new BigDecimal("29.99"), 50));
        products.add(new Product(3L, "Keyboard", "Mechanical keyboard", new BigDecimal("149.99"), 25));
        products.add(new Product(4L, "Monitor", "4K Monitor", new BigDecimal("399.99"), 15));
        products.add(new Product(5L, "Headphones", "Noise-canceling headphones", new BigDecimal("199.99"), 30));
    }

    public List<Product> getAllProducts() {
        return products;
    }

    public Optional<Product> findProductById(long id) {
        return products.stream()
                .filter(p -> p.getId() == id)
                .findFirst();
    }

    public boolean isOutOfStock(long productId) {
        return findProductById(productId)
                .map(p -> p.getStock() <= 0)
                .orElse(true);
    }
}