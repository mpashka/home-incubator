package org.mpashka.examgrab;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

public class SiteGrabberMgou extends SiteGrabber {

    private static final String baseUrl = "https://epk.mgou.ru/ajax/interactive_detail?report_option=1b1fd89c-0f6e-11ed-816e-0cc47a56098d&scenario=e9903b7c-363e-11ec-8162-0cc47a56098d&" +
            "scenarioN=%D0%9A%D0%BE%D0%BD%D0%BA%D1%83%D1%80%D1%81%D0%BD%D1%8B%D0%B5%20%D1%81%D0%BF%D0%B8%D1%81%D0%BA%D0%B8" +
            "%20%D0%BF%D0%BE%20%D0%BF%D1%80%D0%BE%D0%B3%D1%80%D0%B0%D0%BC%D0%BC%D0%B5%20%D0%B1%D0%B0%D0%BA%D0%B0%D0%B2%D1%80%D0%B8%D0%B0%D1%82/" +
            "%D1%81%D0%BF%D0%B5%D1%86%D0%B8%D0%B0%D0%BB%D0%B8%D1%82%D0%B5%D1%82%20%D0%B2%202022%20%D0%B3%D0%BE%D0%B4%D1%83.";
    private static final String[] fields = {
            "level_education",
            "form_education",
            "basis_admission",
            "faculty",
            "direction",
            "profile"
    };

    private ObjectMapper objectMapper = new ObjectMapper();

    public SiteGrabberMgou(Saver saver, Grabber grabber) throws IOException {
        super("mgou", saver, grabber);
        setMyCookies();
    }

    private void setMyCookies() throws IOException {
        CookieHandler cookieHandler = grabber.getClient().cookieHandler().get();
        cookieHandler.put(URI.create("https://epk.mgou.ru/"), Map.of("Set-Cookie", List.of(
                "PHPSESSID=2a02ts5omgkna9btnkvri8ooou; path=/; HttpOnly"
        )));
    }

    @Override
    public HttpRequest request(String url) throws URISyntaxException {
        HttpRequest request = HttpRequest.newBuilder()
//                .uri(new URI("https://epk.mgou.ru/interactive_detail?report_option=1b1fd89c-0f6e-11ed-816e-0cc47a56098d&scenario=e9903b7c-363e-11ec-8162-0cc47a56098d&scenarioN=%D0%9A%D0%BE%D0%BD%D0%BA%D1%83%D1%80%D1%81%D0%BD%D1%8B%D0%B5%20%D1%81%D0%BF%D0%B8%D1%81%D0%BA%D0%B8%20%D0%BF%D0%BE%20%D0%BF%D1%80%D0%BE%D0%B3%D1%80%D0%B0%D0%BC%D0%BC%D0%B5%20%D0%B1%D0%B0%D0%BA%D0%B0%D0%B2%D1%80%D0%B8%D0%B0%D1%82/%D1%81%D0%BF%D0%B5%D1%86%D0%B8%D0%B0%D0%BB%D0%B8%D1%82%D0%B5%D1%82%20%D0%B2%202022%20%D0%B3%D0%BE%D0%B4%D1%83&level_education=5ae44d75-6649-11eb-815a-0cc47a56098d&form_education=5d15186a-403a-11eb-815a-0cc47a56098d&basis_admission=5d151866-403a-11eb-815a-0cc47a56098d%7Cfalse&faculty=5ae44d77-6649-11eb-815a-0cc47a56098d&direction=5ae44d7a-6649-11eb-815a-0cc47a56098d&profile=5ae44d7b-6649-11eb-815a-0cc47a56098d"))
//                .uri(new URI("https://epk.mgou.ru/ajax/interactive_detail?report_option=1b1fd89c-0f6e-11ed-816e-0cc47a56098d&scenario=e9903b7c-363e-11ec-8162-0cc47a56098d&scenarioN=%D0%9A%D0%BE%D0%BD%D0%BA%D1%83%D1%80%D1%81%D0%BD%D1%8B%D0%B5%20%D1%81%D0%BF%D0%B8%D1%81%D0%BA%D0%B8%20%D0%BF%D0%BE%20%D0%BF%D1%80%D0%BE%D0%B3%D1%80%D0%B0%D0%BC%D0%BC%D0%B5%20%D0%B1%D0%B0%D0%BA%D0%B0%D0%B2%D1%80%D0%B8%D0%B0%D1%82/%D1%81%D0%BF%D0%B5%D1%86%D0%B8%D0%B0%D0%BB%D0%B8%D1%82%D0%B5%D1%82%20%D0%B2%202022%20%D0%B3%D0%BE%D0%B4%D1%83&level_education=5ae44d75-6649-11eb-815a-0cc47a56098d&form_education=5d15186a-403a-11eb-815a-0cc47a56098d&basis_admission=5d151866-403a-11eb-815a-0cc47a56098d%7Cfalse&faculty=5ae44d77-6649-11eb-815a-0cc47a56098d&direction=5ae44d7a-6649-11eb-815a-0cc47a56098d&profile=5ae44d7b-6649-11eb-815a-0cc47a56098d&actions=list_applicants"))
                .uri(new URI(url))
                .version(HttpClient.Version.HTTP_1_1)
//                .followRedirects(HttpClient.Redirect.NORMAL)
                .setHeader("Referer", baseUrl + "&level_education=5ae44d75-6649-11eb-815a-0cc47a56098d&form_education=5d15186a-403a-11eb-815a-0cc47a56098d&basis_admission=5d151866-403a-11eb-815a-0cc47a56098d|false&faculty=5ae44d77-6649-11eb-815a-0cc47a56098d&direction=5ae44d7a-6649-11eb-815a-0cc47a56098d&profile=5ae44d7b-6649-11eb-815a-0cc47a56098d")
                .setHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.0.0 Safari/537.36")
                .GET()
                .build();
        return request;
    }

