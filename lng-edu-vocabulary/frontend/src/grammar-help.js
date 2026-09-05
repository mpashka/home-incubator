import { ref } from 'vue'

export const grammarHelpOpen = ref(false)
export const grammarTopic = ref('nouns')

export function openGrammarHelp (topic = 'nouns') {
  grammarTopic.value = topic
  grammarHelpOpen.value = true
}
