package org.mpashka.examgrab;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SiteGrabberMpgu extends SiteGrabber {

    public SiteGrabberMpgu(Saver saver, Grabber grabber) throws Exception {
        super("mpgu", saver, grabber);
//        login(grabber);
        setMyCookies();
    }

    private void setMyCookies() throws IOException {
        CookieHandler cookieHandler = grabber.getClient().cookieHandler().get();
        cookieHandler.put(URI.create("https://sdo.mpgu.org/"), Map.of("Set-Cookie", List.of(
                // https://sdo.mpgu.org/
//                "_csrf-frontend=986de96809f8d9a4d1768bb541c31335803a9c6498016449759ec1535d6f7949a%3A2%3A%7Bi%3A0%3Bs%3A14%3A%22_csrf-frontend%22%3Bi%3A1%3Bs%3A32%3A%22knB5PnkcO85qF8Rpaviy2k0AXyABk4J3%22%3B%7D; path=/; HttpOnly; SameSite=Lax",
                // https://sdo.mpgu.org/site/anti-ddos
                "cookieAntiDdos=1160e6bfbcdc91a428a8333db0d3819d9818b0a3db708d4cb9ee1d2d3cf71d34a%3A2%3A%7Bi%3A0%3Bs%3A14%3A%22cookieAntiDdos%22%3Bi%3A1%3Bs%3A32%3A%22lbQRKeaC0hLKOAmpvPk8opfb0zmh1Uqg%22%3B%7D; path=/; HttpOnly; SameSite=Lax",
                // https://sdo.mpgu.org/site/index
//                "PHPSESSID=vdm20591gfqhsdjsnqcl1df236; path=/; HttpOnly",

/*
youtube
                "YSC=H8MDHCH3Tgk; Domain=.youtube.com; Path=/; Secure; HttpOnly; SameSite=none",
                "VISITOR_INFO1_LIVE=iohYuZpG6Wk; Domain=.youtube.com; Expires=Mon, 30-Jan-2023 03:51:11 GMT; Path=/; Secure; HttpOnly; SameSite=none",
*/

                // https://sdo.mpgu.org/account/login GET
                // https://sdo.mpgu.org/account/login POST
                "PHPSESSID=elln9hl8kfdpkpjldlr4c3otv6; path=/; HttpOnly",
                "_csrf-frontend=fe03b5fbedf1d87718443f92b371916fce0b4f96877cfaf9ca251f1693caa897a%3A2%3A%7Bi%3A0%3Bs%3A14%3A%22_csrf-frontend%22%3Bi%3A1%3Bs%3A32%3A%22WmQgP06qVpVTWuaPpCfH3o3ntOx_FfQG%22%3B%7D; path=/; HttpOnly; SameSite=Lax"

                // https://sdo.mpgu.org/
        )));
    }

    private void login(Grabber grabber) throws Exception {
        HttpClient client = grabber.getClient();

        HttpRequest ddosRequest = HttpRequest.newBuilder()
                .uri(new URI("https://sdo.mpgu.org/site/anti-ddos"))
                .version(HttpClient.Version.HTTP_1_1)
//                .followRedirects(HttpClient.Redirect.NORMAL)
                .setHeader("Referer", "https://sdo.mpgu.org/account/login")
                .setHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.0.0 Safari/537.36")
                .setHeader("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(""))
                .build();
        CompletableFuture<String> ddosResponse = client.sendAsync(ddosRequest, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
        String ddosResult = ddosResponse.get(10, TimeUnit.SECONDS);
        log.info("Ddos result: {}", ddosResult);


        HttpRequest loginRequest = HttpRequest.newBuilder()
                .uri(new URI("https://sdo.mpgu.org/account/login"))
                .version(HttpClient.Version.HTTP_1_1)
//                .followRedirects(HttpClient.Redirect.NORMAL)
                .setHeader("Referer", "https://sdo.mpgu.org/account/login")
                .setHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.0.0 Safari/537.36")
                .setHeader("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(
                        URLEncoder.encode("LoginForm[username]", StandardCharsets.UTF_8) + "=" +
                                URLEncoder.encode("danilka.moukhataev@gmail.com", StandardCharsets.UTF_8) + "&" +
                                URLEncoder.encode("LoginForm[password]", StandardCharsets.UTF_8) + "=" +
                                URLEncoder.encode("os82wWCZ", StandardCharsets.UTF_8) + "&" +
                                URLEncoder.encode("LoginForm[rememberMe]", StandardCharsets.UTF_8) + "=1"
                ))
                .build();
        CompletableFuture<String> loginResponse = client.sendAsync(loginRequest, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
        String loginResult = loginResponse.get(10, TimeUnit.SECONDS);
        log.info("Login result: {}", loginResult);
    }

    @Override
    public HttpRequest request(String url) throws URISyntaxException {
        HttpRequest request = HttpRequest.newBuilder()
//                .uri(new URI("https://epk.mgou.ru/interactive_detail?report_option=1b1fd89c-0f6e-11ed-816e-0cc47a56098d&scenario=e9903b7c-363e-11ec-8162-0cc47a56098d&scenarioN=%D0%9A%D0%BE%D0%BD%D0%BA%D1%83%D1%80%D1%81%D0%BD%D1%8B%D0%B5%20%D1%81%D0%BF%D0%B8%D1%81%D0%BA%D0%B8%20%D0%BF%D0%BE%20%D0%BF%D1%80%D0%BE%D0%B3%D1%80%D0%B0%D0%BC%D0%BC%D0%B5%20%D0%B1%D0%B0%D0%BA%D0%B0%D0%B2%D1%80%D0%B8%D0%B0%D1%82/%D1%81%D0%BF%D0%B5%D1%86%D0%B8%D0%B0%D0%BB%D0%B8%D1%82%D0%B5%D1%82%20%D0%B2%202022%20%D0%B3%D0%BE%D0%B4%D1%83&level_education=5ae44d75-6649-11eb-815a-0cc47a56098d&form_education=5d15186a-403a-11eb-815a-0cc47a56098d&basis_admission=5d151866-403a-11eb-815a-0cc47a56098d%7Cfalse&faculty=5ae44d77-6649-11eb-815a-0cc47a56098d&direction=5ae44d7a-6649-11eb-815a-0cc47a56098d&profile=5ae44d7b-6649-11eb-815a-0cc47a56098d"))
//                .uri(new URI("https://epk.mgou.ru/ajax/interactive_detail?report_option=1b1fd89c-0f6e-11ed-816e-0cc47a56098d&scenario=e9903b7c-363e-11ec-8162-0cc47a56098d&scenarioN=%D0%9A%D0%BE%D0%BD%D0%BA%D1%83%D1%80%D1%81%D0%BD%D1%8B%D0%B5%20%D1%81%D0%BF%D0%B8%D1%81%D0%BA%D0%B8%20%D0%BF%D0%BE%20%D0%BF%D1%80%D0%BE%D0%B3%D1%80%D0%B0%D0%BC%D0%BC%D0%B5%20%D0%B1%D0%B0%D0%BA%D0%B0%D0%B2%D1%80%D0%B8%D0%B0%D1%82/%D1%81%D0%BF%D0%B5%D1%86%D0%B8%D0%B0%D0%BB%D0%B8%D1%82%D0%B5%D1%82%20%D0%B2%202022%20%D0%B3%D0%BE%D0%B4%D1%83&level_education=5ae44d75-6649-11eb-815a-0cc47a56098d&form_education=5d15186a-403a-11eb-815a-0cc47a56098d&basis_admission=5d151866-403a-11eb-815a-0cc47a56098d%7Cfalse&faculty=5ae44d77-6649-11eb-815a-0cc47a56098d&direction=5ae44d7a-6649-11eb-815a-0cc47a56098d&profile=5ae44d7b-6649-11eb-815a-0cc47a56098d&actions=list_applicants"))
                .uri(new URI(url))
                .version(HttpClient.Version.HTTP_1_1)
//                .followRedirects(HttpClient.Redirect.NORMAL)
                .setHeader("Referer", "https://sdo.mpgu.org/competition-list/bachelor?faculty=38")
                .setHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.0.0 Safari/537.36")
                .GET()
                .build();
        return request;
    }

    private int[] faculties = {8,1,2,3,17,11,12,15,38,32,10,16,41,7,5,6,4,13};
    public void grabAll() throws Exception {
        for (int faculty : faculties) {
            CompletableFuture<Grabber.PageInfo> pageInfoFuture = grabFaculty(faculty);
            Grabber.PageInfo pageInfo = pageInfoFuture.get(260, TimeUnit.SECONDS);
            waitIfLoaded(pageInfo);
        }
    }

    private Pattern imgPattern = Pattern.compile("<img src=\"/img/cabinet/(.*).png\">");
    private Map<String, String> types = Map.of(
            "b", "бюджет",
            "dg", "платные",
            "lg", "льготные",
            "c", "целевые",
            "spec_quota", "спец квота"
    );
    private CompletableFuture<Grabber.PageInfo> grabFaculty(int id) throws Exception {
        return grabber.loadPage("https://sdo.mpgu.org/competition-list/bachelor?faculty=" + id, this, false, p -> {
            Matcher trMatcher = tr.matcher(p.getBody());
            if (!trMatcher.find()) {
                log.error("No table in {}", p.getNumber());
                return;
            }
            while (trMatcher.find()) {
                List<String> columns = parseRow(th, trMatcher.group(1));
                String name = columns.get(0).trim().replaceAll("\\s+", " ") + "/" + columns.get(1).trim();

                Matcher aMatcher = a.matcher(columns.get(2));
                while (aMatcher.find()) {
                    String href = aMatcher.group(1);
                    href = href.replaceAll("&amp;", "&");
                    String imgTag = aMatcher.group(2);
                    String img = "unknown";

                    Matcher imgMatcher = imgPattern.matcher(imgTag.trim());
                    if (imgMatcher.find()) {
                        String imgName = imgMatcher.group(1);
                        img = types.getOrDefault(imgName, imgName);
//                        log.warn("Unknown img: {}", k);
                    } else {
                        log.warn("Unknown a body: {}", imgTag);
                    }

                    waitIfLoaded(p);

                    String url = "https://sdo.mpgu.org" + href;
                    log.info("Grab course {} from {}: {}", url, img, name);
                    grabber.grabCourse(url, this, name + "/" + img);
                }
            }
        });
    }

    @Override
    public int grab(String dataStr, Saver.Course course) throws Exception {
        Matcher trMatcher = tr.matcher(dataStr);
        if (!trMatcher.find()) {
            log.error("No table");
            return -1;
        }
        String header = trMatcher.group(1);
        List<String> columns = parseRow(th, header);
        Saver.Exam exam1 = saver.getExam(columns.get(2));
        Saver.Exam exam2 = saver.getExam(columns.get(3));
        Saver.Exam exam3 = saver.getExam(columns.get(4));

        int colScore = columns.indexOf("Сумма баллов");
        int colAgreement = columns.indexOf("Согласие на зачисление подано (+) / отсутствует (-)");
        int colOriginal = columns.indexOf("Подача документа об образовании");
        int maxCol = Math.max(colAgreement, colScore);
        maxCol = Math.max(maxCol, colOriginal);

        int count = 0;
        if (!trMatcher.find()) {
            return 0;
        }
        String[] rows = trStart.split(trMatcher.group(1));
        for (String rowStr : rows) {
            List<String> row = parseRow(td, rowStr);
            if (row.size() <= maxCol) {
                continue;
            }
            Saver.Snils snils = saver.addPerson(row.get(1), null);
            if (snils == null) {
                continue;
            }
            count++;
            parseExam(snils, exam1, row.get(2));
            parseExam(snils, exam2, row.get(3));
            parseExam(snils, exam3, row.get(4));
//            int rawScore = Integer.parseInt(row.get(5));
//            int addScore = Integer.parseInt(row.get(7));
            int score = Integer.parseInt(row.get(colScore));
            boolean agreement = "+".equals(row.get(colAgreement));
            boolean original = "оригинал".equals(row.get(colOriginal));
            saver.addPersonCourse(snils, course, agreement, original, score, false, false, null, false, false, false);
        }
        return count;
    }

    private static final Pattern num = Pattern.compile("([\\d]+)");
    private void parseExam(Saver.Snils snils, Saver.Exam exam, String str) throws SQLException {
        Matcher numMatcher = num.matcher(str);
        if (numMatcher.find()) {
            saver.setAddPersonExam(snils, exam, Integer.parseInt(numMatcher.group(1)));
        }
    }

}
