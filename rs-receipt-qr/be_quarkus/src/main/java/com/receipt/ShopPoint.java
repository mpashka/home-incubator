package com.receipt;

public class ShopPoint {
    private Integer id;
    private Integer shopId;
    private Integer taxId;
    private String name;
    private String locationName;
    private String address;
    private String city;
    private String cityUnit;

    // Getters and setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getShopId() { return shopId; }
    public void setShopId(Integer shopId) { this.shopId = shopId; }
    public Integer getTaxId() { return taxId; }
    public void setTaxId(Integer taxId) { this.taxId = taxId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getCityUnit() { return cityUnit; }
    public void setCityUnit(String cityUnit) { this.cityUnit = cityUnit; }
}
