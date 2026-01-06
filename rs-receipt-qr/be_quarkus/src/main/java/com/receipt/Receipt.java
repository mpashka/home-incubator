package com.receipt;

import java.util.List;

public class Receipt {
    private Integer id;
    private Integer userId;
    private Integer shopPointId;
    private java.sql.Timestamp posTime;
    private java.math.BigDecimal total;
    private String imagePath;
    private java.sql.Timestamp createdAt;
    private List<String> tags;

    // Getters and setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public Integer getShopPointId() { return shopPointId; }
    public void setShopPointId(Integer shopPointId) { this.shopPointId = shopPointId; }
    public java.sql.Timestamp getPosTime() { return posTime; }
    public void setPosTime(java.sql.Timestamp posTime) { this.posTime = posTime; }
    public java.math.BigDecimal getTotal() { return total; }
    public void setTotal(java.math.BigDecimal total) { this.total = total; }
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public java.sql.Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.sql.Timestamp createdAt) { this.createdAt = createdAt; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
} 