package org.test.mpashka.hsqldb;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestMergeOracle {

    @Test
    public void testMerge() throws Exception {
        ResourceBundle properties = ResourceBundle.getBundle("oracle");

//        Class.forName("oracle.jdbc.driver.OracleDriver");
//        Connection c = DriverManager.getConnection("jdbc:hsqldb:file:/opt/db/testdb", "SA", "");
        try (Connection con = DriverManager.getConnection(properties.getString("url"), properties.getString("user"), properties.getString("password"))) {
            String schema = properties.getString("schema");
            if (!schema.isBlank()) {
                con.createStatement().execute(MessageFormat.format("""
                        ALTER SESSION SET CURRENT_SCHEMA={0}
                        """, schema));
            }
            createTable(con);

            try {
                test1(con);
                log.info("Test1 passed");
            } catch (SQLException e) {
                log.error("Test1 fail", e);
            }

            try {
                test2(con);
                log.info("Test2 passed");
            } catch (SQLException e) {
                log.error("Test2 fail", e);
            }

            try {
                test3(con);
                log.info("Test3 passed");
            } catch (SQLException e) {
                log.error("Test3 failed", e);
            }
        }
    }

    private static void createTable(Connection con) {
        try {
            con.createStatement().execute("""
                    create table TEST1
                    (
                        ID NUMBER(18) not null primary key,
                        DATE1 DATE,
                        TIMESTAMP1 TIMESTAMP(6),
                        NUMBER1 NUMBER(18),
                        CLOB1 CLOB,
                        VARCHAR2_1 VARCHAR2(1024 char)
                    )                    
                    """);
        } catch (SQLException e) {
            log.info("Error creating table: {}", e.getMessage());
        }
    }

    private static void test1(Connection con) throws SQLException {
        PreparedStatement preparedStatement1 = con.prepareStatement("""
                MERGE INTO TEST1 dest 
                USING (select ? AS ID, ? AS DATE1, ? AS TIMESTAMP1, ? AS NUMBER1, ? AS CLOB1, ? AS VARCHAR2_1 FROM dual) src  
                ON (src.ID = dest.ID)  
                WHEN MATCHED THEN 
                    UPDATE SET dest.DATE1 = src.DATE1, dest.TIMESTAMP1 = src.TIMESTAMP1, dest.NUMBER1 = src.NUMBER1, dest.CLOB1 = src.CLOB1, dest.VARCHAR2_1 = src.VARCHAR2_1
                WHEN NOT MATCHED THEN 
                    INSERT (ID, DATE1, TIMESTAMP1, NUMBER1, CLOB1, VARCHAR2_1) VALUES (src.ID, src.DATE1, src.TIMESTAMP1, src.NUMBER1, src.CLOB1, src.VARCHAR2_1)
                """);

        ParameterMetaData parameterMetaData = preparedStatement1.getParameterMetaData();
        for (int i = 1; i <= parameterMetaData.getParameterCount(); i++) {
            try {
                log.info("Type[{}]: {}", i, parameterMetaData.getParameterTypeName(i));
            } catch (SQLException e) {
                log.info("Can't get type [{}]: {}", i, e.getMessage());
            }
        }

        preparedStatement1.setLong(1, 1);
        preparedStatement1.setObject(2, new Date(System.currentTimeMillis()));
        preparedStatement1.setObject(3, new Date(System.currentTimeMillis()));
        preparedStatement1.setDate(2, new Date(System.currentTimeMillis()));
//        preparedStatement1.setDate(3, new Date(System.currentTimeMillis()));
        preparedStatement1.setLong(4, 4);
        preparedStatement1.setString(5, "clob1");
        preparedStatement1.setString(6, "varchar1");
        preparedStatement1.execute();
    }

    private static void test2(Connection con) throws SQLException {
        PreparedStatement preparedStatement1 = con.prepareStatement("""
                MERGE INTO TEST1 dest 
                USING (select ? AS ID, ? AS NUMBER1, ? AS CLOB1, ? AS VARCHAR2_1 FROM dual) src  
                ON (src.ID = dest.ID)  
                WHEN MATCHED THEN 
                    UPDATE SET dest.NUMBER1 = src.NUMBER1, dest.CLOB1 = src.CLOB1, dest.VARCHAR2_1 = src.VARCHAR2_1
                WHEN NOT MATCHED THEN 
                    INSERT (ID, NUMBER1, CLOB1, VARCHAR2_1) VALUES (src.ID, src.NUMBER1, src.CLOB1, src.VARCHAR2_1)
                """);

        preparedStatement1.setLong(1, 1);
        preparedStatement1.setLong(2, 4);
        preparedStatement1.setString(3, "clob1");
        preparedStatement1.setString(4, "varchar1");
        preparedStatement1.execute();
    }

    private static void test3(Connection con) throws SQLException {
        PreparedStatement preparedStatement1 = con.prepareStatement("""
                MERGE INTO TEST1 dest 
                USING (VALUES(?,?,?,?,?,?)) AS src(ID,DATE1,TIMESTAMP1,NUMBER1,CLOB1,VARCHAR2_1)  
                ON (src.ID = dest.ID)  
                WHEN MATCHED THEN 
                    UPDATE SET dest.DATE1 = src.DATE1, dest.TIMESTAMP1 = src.TIMESTAMP1, dest.NUMBER1 = src.NUMBER1, dest.CLOB1 = src.CLOB1, dest.VARCHAR2_1 = src.VARCHAR2_1
                WHEN NOT MATCHED THEN 
                    INSERT (ID, DATE1, TIMESTAMP1, NUMBER1, CLOB1, VARCHAR2_1) VALUES (src.ID, src.DATE1, src.TIMESTAMP1, src.NUMBER1, src.CLOB1, src.VARCHAR2_1)
                """);

        preparedStatement1.setLong(1, 1);
        preparedStatement1.setObject(2, new Date(System.currentTimeMillis()));
        preparedStatement1.setObject(3, new Date(System.currentTimeMillis()));
        preparedStatement1.setDate(2, new Date(System.currentTimeMillis()));
//        preparedStatement1.setDate(3, new Date(System.currentTimeMillis()));
        preparedStatement1.setLong(4, 4);
        preparedStatement1.setString(5, "clob1");
        preparedStatement1.setString(6, "varchar1");
        preparedStatement1.execute();
    }

}
