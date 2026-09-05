import { ref } from 'vue'

// Состояние справки по грамматике. У неё два вида:
//   - общий: разделы «Падежи», «Глаголы», «Местоимения», «Прилагательные»;
//   - правило: то самое правило, по которому получена форма в карточке, с парадигмой
//     конкретного слова, примерами и исключениями.
// @tag:word-forms

export const grammarHelpOpen = ref(false)
export const grammarTopic = ref('nouns')

/** Форма, по которой открыли правило, и слово, из карточки которого пришли. */
export const ruleForm = ref(null)
export const ruleWord = ref(null)

/** Общая справка: раздел грамматики целиком. */
export function openGrammarHelp (topic = 'nouns') {
  ruleForm.value = null
  ruleWord.value = null
  grammarTopic.value = topic
  grammarHelpOpen.value = true
}

/**
 * Правило, породившее конкретную форму.
 *
 * @param {Object} form форма из карточки: `grammar`, `rule`, `ruleType`
 * @param {Object} word слово, которому она принадлежит
 */
export function openRule (form, word) {
  ruleForm.value = form
  ruleWord.value = word
  grammarTopic.value = 'rule'
  grammarHelpOpen.value = true
}
