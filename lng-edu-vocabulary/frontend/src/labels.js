// Русские названия для кодов, приходящих с бэкенда.

const PART_OF_SPEECH = {
  NOUN: 'существительное',
  VERB: 'глагол',
  ADJECTIVE: 'прилагательное',
  ADVERB: 'наречие',
  PRONOUN: 'местоимение',
  NUMERAL: 'числительное',
  INTERJECTION: 'междометие',
  CONJUNCTION: 'союз',
  PREPOSITION: 'предлог',
  PARTICLE: 'частица',
  UNKNOWN: ''
}

const STATUS = {
  NO_TRANSLATION: 'нет перевода',
  IMPORTED: 'импортировано',
  COMPLETE: 'полностью обработано'
}

/** Часть речи по-русски; пустая строка, если код неизвестен или его нет. */
export function partOfSpeechName (code) {
  if (!code) return ''
  return PART_OF_SPEECH[code] ?? ''
}

/** Состояние обработки статьи по-русски. */
export function statusName (code) {
  if (!code) return ''
  return STATUS[code] ?? ''
}

/** Ключ оформления пометки о состоянии — для скромной подсветки. */
export function statusKey (code) {
  return STATUS[code] ? code.toLowerCase().replace(/_/g, '-') : ''
}
