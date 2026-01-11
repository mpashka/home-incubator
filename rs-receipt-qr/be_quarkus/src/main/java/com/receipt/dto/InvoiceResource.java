package com.receipt.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class InvoiceResource {
    @JsonProperty("iic")
    private String iic;

    @JsonProperty("totalPrice")
    private Double totalPrice;

    @JsonProperty("dateTimeCreated")
    private String dateTimeCreated;

    @JsonProperty("issuerTaxNumber")
    private String issuerTaxNumber;

    @JsonProperty("items")
    private List<InvoiceItem> items;

    // Getters and setters
    public String getIic() { return iic; }
    public void setIic(String iic) { this.iic = iic; }
    public Double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(Double totalPrice) { this.totalPrice = totalPrice; }
    public String getDateTimeCreated() { return dateTimeCreated; }
    public void setDateTimeCreated(String dateTimeCreated) { this.dateTimeCreated = dateTimeCreated; }
    public String getIssuerTaxNumber() { return issuerTaxNumber; }
    public void setIssuerTaxNumber(String issuerTaxNumber) { this.issuerTaxNumber = issuerTaxNumber; }
    public List<InvoiceItem> getItems() { return items; }
    public void setItems(List<InvoiceItem> items) { this.items = items; }
}
