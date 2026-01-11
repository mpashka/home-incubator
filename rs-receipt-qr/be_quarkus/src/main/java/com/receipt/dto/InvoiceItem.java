package com.receipt.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class InvoiceItem {
    @JsonProperty("name")
    private String name;

    @JsonProperty("code")
    private String code;

    @JsonProperty("unit")
    private String unit;

    @JsonProperty("quantity")
    private Double quantity;

    @JsonProperty("priceAfterVat")
    private Double priceAfterVat;

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public Double getQuantity() { return quantity; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }
    public Double getPriceAfterVat() { return priceAfterVat; }
    public void setPriceAfterVat(Double priceAfterVat) { this.priceAfterVat = priceAfterVat; }
}
