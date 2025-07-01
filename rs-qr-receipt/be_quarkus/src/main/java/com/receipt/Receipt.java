package com.receipt;

import java.util.List;

public class Receipt {
    private Integer id;
    private Integer shopId;
    private java.sql.Date date;
    private java.math.BigDecimal total;
    private String imagePath;
    private java.sql.Timestamp createdAt;
    private List<String> tags;

    // Getters and setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getShopId() { return shopId; }
    public void setShopId(Integer shopId) { this.shopId = shopId; }
    public java.sql.Date getDate() { return date; }
    public void setDate(java.sql.Date date) { this.date = date; }
    public java.math.BigDecimal getTotal() { return total; }
    public void setTotal(java.math.BigDecimal total) { this.total = total; }
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public java.sql.Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.sql.Timestamp createdAt) { this.createdAt = createdAt; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
} 