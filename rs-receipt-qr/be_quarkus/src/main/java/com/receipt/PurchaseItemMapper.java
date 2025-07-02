package com.receipt;

import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface PurchaseItemMapper {
    @Select("SELECT * FROM purchase_item WHERE id = #{id}")
    @Results({
        @Result(property = "tags", column = "tags", javaType = java.util.List.class, typeHandler = org.apache.ibatis.type.ArrayTypeHandler.class)
    })
    PurchaseItem getById(Integer id);

    @Select("SELECT * FROM purchase_item WHERE receipt_id = #{receiptId}")
    @Results({
        @Result(property = "tags", column = "tags", javaType = java.util.List.class, typeHandler = org.apache.ibatis.type.ArrayTypeHandler.class)
    })
    List<PurchaseItem> getByReceiptId(Integer receiptId);

    @Insert("INSERT INTO purchase_item(receipt_id, name, category_id, price, quantity, warranty_id, tags) VALUES(#{receiptId}, #{name}, #{categoryId}, #{price}, #{quantity}, #{warrantyId}, #{tags})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(PurchaseItem item);

    @Update("UPDATE purchase_item SET receipt_id=#{receiptId}, name=#{name}, category_id=#{categoryId}, price=#{price}, quantity=#{quantity}, warranty_id=#{warrantyId}, tags=#{tags} WHERE id=#{id}")
    void update(PurchaseItem item);

    @Delete("DELETE FROM purchase_item WHERE id=#{id}")
    void delete(Integer id);
} 