package org.mpashka.vocabulary.core;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Правила определения части речи по исходной статье.
 *
 * <p>Часть речи в исходной базе явно не задана. Она выводится двумя путями:
 * по грамматической помете и, если пометы нет, по <b>строению статьи</b> —
 * набору форм после заглавного слова. Без строения не обойтись: в 31,5 % статей
 * (14 382 из 45 633) распознаваемой пометы нет.
 *
 * <p>Порядок правил важен: явная помета сильнее строения.
 *
 * @see <a href="../../../../../../../docs/implementation/source-db.md">docs/implementation/source-db.md</a>, раздел 6
 */
// @tag:part-of-speech
public final class PartOfSpeechRules {

    /**
     * Пометы, прямо называющие часть речи.
     *
     * <p>Ловушка исходной базы: {@code ср} здесь означает <b>«сравни»</b>, а не
     * «средний род». Средний род обозначен через {@code с}. Поэтому {@code ср}
     * в этой таблице отсутствует намеренно.
     */
    private static final Map<String, PartOfSpeech> BY_MARK = Map.ofEntries(
            Map.entry("м", PartOfSpeech.NOUN),
            Map.entry("ж", PartOfSpeech.NOUN),
            Map.entry("с", PartOfSpeech.NOUN),
            Map.entry("собир", PartOfSpeech.NOUN),
            Map.entry("сов", PartOfSpeech.VERB),
            Map.entry("несов", PartOfSpeech.VERB),
            Map.entry("многокр", PartOfSpeech.VERB),
            Map.entry("безл", PartOfSpeech.VERB),
            Map.entry("прил", PartOfSpeech.ADJECTIVE),
            Map.entry("нареч", PartOfSpeech.ADVERB),
            Map.entry("мест", PartOfSpeech.PRONOUN),
            Map.entry("вопросит", PartOfSpeech.PRONOUN),
            Map.entry("относит", PartOfSpeech.PRONOUN),
            Map.entry("числит", PartOfSpeech.NUMERAL),
            Map.entry("порядк", PartOfSpeech.NUMERAL),
            Map.entry("межд", PartOfSpeech.INTERJECTION),
            Map.entry("союз", PartOfSpeech.CONJUNCTION),
            Map.entry("предл", PartOfSpeech.PREPOSITION),
            Map.entry("частица", PartOfSpeech.PARTICLE));

    /**
     * Пометы, которые сами по себе часть речи не называют: область знания, стиль,
     * происхождение. Нужны, чтобы отличать помету от прочего текста.
     */
    private static final Set<String> OTHER_MARKS = Set.of(
            "уст", "обл", "мн", "тур", "уменьш", "увел", "бот", "разг", "ист", "зоол",
            "грам", "анат", "прост", "воен", "тех", "род", "мед", "мат", "церк", "мор",
            "хим", "лингв", "кул", "фольк", "неизм", "нескл", "ласк", "юр", "ирон", "рел",
            "муз", "презр", "спорт", "книжн", "физ", "фин", "эк", "спец", "бран", "полит",
            "бух", "вин", "геол", "геогр", "дип", "шутл", "мин", "астр", "филос", "вет",
            "биол", "театр", "полигр", "текст", "архит", "охот", "карт", "местн", "дат",
            "тв", "знач", "досл", "посл", "погов", "перен", "см", "ср");

    private PartOfSpeechRules() {
    }

    /** Является ли текст грамматической или стилистической пометой. */
    public static boolean isMark(String text) {
        return BY_MARK.containsKey(text) || OTHER_MARKS.contains(text);
    }

    /**
     * Определяет часть речи. Возвращает {@link PartOfSpeech#UNKNOWN}, если ни одно
     * правило не сработало — такое слово идёт в очередь на разбор языковой моделью.
     *
     * @param marks  грамматические пометы из начала статьи
     * @param chunks все фрагменты статьи
     */
    public static PartOfSpeech detect(List<String> marks, List<Chunk> chunks) {
        PartOfSpeech byMark = detectByMark(marks);
        return byMark != PartOfSpeech.UNKNOWN ? byMark : detectByStructure(chunks);
    }

