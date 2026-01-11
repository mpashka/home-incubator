package com.receipt;

import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ReceiptRawMapper {
    @Select("SELECT receipt_raw_id AS id, user_id AS userId, url, receipt_id AS receiptId, created_at AS createdAt, status FROM receipt_raw WHERE receipt_raw_id = #{id}")
    ReceiptRaw getById(Integer id);

    @Select("SELECT receipt_raw_id AS id, user_id AS userId, url, receipt_id AS receiptId, created_at AS createdAt, status FROM receipt_raw WHERE user_id = #{userId}")
    List<ReceiptRaw> getByUserId(Integer userId);

    @Select("SELECT receipt_raw_id AS id, user_id AS userId, url, receipt_id AS receiptId, created_at AS createdAt, status FROM receipt_raw WHERE status = #{status}")
    List<ReceiptRaw> getByStatus(String status);

    @Insert("INSERT INTO receipt_raw(user_id, url, status, created_at) VALUES(#{userId}, #{url}, #{status}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "receipt_raw_id")
    void insert(ReceiptRaw receiptRaw);

    @Update("UPDATE receipt_raw SET user_id=#{userId}, url=#{url}, receipt_id=#{receiptId}, status=#{status} WHERE receipt_raw_id=#{id}")
    void update(ReceiptRaw receiptRaw);

    @Delete("DELETE FROM receipt_raw WHERE receipt_raw_id=#{id}")
    void delete(Integer id);
}
