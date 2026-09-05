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
export const showForms = persistent('vocabulary.showForms', 'true')
export const showExamples = persistent('vocabulary.showExamples', 'true')
export const showIdioms = persistent('vocabulary.showIdioms', 'true')
export const showRoots = persistent('vocabulary.showRoots', 'true')
export const formLabels = persistent('vocabulary.formLabels', 'serbian')
export const theme = persistent('vocabulary.theme', 'system')
