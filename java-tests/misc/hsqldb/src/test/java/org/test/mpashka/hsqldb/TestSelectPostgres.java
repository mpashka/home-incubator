package org.test.mpashka.hsqldb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class TestSelectPostgres {
    @Test
    public void testColumnName() throws SQLException {
        try (Connection con = DriverManager.getConnection("jdbc:hsqldb:mem:test;default_schema=test",
                Map.of(
                        "user", "user1",
                        "password", "pass",
                        "sql.syntax_pgs", "true",
                        "sql.lowercase_ident", "true",
                        "hsqldb.tx", "mvcc"
                ).entrySet().stream().collect(
                        Collectors.toMap(
                                e -> String.valueOf(e.getKey()),
                                e -> String.valueOf(e.getValue()),
                                (prev, next) -> next, Properties::new
                        ))
        )) {
            con.createStatement().execute("""
                    create table TEST1
                    (
                        ID bigserial not null primary key,
                        DATE1 DATE,
                        TIMESTAMP1 TIMESTAMP(6),
                        NUMBER1 bigint,
                        CLOB1 text,
                        VARCHAR2_1 text
                    );
                    
                    create table TEST2
                    (
                        ID bigserial not null primary key,
                        DATE1 DATE,
                        TIMESTAMP1 TIMESTAMP(6),
                        NUMBER1 bigint,
                        CLOB1 text,
                        VARCHAR2_1 text
                    );
                    """);

            PreparedStatement selectStatement = con.prepareStatement("""
                    SELECT t1.ID t1_id, t1.DATE1, t2.ID, t2.DATE1
                    FROM test1 t1
                        JOIN test2 t2 ON t1.ID = t2.ID
                    """);
            ResultSetMetaData metaData = selectStatement.getMetaData();
            log.info("Columns: {}", metaData.getColumnCount());
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                log.info("    Name: {}, label:{}, type: {}", metaData.getColumnName(i), metaData.getColumnLabel(i), metaData.getColumnType(i));
            }
        }
    }

    @Test
    public void testEnum() throws SQLException {
        try (Connection con = DriverManager.getConnection("jdbc:hsqldb:mem:test;default_schema=test",
                Map.of(
                        "user", "user1",
                        "password", "pass",
                        "sql.syntax_pgs", "true",
                        "sql.lowercase_ident", "true",
                        "hsqldb.tx", "mvcc"
                ).entrySet().stream().collect(
                        Collectors.toMap(
                                e -> String.valueOf(e.getKey()),
                                e -> String.valueOf(e.getValue()),
                                (prev, next) -> next, Properties::new
                        ))
        )) {
            con.createStatement().execute("""
                    CREATE TYPE yandex_group_type AS ENUM ('DEPARTMENT', 'SERVICE', 'SERVICEROLE', 'WIKI');
                    """);

        }
    }

}
