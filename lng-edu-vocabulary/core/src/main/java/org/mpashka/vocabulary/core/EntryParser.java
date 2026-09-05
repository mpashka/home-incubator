package org.mpashka.vocabulary.core;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Собирает {@link Entry} из строки исходной базы.
 *
 * <p>Опорные признаки разметки, на которых держится разбор (проверены на всей базе):
 * <ul>
 *   <li>первый фрагмент статьи — всегда заглавное слово с пометой {@code C};</li>
 *   <li>перевод <b>заглавного слова</b> помечен {@code RV}, а перевод <b>примера</b> —
 *       пустой пометой. Это единственный способ отличить одно от другого;</li>
 *   <li>границы переводов берутся из поля {@code kw}: относительно групп {@code RV}
 *       оно только склеивает и никогда не дробит мельче (0 исключений на 42 тыс. статей).</li>
 * </ul>
 *
 * @see <a href="../../../../../../../docs/implementation/source-db.md">docs/implementation/source-db.md</a>
 */
// @tag:markup
public final class EntryParser {

    /** Знак начала блока устойчивых оборотов. */
    private static final String IDIOM_MARKER = "◊";

    private EntryParser() {
    }

    /**
     * Разбирает статью.
     *
     * @param name   поле {@code words.name} — латиница без ударений
     * @param kw     поле {@code words.kw} — переводы через {@code ;}
     * @param markup поле {@code words.xml}
     */
    public static Entry parse(String name, String kw, String markup) {
        List<Chunk> chunks = MarkupParser.parse(markup);
        if (chunks.isEmpty()) {
            return empty(name);
        }

        String rawHeadword = chunks.get(0).text();
        String headword = Serbian.stripStemMarker(rawHeadword);
        List<String> marks = collectMarks(chunks);
        List<String> keywords = splitKeywords(kw);

        Body body = readBody(chunks, rawHeadword, keywords);
        if (body.senses.isEmpty()) {
            // Отсылочные статьи («см. …») не содержат ни одного фрагмента RV,
            // но переводы у них есть — в поле kw. Это единственный их источник.
            body.senses.add(new Entry.Sense(null,
                    keywords.stream().map(Russian::renderStress).toList(), List.of()));
        }

        return new Entry(
                name,
                Serbian.renderAccents(headword),
                Serbian.renderAccents(Serbian.toLatin(headword)),
                marks,
                PartOfSpeechRules.detect(marks, chunks),
                body.senses,
                body.idioms,
                WordStatus.IMPORTED);
    }

    /** Тело статьи: значения и устойчивые обороты. */
    private static final class Body {
        final List<Entry.Sense> senses = new ArrayList<>();
        final List<Entry.Example> idioms = new ArrayList<>();
    }

