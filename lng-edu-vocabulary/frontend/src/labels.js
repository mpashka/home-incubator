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

// --- Пометы словоформ ------------------------------------------------------
// Владелец расшифровки помет вроде `gen.sg`: и список найденного, и карточка,
// и справка по правилу берут названия отсюда, а не заводят свои.
// @tag:word-forms

const CASE_RUSSIAN = {
  nom: 'именительный', gen: 'родительный', dat: 'дательный', acc: 'винительный',
  voc: 'звательный', ins: 'творительный', loc: 'местный'
}

const CASE_SERBIAN = {
  nom: 'Nominativ', gen: 'Genitiv', dat: 'Dativ', acc: 'Akuzativ',
  voc: 'Vokativ', ins: 'Instrumental', loc: 'Lokativ'
}

const NUMBER_RUSSIAN = { sg: 'единственное число', pl: 'множественное число' }
const NUMBER_SERBIAN = { sg: 'jednine', pl: 'množine' }

/** Пометы, которые падежом не описываются. */
const WHOLE = {
  'praes.1sg': ['настоящее время, 1-е лицо единственного числа', 'Prezent'],
  inf: ['инфинитив', 'Infinitiv'],
  adj: ['форма прилагательного', 'Pridev'],
  'nom.sg.вариант': ['вариант ударения заглавного слова', 'Nominativ jednine']
}

/** Падежная часть пометы: `gen.sg` → `gen`; для непадежных помет — пустая строка. */
export function caseKey (grammar) {
  const key = (grammar ?? '').split('.')[0]
  return CASE_RUSSIAN[key] ? key : ''
}

/** Помета по-русски: `gen.sg` → «родительный падеж, единственное число». */
export function formName (grammar) {
  if (!grammar) return ''
  if (WHOLE[grammar]) return WHOLE[grammar][0]
  const [kind, number] = grammar.split('.')
  if (!CASE_RUSSIAN[kind]) return grammar
  const numberName = NUMBER_RUSSIAN[number]
  return `${CASE_RUSSIAN[kind]} падеж${numberName ? ', ' + numberName : ''}`
}

/** Помета по-сербски: `gen.sg` → «Genitiv jednine». Так подписаны формы в учебниках. */
export function formNameSerbian (grammar) {
  if (!grammar) return ''
  if (WHOLE[grammar]) return WHOLE[grammar][1]
  const [kind, number] = grammar.split('.')
  if (!CASE_SERBIAN[kind]) return grammar
  return `${CASE_SERBIAN[kind]}${NUMBER_SERBIAN[number] ? ' ' + NUMBER_SERBIAN[number] : ''}`
}
