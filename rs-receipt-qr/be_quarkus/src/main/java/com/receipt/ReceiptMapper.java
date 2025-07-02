package com.receipt;

import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ReceiptMapper {
    @Select("SELECT * FROM receipt WHERE id = #{id}")
    @Results({
        @Result(property = "tags", column = "tags", javaType = java.util.List.class, typeHandler = org.apache.ibatis.type.ArrayTypeHandler.class)
    })
    Receipt getById(Integer id);

    @Select("SELECT * FROM receipt")
    @Results({
        @Result(property = "tags", column = "tags", javaType = java.util.List.class, typeHandler = org.apache.ibatis.type.ArrayTypeHandler.class)
    })
    List<Receipt> getAll();

    @Insert("INSERT INTO receipt(shop_id, date, total, image_path, tags) VALUES(#{shopId}, #{date}, #{total}, #{imagePath}, #{tags})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Receipt receipt);

    @Update("UPDATE receipt SET shop_id=#{shopId}, date=#{date}, total=#{total}, image_path=#{imagePath}, tags=#{tags} WHERE id=#{id}")
    void update(Receipt receipt);

    @Delete("DELETE FROM receipt WHERE id=#{id}")
    void delete(Integer id);
} 