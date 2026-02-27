package jp.co.example.ec;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CartBean implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private List<CartItem> items;
    private ProductBean productBean;

    public CartBean() {
        items = new ArrayList<>();
    }

    public void setProductBean(ProductBean productBean) {
        this.productBean = productBean;
    }

    public void addToCart(long productId, int quantity) {
        if (productBean != null) {
            Optional<Product> product = productBean.findProductById(productId);
            if (product.isPresent()) {
                Product p = product.get();
                Optional<CartItem> existingItem = items.stream()
                        .filter(item -> item.getProduct().getId() == productId)
                        .findFirst();
                
                if (existingItem.isPresent()) {
                    existingItem.get().setQuantity(existingItem.get().getQuantity() + quantity);
                } else {
                    items.add(new CartItem(p, quantity));
                }
            }
        }
    }

    public void removeFromCart(long productId) {
        items.removeIf(item -> item.getProduct().getId() == productId);
    }

    public void clear() {
        items.clear();
    }

    public BigDecimal getTotal() {
        return items.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getItemCount() {
        return items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public List<CartItem> getItems() {
        return items;
    }

    public int getItemCountList() {
        return items.size();
    }
}