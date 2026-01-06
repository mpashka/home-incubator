package com.receipt;

import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.ArrayTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.util.List;

@Mapper
public interface ReceiptMapper {
    @Select("SELECT receipt_id AS id, user_id AS userId, shop_point_id AS shopPointId, pos_time AS posTime, total, image_path AS imagePath, created_at AS createdAt, tags FROM receipt WHERE receipt_id = #{id}")
    @Results({
        @Result(property = "tags", column = "tags", javaType = List.class, jdbcType = JdbcType.ARRAY, typeHandler = ArrayTypeHandler.class)
    })
    Receipt getById(Integer id);

    @Select("SELECT receipt_id AS id, user_id AS userId, shop_point_id AS shopPointId, pos_time AS posTime, total, image_path AS imagePath, created_at AS createdAt, tags FROM receipt")
    @Results({
        @Result(property = "tags", column = "tags", javaType = List.class, jdbcType = JdbcType.ARRAY, typeHandler = ArrayTypeHandler.class)
    })
    List<Receipt> getAll();

    @Insert("INSERT INTO receipt(user_id, shop_point_id, pos_time, total, image_path, tags) VALUES(#{userId}, #{shopPointId}, #{posTime}, #{total}, #{imagePath}, #{tags, typeHandler=org.apache.ibatis.type.ArrayTypeHandler})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "receipt_id")
    void insert(Receipt receipt);

    @Update("UPDATE receipt SET user_id=#{userId}, shop_point_id=#{shopPointId}, pos_time=#{posTime}, total=#{total}, image_path=#{imagePath}, tags=#{tags, typeHandler=org.apache.ibatis.type.ArrayTypeHandler} WHERE receipt_id=#{id}")
    void update(Receipt receipt);

    @Delete("DELETE FROM receipt WHERE receipt_id=#{id}")
    void delete(Integer id);
}
