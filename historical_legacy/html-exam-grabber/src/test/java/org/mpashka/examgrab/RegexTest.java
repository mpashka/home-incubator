package org.mpashka.examgrab;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegexTest {
    private static final Logger log = LoggerFactory.getLogger(RegexTest.class);

    @Test
    public void regexTest() {
        String text = """
begin
<tr id="65199" data-key="122381649"><td>1</td><td>0013 ИГН_ИСТБ_БП_Д_4_22</td><td>193-633-702 87</td><td>Нет</td><td>303</td><td>293</td><td>10</td><td>93</td><td>100</td><td>100</td><td>Нет</td><td>Нет</td><td>Нет</td><td></td></tr>

<tr><th>№</th><th>Номер заявления</th><th>СНИЛС/УИА</th><th>Зачисление без ВИ</th><th>Сумма (ВИ+ИД)</th><th>Сумма (ВИ)</th><th>ИД</th><th>История</th><th>Обществознание</th><th>Русский язык</th><th>Преимущественное право на поступление</th><th>Оригинал документа об образовании</th><th>Наличие согласия на зачисление</th><th>Льгота</th></tr>
</thead>
<tbody>
<tr id="65199" data-key="122381649"><td>1</td><td>0013 ИГН_ИСТБ_БП_Д_4_22</td><td>193-633-702 87</td><td>Нет</td><td>303</td><td>293</td><td>10</td><td>93</td><td>100</td><td>100</td><td>Нет</td><td>Нет</td><td>Нет</td><td></td></tr>
<tr id="82038" data-key="122382259"><td>2</td><td>0466 ИГН_ИСТБ_БП_Д_4_22</td><td>169-606-750 09</td><td>Нет</td><td>303</td><td>293</td><td>10</td><td>93</td><td>100</td><td>100</td><td>Нет</td><td>Нет</td><td>Нет</td><td></td></tr>
                """;
        Pattern tr = Pattern.compile("<tr.*?>(.*?)</tr\\s*>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

        Matcher matcher = tr.matcher(text);
        while (matcher.find()) {
            log.info("row: {}", matcher.group(1));
        }

        log.info("row [{}]: {}", matcher.find(0), matcher.group(1));
        log.info("row [{}]: {}", matcher.find(20), matcher.group(1));
        log.info("row [{}]: {}", matcher.find(100), matcher.group(1));

    }
}
