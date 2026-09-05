package org.mpashka.vocabulary.core;

/**
 * Словоформа с грамматической пометой.
 *
 * <p>Помета — внутреннее обозначение указателя, а не помета словаря:
 * {@code nom.sg}, {@code gen.sg}, {@code loc.pl} у существительных,
 * {@code praes.1sg} у глаголов, {@code adj} у форм прилагательного, род которых
 * из статьи не выводится.
 *
 * @param grammar помета формы
 * @param value   сама форма; в указателе — без знаков ударения
 */
// @tag:word-forms
public record Form(String grammar, String value) {
}
