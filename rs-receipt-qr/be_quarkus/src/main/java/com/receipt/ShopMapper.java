package com.receipt;

import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.ArrayTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.util.List;

@Mapper
public interface ShopMapper {
    @Select("SELECT shop_id AS id, name, tags FROM shop WHERE shop_id = #{id}")
    @Results({
        @Result(property = "tags", column = "tags", javaType = List.class, jdbcType = JdbcType.ARRAY, typeHandler = ArrayTypeHandler.class)
    })
    Shop getById(Integer id);

    @Select("SELECT shop_id AS id, name, tags FROM shop")
    @Results({
        @Result(property = "tags", column = "tags", javaType = List.class, jdbcType = JdbcType.ARRAY, typeHandler = ArrayTypeHandler.class)
    })
    List<Shop> getAll();

    @Insert("INSERT INTO shop(name, tags) VALUES(#{name}, #{tags, typeHandler=org.apache.ibatis.type.ArrayTypeHandler})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "shop_id")
    void insert(Shop shop);

    @Update("UPDATE shop SET name=#{name}, tags=#{tags, typeHandler=org.apache.ibatis.type.ArrayTypeHandler} WHERE shop_id=#{id}")
    void update(Shop shop);

    @Delete("DELETE FROM shop WHERE shop_id=#{id}")
    void delete(Integer id);
}