    private static Body readBody(List<Chunk> chunks, String rawHeadword, List<String> keywords) {
        Body body = new Body();
        Set<String> keywordSet = new LinkedHashSet<>(keywords);

        Integer senseNumber = null;
        List<String> translations = new ArrayList<>();
        List<Entry.Example> examples = new ArrayList<>();
        StringBuilder translationBuffer = new StringBuilder();
        StringBuilder serbian = new StringBuilder();
        StringBuilder russian = new StringBuilder();
        boolean inIdioms = false;

        // «Шапка» — всё до первого перевода или номера значения: заглавное слово, его
        // акцентные варианты (го‛ра, го“ра), пометы и римская цифра омонима. Примеров
        // здесь быть не может, иначе вариант заглавного слова уедет в пример.
        boolean inHeader = true;

        for (int i = 1; i < chunks.size(); i++) {
            Chunk chunk = chunks.get(i);

            // Точка в самом конце завершает статью целиком (так в 45 493 статьях
            // из 45 633) и к переводу последнего примера не относится.
            if (i == chunks.size() - 1 && !chunk.hasTag() && chunk.text().equals(".")) {
                break;
            }

            // Омонимы упакованы в одну строку и разделены разрывом абзаца. После него
            // начинается новая статья со своей шапкой.
            if (!chunk.hasTag() && chunk.text().startsWith("@")) {
                flushExample(serbian, russian, rawHeadword, inIdioms ? body.idioms : examples);
                flushTranslation(translationBuffer, translations);
                if (!translations.isEmpty() || !examples.isEmpty()) {
                    body.senses.add(new Entry.Sense(senseNumber, List.copyOf(translations), List.copyOf(examples)));
                    translations.clear();
                    examples.clear();
                }
                senseNumber = null;
                inHeader = true;
                inIdioms = false;
                continue;
            }

            if (chunk.isTranslation() || Chunk.SENSE_NUMBER.equals(chunk.tag())) {
                inHeader = false;
            }

            if (Chunk.SYMBOL.equals(chunk.tag()) && chunk.text().contains(IDIOM_MARKER)) {
                // Дальше до конца статьи идут устойчивые обороты. Их переводы размечены
                // пустой пометой, а не RV, поэтому в kw они не попадают.
                flushExample(serbian, russian, rawHeadword, inIdioms ? body.idioms : examples);
                flushTranslation(translationBuffer, translations);
                inIdioms = true;
                continue;
            }

            if (!inIdioms && Chunk.SENSE_NUMBER.equals(chunk.tag()) && isSenseNumber(chunks, i)) {
                flushExample(serbian, russian, rawHeadword, examples);
                flushTranslation(translationBuffer, translations);
                if (!translations.isEmpty() || !examples.isEmpty()) {
                    body.senses.add(new Entry.Sense(senseNumber, List.copyOf(translations), List.copyOf(examples)));
                    translations.clear();
                    examples.clear();
                }
                senseNumber = parseNumber(chunk.text());
                continue;
            }

            if (chunk.isSerbian()) {
                if (inHeader) {
                    // Вариант написания заглавного слова, а не пример.
                    continue;
                }
                // Началась сербская фраза — значит, предыдущий пример закончен.
                if (russian.length() > 0) {
                    flushExample(serbian, russian, rawHeadword, inIdioms ? body.idioms : examples);
                }
                flushTranslation(translationBuffer, translations);
                append(serbian, chunk.text());
                continue;
            }

            if (chunk.isTranslation()) {
                append(translationBuffer, chunk.text());
                continue;
            }

            if (chunk.isRussianPlain()) {
                // Пустая помета — это либо перевод примера, либо грамматическая помета
                // и римская цифра омонима. Переводом примера считаем только то,
                // что идёт после сербской фразы и вне шапки статьи.
                if (!inHeader && serbian.length() > 0) {
                    append(russian, chunk.text());
                }
                continue;
            }

            // Знак препинания: разделяет переводы и примеры.
            if (chunk.isSeparator()) {
                String punctuation = chunk.text();
                if (punctuation.startsWith(";")) {
                    flushExample(serbian, russian, rawHeadword, inIdioms ? body.idioms : examples);
                    flushTranslation(translationBuffer, translations);
                } else if (punctuation.startsWith(",") && translationBuffer.length() > 0) {
                    // Запятая разрывает перевод не всегда. Разрешает это поле kw:
                    // если накопленное уже есть в kw — перевод закончен, иначе продолжаем.
                    if (keywordSet.contains(translationBuffer.toString())) {
                        flushTranslation(translationBuffer, translations);
                    } else {
                        translationBuffer.append(',');
                    }
                } else if (russian.length() > 0) {
                    // Знак препинания внутри перевода примера. Без него теряются запятые
                    // и точки сокращений: «кого’-л.» превращается в «кого’-л».
                    russian.append(punctuation);
                }
            }
        }

        flushExample(serbian, russian, rawHeadword, inIdioms ? body.idioms : examples);
        flushTranslation(translationBuffer, translations);
        if (!translations.isEmpty() || !examples.isEmpty()) {
            body.senses.add(new Entry.Sense(senseNumber, List.copyOf(translations), List.copyOf(examples)));
        }
        return body;
    }