    /**
     * Часть речи по явной грамматической помете. {@link PartOfSpeech#UNKNOWN},
     * если ни одна помета часть речи не называет.
     */
    public static PartOfSpeech detectByMark(List<String> marks) {
        for (String mark : marks) {
            PartOfSpeech byMark = BY_MARK.get(mark);
            if (byMark != null) {
                return byMark;
            }
        }
        return PartOfSpeech.UNKNOWN;
    }

    /**
     * Часть речи по строению статьи — набору форм после заглавного слова, без оглядки
     * на пометы. Вынесено отдельно, чтобы правила можно было проверять вслепую:
     * у статей с явной пометой помету скрывают и смотрят, восстановит ли её строение.
     */
    public static PartOfSpeech detectByStructure(List<Chunk> chunks) {
        if (chunks.isEmpty()) {
            return PartOfSpeech.UNKNOWN;
        }
        if (looksLikeVerb(chunks.getFirst().text())) {
            return PartOfSpeech.VERB;
        }
        if (looksLikeAdjective(chunks)) {
            return PartOfSpeech.ADJECTIVE;
        }
        return PartOfSpeech.UNKNOWN;
    }

    /**
     * Глагол опознаётся по инфинитиву: в сербском языке инфинитив оканчивается
     * на {@code -ти} либо {@code -ћи}, возвратный — с частицей {@code се}.
     *
     * <p>Проверено на всей исходной базе: из 6 165 статей с пометой {@code сов}/
     * {@code несов}/{@code многокр} на {@code -ти}/{@code -ћи} оканчиваются
     * <b>все до единой</b>. В обратную сторону ошибок 44 из 14 971 (0,3 %) — это
     * существительные во множественном числе вроде {@code Благовести}, {@code ефекти},
     * {@code девчићи}.
     *
     * <p>Прежнее правило — «вторая форма оканчивается на {@code -м}» — оказалось хуже:
     * оно принимало за глаголы {@code Амстердам (а’мстердам)} и {@code барем, ба^рем},
     * где вторая форма — всего лишь вариант написания.
     */
    private static boolean looksLikeVerb(String headword) {
        String bare = Serbian.stripAccents(Serbian.stripStemMarker(headword)).trim();
        if (bare.endsWith(" се")) {
            bare = bare.substring(0, bare.length() - 3).trim();
        }
        return bare.endsWith("ти") || bare.endsWith("ћи");
    }

    /**
     * Строение прилагательного: за заглавным словом идут формы по родам, записанные
     * через тильду ({@code абеце’д||ан, ~ни_, ~на, ~но}).
     *
     * <p>Одного лишь наличия форм с тильдой мало: через тильду записывают и падежные
     * формы существительных ({@code бе‛збедн||о_ст, ~ости}). Признаком служит именно
     * <b>пара родовых окончаний</b> — женское на {@code -а} и средний на {@code -о}.
     * Без этого условия правило принимало за прилагательные {@code безбедност} и
     * {@code детенце}.
     */
    private static boolean looksLikeAdjective(List<Chunk> chunks) {
        boolean feminine = false;
        boolean neuter = false;
        for (int i = 1; i < chunks.size(); i++) {
            Chunk chunk = chunks.get(i);
            if (chunk.isTranslation() || Chunk.SENSE_NUMBER.equals(chunk.tag())) {
                break;
            }
            // Только сербский текст статьи: ссылки (CL) — это отсылки к другим словам,
            // а не формы разбираемого.
            if (!Chunk.SERBIAN.equals(chunk.tag())) {
                continue;
            }
            // Формы пишут и через тильду (~ни_, ~на, ~но), и целиком (бе’ла, бе’ло).
            String form = Serbian.stripAccents(chunk.text());
            if (form.endsWith("а")) {
                feminine = true;
            } else if (form.endsWith("о") || form.endsWith("е")) {
                // Мягкие прилагательные имеют средний род на -е: ба“бљи, ~а_, ~е_.
                neuter = true;
            }
        }
        // Требовать ещё и мужской род на -и нельзя: у притяжательных его нет
        // (да’дин, ~а, ~о), и покрытие падает сразу на пять процентных пунктов.
        return feminine && neuter;
    }
}
