package org.mpashka.vocabulary.core;

import java.util.List;

/**
 * Словарная статья в разобранном виде.
 *
 * @param name          заглавное слово латиницей без ударений — ключ исходной базы
 * @param headword      заглавное слово кириллицей с комбинируемыми знаками ударения
 * @param headwordLatin то же латиницей с комбинируемыми знаками ударения
 * @param grammar       грамматические и стилистические пометы: {@code ж.}, {@code несов.}
 * @param partOfSpeech  часть речи
 * @param senses        значения слова; если нумерации в исходнике не было — одно значение
 * @param idioms        устойчивые обороты из блока после {@code ◊}
 * @param status        состояние обработки
 */
public record Entry(
        String name,
        String headword,
        String headwordLatin,
        List<String> grammar,
        PartOfSpeech partOfSpeech,
        List<Sense> senses,
        List<Example> idioms,
        WordStatus status) {

    /**
     * Значение слова.
     *
     * @param number       номер значения; {@code null}, если в исходнике нумерации не было
     * @param translations переводы на русский
     * @param examples     примеры употребления
     */
    public record Sense(Integer number, List<String> translations, List<Example> examples) {
    }

    /**
     * Пример употребления: сербская фраза и её русский перевод.
     *
     * @param serbian сербская фраза, кириллицей, с раскрытой тильдой
     * @param russian русский перевод
     */
    public record Example(String serbian, String russian) {
    }
}
