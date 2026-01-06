package com.receipt;

import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.ArrayTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.util.List;

@Mapper
public interface ReceiptItemMapper {
    @Select("SELECT * FROM receipt_item WHERE receipt_item_id = #{id}")
    @Results({
        @Result(property = "id", column = "receipt_item_id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "receiptId", column = "receipt_id"),
        @Result(property = "categoryId", column = "category_id"),
        @Result(property = "warrantyId", column = "warranty_id"),
        @Result(property = "tags", column = "tags", javaType = List.class, jdbcType = JdbcType.ARRAY, typeHandler = ArrayTypeHandler.class)
    })
    ReceiptItem getById(Integer id);

    @Select("SELECT * FROM receipt_item WHERE receipt_id = #{receiptId}")
    @Results({
        @Result(property = "id", column = "receipt_item_id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "receiptId", column = "receipt_id"),
        @Result(property = "categoryId", column = "category_id"),
        @Result(property = "warrantyId", column = "warranty_id"),
        @Result(property = "tags", column = "tags", javaType = List.class, jdbcType = JdbcType.ARRAY, typeHandler = ArrayTypeHandler.class)
    })
    List<ReceiptItem> getByReceiptId(Integer receiptId);

    @Select("SELECT * FROM receipt_item WHERE user_id = #{userId}")
    @Results({
        @Result(property = "id", column = "receipt_item_id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "receiptId", column = "receipt_id"),
        @Result(property = "categoryId", column = "category_id"),
        @Result(property = "warrantyId", column = "warranty_id"),
        @Result(property = "tags", column = "tags", javaType = List.class, jdbcType = JdbcType.ARRAY, typeHandler = ArrayTypeHandler.class)
    })
    List<ReceiptItem> getByUserId(Integer userId);

    @Insert("INSERT INTO receipt_item(user_id, receipt_id, name, category_id, price, quantity, warranty_id, tags) VALUES(#{userId}, #{receiptId}, #{name}, #{categoryId}, #{price}, #{quantity}, #{warrantyId}, #{tags, typeHandler=org.apache.ibatis.type.ArrayTypeHandler})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "receipt_item_id")
    void insert(ReceiptItem item);

    @Update("UPDATE receipt_item SET user_id=#{userId}, receipt_id=#{receiptId}, name=#{name}, category_id=#{categoryId}, price=#{price}, quantity=#{quantity}, warranty_id=#{warrantyId}, tags=#{tags, typeHandler=org.apache.ibatis.type.ArrayTypeHandler} WHERE receipt_item_id=#{id}")
    void update(ReceiptItem item);

    @Delete("DELETE FROM receipt_item WHERE receipt_item_id=#{id}")
    void delete(Integer id);
}
