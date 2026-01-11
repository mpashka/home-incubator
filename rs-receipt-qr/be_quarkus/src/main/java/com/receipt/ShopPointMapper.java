package com.receipt;

import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ShopPointMapper {
    @Select("SELECT shop_point_id AS id, shop_id AS shopId, tax_id AS taxId, name, location_name AS locationName, address, city, city_unit AS cityUnit FROM shop_point WHERE shop_point_id = #{id}")
    ShopPoint getById(Integer id);

    @Select("SELECT shop_point_id AS id, shop_id AS shopId, tax_id AS taxId, name, location_name AS locationName, address, city, city_unit AS cityUnit FROM shop_point WHERE tax_id = #{taxId}")
    ShopPoint getByTaxId(Integer taxId);

    @Select("SELECT shop_point_id AS id, shop_id AS shopId, tax_id AS taxId, name, location_name AS locationName, address, city, city_unit AS cityUnit FROM shop_point WHERE shop_id = #{shopId}")
    List<ShopPoint> getByShopId(Integer shopId);

    @Select("SELECT shop_point_id AS id, shop_id AS shopId, tax_id AS taxId, name, location_name AS locationName, address, city, city_unit AS cityUnit FROM shop_point")
    List<ShopPoint> getAll();

    @Insert("INSERT INTO shop_point(shop_id, tax_id, name, location_name, address, city, city_unit) VALUES(#{shopId}, #{taxId}, #{name}, #{locationName}, #{address}, #{city}, #{cityUnit})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "shop_point_id")
    void insert(ShopPoint shopPoint);

    @Update("UPDATE shop_point SET shop_id=#{shopId}, tax_id=#{taxId}, name=#{name}, location_name=#{locationName}, address=#{address}, city=#{city}, city_unit=#{cityUnit} WHERE shop_point_id=#{id}")
    void update(ShopPoint shopPoint);

    @Delete("DELETE FROM shop_point WHERE shop_point_id=#{id}")
    void delete(Integer id);
}