    public void grabHistoryFaculty() throws Exception {
        // https://epk.mgou.ru/ajax/interactive_detail?report_option=fba5b6b9-1309-11ed-816e-0cc47a56098d&scenario=e9903b7c-363e-11ec-8162-0cc47a56098d&scenarioN=%D0%9A%D0%BE%D0%BD%D0%BA%D1%83%D1%80%D1%81%D0%BD%D1%8B%D0%B5%20%D1%81%D0%BF%D0%B8%D1%81%D0%BA%D0%B8%20%D0%BF%D0%BE%20%D0%BF%D1%80%D0%BE%D0%B3%D1%80%D0%B0%D0%BC%D0%BC%D0%B5%20%D0%B1%D0%B0%D0%BA%D0%B0%D0%BB%D0%B0%D0%B2%D1%80%D0%B8%D0%B0%D1%82/%D1%81%D0%BF%D0%B5%D1%86%D0%B8%D0%B0%D0%BB%D0%B8%D1%82%D0%B5%D1%82%20%D0%B2%202022%20%D0%B3%D0%BE%D0%B4%D1%83.&
        // level_education=5ae44d75-6649-11eb-815a-0cc47a56098d&
        // form_education=5d15186a-403a-11eb-815a-0cc47a56098d&
        // basis_admission=5d151866-403a-11eb-815a-0cc47a56098d|false&
        // faculty=5ae44d77-6649-11eb-815a-0cc47a56098d&
        // actions=direction
        String levelUrl = baseUrl + "&level_education=5ae44d75-6649-11eb-815a-0cc47a56098d&form_education=5d15186a-403a-11eb-815a-0cc47a56098d&" +
                "basis_admission=" + URLEncoder.encode("5d151866-403a-11eb-815a-0cc47a56098d|false", StandardCharsets.UTF_8) + "&" +
                "faculty=5ae44d77-6649-11eb-815a-0cc47a56098d";
        grabLevel("", levelUrl, 4);
    }

    public void grabAll() throws Exception {
        String levelUrl = baseUrl + "&level_education=5ae44d75-6649-11eb-815a-0cc47a56098d";
        grabLevel("", levelUrl, 1);
    }

    public void grabSome() throws Exception {
        grabber.grabCourse(baseUrl + "&level_education=5ae44d75-6649-11eb-815a-0cc47a56098d&form_education=5d15186a-403a-11eb-815a-0cc47a56098d&basis_admission=5d151866-403a-11eb-815a-0cc47a56098d|false&faculty=5ae44d77-6649-11eb-815a-0cc47a56098d&direction=5ae44d7d-6649-11eb-815a-0cc47a56098d&profile=5ae44d81-6649-11eb-815a-0cc47a56098d&actions=list_applicants", this, "История/Исторические науки");
        grabber.grabCourse(baseUrl + "https://epk.mgou.ru/ajax/interactive_detail?report_option=fba5b6b9-1309-11ed-816e-0cc47a56098d&scenario=e9903b7c-363e-11ec-8162-0cc47a56098d&scenarioN=Конкурсные списки по программе бакалавриат/специалитет в 2022 году.&level_education=5ae44d75-6649-11eb-815a-0cc47a56098d&form_education=5d15186a-403a-11eb-815a-0cc47a56098d&basis_admission=5d151866-403a-11eb-815a-0cc47a56098d|false&faculty=5ae44d77-6649-11eb-815a-0cc47a56098d&direction=5ae44d7d-6649-11eb-815a-0cc47a56098d&profile=5ae44d81-6649-11eb-815a-0cc47a56098d&actions=list_applicants", this, "Пед/История литература");
    }

