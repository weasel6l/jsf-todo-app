package jp.co.example.ec;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class OrderBean implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String orderNumber;
    private BigDecimal orderTotal;
    private LocalDateTime orderDate;
    private String status;

    public void createOrder(CartBean cartBean) {
        if (cartBean != null && !cartBean.isEmpty()) {
            this.orderNumber = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            this.orderTotal = cartBean.getTotal();
            this.orderDate = LocalDateTime.now();
            this.status = "COMPLETED";
        }
    }

    public void reset() {
        this.orderNumber = null;
        this.orderTotal = null;
        this.orderDate = null;
        this.status = null;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public BigDecimal getOrderTotal() {
        return orderTotal;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public String getStatus() {
        return status;
    }

    public boolean isOrderCompleted() {
        return orderNumber != null && "COMPLETED".equals(status);
    }
}