package org.mpashka.vocabulary.importer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.function.Consumer;

/**
 * Последовательное чтение всех статей исходной sqlite-базы.
 *
 * <p>База открывается <b>только на чтение</b>: это чужой файл из Android-приложения,
 * менять его нельзя.
 */
// @tag:source-db @tag:import
public final class SourceReader {

    /** Путь к исходной базе по умолчанию. */
    public static final String DEFAULT_PATH =
            "/home/ya-pashka/Documents/Srpski/Recnik/srb_rus_apk/srbbase.db";

    private final String url;

    public SourceReader(String path) {
        this.url = "jdbc:sqlite:file:" + path + "?mode=ro";
    }

    /** Строка исходной таблицы {@code words}. */
    public record Row(String name, String stress, String kw, String xml) {
    }

    /** Прогоняет обработчик по всем статьям в порядке заглавного слова. */
    public void forEach(Consumer<Row> handler) {
        try (Connection connection = DriverManager.getConnection(url);
             Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery(
                     "select name, stress, kw, xml from words order by name")) {
            while (rows.next()) {
                handler.accept(new Row(rows.getString("name"), rows.getString("stress"),
                        rows.getString("kw"), rows.getString("xml")));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Не удалось прочитать исходную базу словаря", e);
        }
    }
}
