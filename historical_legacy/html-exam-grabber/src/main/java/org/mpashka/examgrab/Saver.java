package org.mpashka.examgrab;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Properties;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Saver {

    private static final Logger log = LoggerFactory.getLogger(Saver.class);

    private Connection conn;

    private PreparedStatement getCourse;
    private PreparedStatement addCourse;
    private PreparedStatement addPerson;
    private PreparedStatement addPersonCourse;
    private PreparedStatement getExam;
    private PreparedStatement addExam;
    private PreparedStatement addPersonExam;


    public Saver() throws SQLException {
//        String url = "jdbc:postgresql://localhost/grabber?currentSchema=grabber";
        String url = "jdbc:postgresql://localhost/grabber";
        Properties props = new Properties();
        props.setProperty("user","grabber");
        props.setProperty("password","grabber");
//        props.setProperty("ssl","true");
        conn = DriverManager.getConnection(url, props);

        addCourse = conn.prepareStatement("""
INSERT INTO course(institute_name, course_name) VALUES (?, ?)
    ON CONFLICT ON CONSTRAINT unique_names DO NOTHING
    RETURNING course_id;
""");
        getCourse = conn.prepareStatement("""
SELECT course_id FROM course WHERE institute_name=? AND course_name=?;
""");
        addPerson = conn.prepareStatement("""
INSERT INTO person(snils, name) VALUES (?, ?)
    ON CONFLICT ON CONSTRAINT snils_pk DO NOTHING;
""");
        addPersonCourse = conn.prepareStatement("""
INSERT INTO person_course(snils, course_id, agreement, original, score, no_exam, privilege_score, organization,
                         olympiad, privilege_total, targeted) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    ON CONFLICT ON CONSTRAINT person_course_pk DO UPDATE SET 
    agreement=?, original=?, score=?, no_exam=?, privilege_score=?, organization=?,
                         olympiad=?, privilege_total=?, targeted=?;
""");
        getExam = conn.prepareStatement("""
SELECT exam_id FROM exams WHERE exam_name=?;
""");
        addExam = conn.prepareStatement("""
INSERT INTO exams (exam_name) VALUES (?) RETURNING exam_id;
""");
        addPersonExam = conn.prepareStatement("""
INSERT INTO person_exams(snils, exam_id, result) VALUES (?, ?, ?)
    ON CONFLICT ON CONSTRAINT person_exams_pk DO UPDATE SET result=?;
""");
    }

    public Course addCourse(SiteGrabber institute, String course) throws SQLException {
        if (course.length() > 100) {
            course = minCourseName(course, 100);
        }
        getCourse.setString(1, institute.getName());
        getCourse.setString(2, course);
        try (ResultSet resultSet = getCourse.executeQuery()) {
            if (resultSet.next()) {
                int courseId = resultSet.getInt("course_id");
                return new Course(courseId);
            }
        }

        addCourse.setString(1, institute.getName());
        addCourse.setString(2, course);
        try (ResultSet resultSet = addCourse.executeQuery()) {
            if (resultSet.next()) {
                int courseId = resultSet.getInt("course_id");
                return new Course(courseId);
            }
        }

        throw new RuntimeException();
    }

    private String minCourseName(String course, int len) {
        String[] split = course.split("/");
        int maxLen = len / split.length-1;
        return Arrays.stream(split)
                .filter(s -> !s.isBlank())
                .map(s -> {
                    if (s.length() > maxLen) {
                        s = s.substring(0, maxLen);
                    }
                    return s.trim();
                })
                .collect(Collectors.joining("/"));
    }

    public Snils addPerson(String snils, String name) throws SQLException {
        snils = snils.replaceAll("[- ]", "");
        if (snils.length() != 11) {
            log.warn("Invalid snils: {}", snils);
            return null;
        }
        if (name != null) {
            addPerson.setString(1, snils);
            addPerson.setString(2, name);
            addPerson.executeUpdate();
        }
        return new Snils(snils);
    }

    public Exam getExam(String name) throws SQLException {
        name = name.toLowerCase().trim().replaceAll("[\\s+]", " ");
        if (name.endsWith(" (ви)")) {
            name = name.substring(0, name.length() - 5);
        }
        if (name.endsWith(" (профильная)")) {
            name = name.substring(0, name.length() - " (профильная)".length());
        }
        name = switch (name) {
            case "ист" -> "история";
            case "ино / общ" -> "иностранный/обществознание";
            case "рус","русский язык" -> "русский";
            case "иностранный язык (английский)" -> "английский";
//            case "биология/основы психологии" -> "биология/основы психологии";
            default -> name;
        };
        if (name.length() >= 40) {
            name = minCourseName(name, 40);
        }

        getExam.setString(1, name);
        try (ResultSet resultSet = getExam.executeQuery()) {
            if (resultSet.next()) {
                return new Exam(resultSet.getInt("exam_id"));
            }
        }

        addExam.setString(1, name);
        try (ResultSet resultSet = addExam.executeQuery()) {
            if (!resultSet.next()) {
                throw new RuntimeException("Not found exam " + name);
            }
            return new Exam(resultSet.getInt("exam_id"));
        }
    }

    public void addPersonCourse(Snils snils, Course course, boolean agreement, boolean original,
                                int score, boolean noExam, boolean privilegeScore, String organization,
                                boolean olympiad, boolean privilegeTotal, boolean targeted) throws SQLException {
        addPersonCourse.setString(1, snils.snils());
        addPersonCourse.setInt(2, course.courseId());
        addPersonCourse.setBoolean(3, agreement);
        addPersonCourse.setBoolean(4, original);
        addPersonCourse.setInt(5, score);
        addPersonCourse.setBoolean(6, noExam);
        addPersonCourse.setBoolean(7, privilegeScore);
        addPersonCourse.setString(8, organization);
        addPersonCourse.setBoolean(9, olympiad);
        addPersonCourse.setBoolean(10, privilegeTotal);
        addPersonCourse.setBoolean(11, targeted);

        addPersonCourse.setBoolean(12, agreement);
        addPersonCourse.setBoolean(13, original);
        addPersonCourse.setInt(14, score);
        addPersonCourse.setBoolean(15, noExam);
        addPersonCourse.setBoolean(16, privilegeScore);
        addPersonCourse.setString(17, organization);
        addPersonCourse.setBoolean(18, olympiad);
        addPersonCourse.setBoolean(19, privilegeTotal);
        addPersonCourse.setBoolean(20, targeted);

        addPersonCourse.executeUpdate();
    }

    public void setAddPersonExam(Snils snils, Exam exam, int result) throws SQLException {
        addPersonExam.setString(1, snils.snils());
        addPersonExam.setInt(2, exam.examId());
        addPersonExam.setInt(3, result);
        addPersonExam.setInt(4, result);
        addPersonExam.executeUpdate();
    }

    public void close() throws SQLException {
        conn.close();
    }

    public record Course(int courseId) {}
    public record Snils(String snils) {}
    public record Exam(int examId) {}

}
