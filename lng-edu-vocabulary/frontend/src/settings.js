import { ref, watch } from 'vue'

const saved = (key, fallback) => localStorage.getItem(key) ?? fallback
const persistent = (key, fallback) => {
  const value = ref(saved(key, fallback))
  watch(value, next => localStorage.setItem(key, next))
  return value
}

/** Как показывать сербский текст: кириллицей или латиницей. */
export const displayAlphabet = persistent('vocabulary.displayAlphabet', 'cyrillic')
/** Правило выбора сербского или русского поиска по алфавиту ввода. */
export const searchAlphabet = persistent('vocabulary.searchAlphabet', 'current')
/**
 * Искать ли по словоформам. Выключенный поиск ищет только по заглавному слову
 * и переводу: «вода» тогда не приводит к «во̏д», у которого это родительный падеж.
 */
export const searchForms = persistent('vocabulary.searchForms', 'true')
export const showForms = persistent('vocabulary.showForms', 'true')
export const showExamples = persistent('vocabulary.showExamples', 'true')
export const showIdioms = persistent('vocabulary.showIdioms', 'true')
export const showRoots = persistent('vocabulary.showRoots', 'true')
export const formLabels = persistent('vocabulary.formLabels', 'serbian')
export const theme = persistent('vocabulary.theme', 'system')

// Разделы справки по правилу — своя панель, как у карточки.
export const ruleParadigm = persistent('vocabulary.ruleParadigm', 'true')
export const ruleExamples = persistent('vocabulary.ruleExamples', 'true')
export const ruleExceptions = persistent('vocabulary.ruleExceptions', 'true')
/** Разбор именно того слова, из карточки которого открыли правило. */
export const ruleWordBreakdown = persistent('vocabulary.ruleWordBreakdown', 'true')
