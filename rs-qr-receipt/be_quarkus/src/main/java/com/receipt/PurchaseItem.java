package com.receipt;

import java.util.List;

public class PurchaseItem {
    private Integer id;
    private Integer receiptId;
    private String name;
    private Integer categoryId;
    private java.math.BigDecimal price;
    private Integer quantity;
    private Integer warrantyId;
    private List<String> tags;

    // Getters and setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getReceiptId() { return receiptId; }
    public void setReceiptId(Integer receiptId) { this.receiptId = receiptId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
    public java.math.BigDecimal getPrice() { return price; }
    public void setPrice(java.math.BigDecimal price) { this.price = price; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Integer getWarrantyId() { return warrantyId; }
    public void setWarrantyId(Integer warrantyId) { this.warrantyId = warrantyId; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
} 