    /**
     * Номер значения — это фрагмент {@code D}, за которым идёт закрывающая скобка.
     * Если дальше стоит точка, это верхний уровень в статьях-предлогах (падеж),
     * а не значение.
     */
    private static boolean isSenseNumber(List<Chunk> chunks, int index) {
        if (index + 1 >= chunks.size()) {
            return false;
        }
        Chunk next = chunks.get(index + 1);
        return !next.hasTag() && next.text().startsWith(")");
    }

    /** Номер значения из текста фрагмента. {@code null}, если это не число. */
    private static Integer parseNumber(String text) {
        String digits = text.trim();
        if (digits.isEmpty() || !digits.chars().allMatch(Character::isDigit)) {
            return null;
        }
        try {
            return Integer.valueOf(digits);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static void flushTranslation(StringBuilder buffer, List<String> target) {
        String value = buffer.toString().trim();
        if (!value.isEmpty()) {
            target.add(Russian.renderStress(value));
        }
        buffer.setLength(0);
    }

    private static void flushExample(StringBuilder serbian, StringBuilder russian,
                                     String rawHeadword, List<Entry.Example> target) {
        String left = serbian.toString().trim();
        String right = russian.toString().trim();
        if (!left.isEmpty() && !right.isEmpty()) {
            target.add(new Entry.Example(
                    Serbian.renderAccents(Serbian.expandTilde(left, rawHeadword)),
                    Russian.renderStress(right)));
        }
        serbian.setLength(0);
        russian.setLength(0);
    }

    private static void append(StringBuilder buffer, String text) {
        if (text.isEmpty()) {
            return;
        }
        if (buffer.length() > 0 && !text.startsWith("-") && !text.startsWith(",")) {
            buffer.append(' ');
        }
        buffer.append(text);
    }

    /**
     * Грамматические пометы — фрагменты с пустой пометой, стоящие до первого перевода
     * или номера значения. Римские цифры омонимов сюда не попадают: они не пометы.
     */
    public static List<String> collectMarks(List<Chunk> chunks) {
        // Набор, а не список: у слова с несколькими вариантами написания одна и та же
        // помета повторяется при каждом варианте (го‛ра ж., го“ра ж.).
        Set<String> marks = new LinkedHashSet<>();
        for (int i = 1; i < chunks.size(); i++) {
            Chunk chunk = chunks.get(i);
            if (chunk.isTranslation() || Chunk.SENSE_NUMBER.equals(chunk.tag())) {
                break;
            }
            if (Chunk.RUSSIAN_PLAIN.equals(chunk.tag()) && PartOfSpeechRules.isMark(chunk.text())
                    && !isCompoundAbbreviation(chunks, i)) {
                marks.add(chunk.text());
            }
        }
        return List.copyOf(marks);
    }

    /**
     * Часть ли это составного сокращения вида {@code с.-х.} (сельскохозяйственный).
     *
     * <p>Без этой проверки буква {@code с} из {@code с.-х.} читается как помета
     * среднего рода, и глагол {@code це’пити} объявляется существительным.
     * Признак составного сокращения — идущий следом дефис после точки.
     */
    private static boolean isCompoundAbbreviation(List<Chunk> chunks, int index) {
        return index + 2 < chunks.size()
                && !chunks.get(index + 1).hasTag() && chunks.get(index + 1).text().startsWith(".")
                && !chunks.get(index + 2).hasTag() && chunks.get(index + 2).text().startsWith("-");
    }

    private static List<String> splitKeywords(String kw) {
        if (kw == null || kw.isBlank()) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        for (String part : kw.split("[;|]")) {
            String value = part.trim();
            if (!value.isEmpty()) {
                result.add(value);
            }
        }
        return result;
    }

    private static Entry empty(String name) {
        return new Entry(name, name, name, List.of(), PartOfSpeech.UNKNOWN,
                List.of(new Entry.Sense(null, List.of(), List.of())), List.of(),
                WordStatus.NO_TRANSLATION);
    }
}
