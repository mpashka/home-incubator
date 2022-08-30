package org.mpashka.examgrab;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SiteGrabberMgpu extends SiteGrabber {

    public SiteGrabberMgpu(Saver saver, Grabber grabber) throws Exception {
        super("mgpu", saver, grabber);
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

    @Override
    public int grab(String dataStr, Saver.Course course) throws Exception {
        Matcher trMatcher = tr.matcher(dataStr);
        if (!trMatcher.find()) {
            log.error("No table");
            return -1;
        }
        String header = trMatcher.group(1);
        List<String> columns = parseRow(th, header);
        Saver.Exam exam1 = saver.getExam(columns.get(7));
        Saver.Exam exam2 = saver.getExam(columns.get(8));
        Saver.Exam exam3 = saver.getExam(columns.get(9));

        int count = 0;
        while (trMatcher.find()) {
            List<String> row = parseRow(td, trMatcher.group(1));
            Saver.Snils snils = saver.addPerson(row.get(2), null);
            if (snils == null) {
                continue;
            }
            count++;
            saver.setAddPersonExam(snils, exam1, Integer.parseInt(row.get(7)));
            saver.setAddPersonExam(snils, exam2, Integer.parseInt(row.get(8)));
            saver.setAddPersonExam(snils, exam3, Integer.parseInt(row.get(9)));
            int rawScore = Integer.parseInt(row.get(5));
            int addScore = Integer.parseInt(row.get(6));
            int score = Integer.parseInt(row.get(4));
            boolean privilege_score = "Да".equals(row.get(10));
            boolean original = "Да".equals(row.get(11));
            boolean agreement = "Да".equals(row.get(12));
            saver.addPersonCourse(snils, course, agreement, original, score, false, privilege_score, null, false, false, false);
        }
        return count;
    }

    private static final Pattern mpguDiv = Pattern.compile("<div class=\"rating-grid-table\">(.*?)</div>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern liDiv = Pattern.compile("<li>(.*?)</li>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    public void grabAll() throws Exception {
        for (int i = 2_000; i < 10_000; i++) {
            int finalI = i;
            Grabber.PageInfo pageInfo = grabber.loadPage("https://pk.mgpu.ru/pk/dictionary/competitive-group-rating?competitiveGroupId=" + i, this, true, p -> {
                if (p.getResponseCode() == 200) {
                    Matcher matcherDiv = mpguDiv.matcher(p.getBody());
                    if (matcherDiv.find()) {
                        List<String> lis = parseRow(liDiv, matcherDiv.group(1));
                        if (lis.size() > 3) {
                            String name = lis.get(0).trim() + "/" +
                                    lis.get(1).trim() + "/" +
                                    lis.get(2).trim();
                            grab(finalI, name);
                        }
                    }
                }
            }).get();
            waitIfLoaded(pageInfo);
        }
    }

    public void grabSeleted() throws Exception {
        grab(2340, "46.03.01 История/бюджет");
        grab(2506, "46.03.01 История/оплата");
        grab(2309, "44.03.05 Пед (Право, обществознание)/бюджет");
        grab(2464, "44.03.05 Пед (Право, обществознание)/оплата");
        grab(2341, "44.03.05 Пед (ИГН)/бюджет");
        grab(2508, "44.03.05 Пед (ИГН)/оплата");
    }
    
    private void grab(int num, String name) throws Exception {
        CompletableFuture<Grabber.PageInfo> future = grabber.grabCourse("https://pk.mgpu.ru/pk/dictionary/competitive-group-rating?competitiveGroupId=" + num, this, name);
        Grabber.PageInfo pageInfo = future.get(10, TimeUnit.SECONDS);
        waitIfLoaded(pageInfo);
    }
}