    private void grabLevel(String name, String baseUrl, int paramIdx) throws Exception {
        if (paramIdx >= fields.length) {
            grabber.grabCourse(baseUrl, this, name);
            return;
        }
        String paramName = fields[paramIdx];
        grabber.loadPage(baseUrl + "&actions=" + paramName, this, false, pForms -> {
            Map<String, Map<String, List<Map<String, String>>>> map = objectMapper.readValue(pForms.getBody(), Map.class);
            List<Map<String, String>> formEducation = map.get("data").get(paramName);
            for (Map<String, String> stringStringMap : formEducation) {
                String formId = stringStringMap.get("id");
                if (formId.equals("0")) {
                    continue;
                }
                String formName = stringStringMap.get("name");
                List<Map<String, String>> add_data = (List<Map<String, String>>) (Object) stringStringMap.get("add_data");
                if (!add_data.isEmpty()) {
                    log.info("Add data: {}", add_data);
                    for (Map<String, String> add_datum : add_data) {
                        for (String value : add_datum.values()) {
                            formId += "|" + value;
                        }
                    }
                }
                String formEducationUrl = baseUrl + "&" + paramName + "=" + URLEncoder.encode(formId, StandardCharsets.UTF_8);
                waitIfLoaded(pForms);
                grabLevel(name.isEmpty() ? formName : name + "/" + formName, formEducationUrl, paramIdx+1);
            }
        });
    }

    @Override
    public int grab(String dataStr, Saver.Course course) throws Exception {
        Map<String, Map<String, ?>> map = objectMapper.readValue(dataStr, Map.class);
        Boolean success = (Boolean) (Object) map.get("success");
        if (!Boolean.TRUE.equals(success)) {
            String message = (String) (Object) map.get("message");
            log.error("Grab error: {}", message);
            return -1;
        }
        Map<String, ?> data = map.get("data");
        List<Map<String,String>> fieldsList = (List<Map<String, String>>) data.get("list_fields");
        Map<String, String> fieldMap = fieldsList.stream().collect(Collectors.toMap(s -> s.get("field_name"), s -> s.get("field_heading")));
        Saver.Exam exam1 = saver.getExam(fieldMap.get("Предмет1"));
        Saver.Exam exam2 = saver.getExam(fieldMap.get("Предмет2"));
        Saver.Exam exam3 = saver.getExam(fieldMap.get("Предмет3"));

        List<Map<String,String>> listApplicants = (List<Map<String, String>>) data.get("list_applicants");
        for (Map<String, String> a : listApplicants) {
            // {Номер=1, УникальныйКод=164-976-238 16, СогласиеНаЗачисление=Нет, Оригинал=Нет, Предмет1=96, Предмет2=97, Предмет3=98, СуммаБалловПоПредметам=291,
            // СуммаБалловПоИДДляКонкурса=5, СуммаБаллов=296, БезВступительныхИспытаний=Нет, ЕстьПреимущественноеПраво=Нет, НаправляющаяОрганизация=, Олимпиадник=Нет,
            // Льготник=Нет, Целевик=Нет, БаллыПоПредметам=0, СуммаБалловПоИДДляСортировки=0, ФизическоеЛицо=Семеняк Полина Павловна, color=}
            String snilsStr = a.get("УникальныйКод");
            boolean agreement = a.get("СогласиеНаЗачисление").equals("Да");
            boolean original = a.get("Оригинал").equals("Да");
            int score1 = Integer.parseInt(a.get("Предмет1"));
            int score2 = Integer.parseInt(a.get("Предмет2"));
            int score3 = Integer.parseInt(a.get("Предмет3"));
            int score = Integer.parseInt(a.get("СуммаБаллов"));
            boolean no_exam = a.get("БезВступительныхИспытаний").equals("Да");
            boolean privilege_score = a.get("ЕстьПреимущественноеПраво").equals("Да");
            String organization = a.get("НаправляющаяОрганизация");
            boolean olympiad = a.get("Олимпиадник").equals("Да");
            boolean privilege_total = a.get("Льготник").equals("Да");
            boolean targeted = a.get("Целевик").equals("Да");
            String name = a.get("ФизическоеЛицо");

            Saver.Snils snils = saver.addPerson(snilsStr, name);
            saver.setAddPersonExam(snils, exam1, score1);
            saver.setAddPersonExam(snils, exam2, score2);
            saver.setAddPersonExam(snils, exam3, score3);

            saver.addPersonCourse(snils, course, agreement, original, score, no_exam, privilege_score, organization, olympiad, privilege_total, targeted);
        }
//        log.info("Read map: {}", map);
        return listApplicants.size();
    }
}
