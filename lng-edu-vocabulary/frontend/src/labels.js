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

const NUMBER_SHORT = { sg: 'ед. ч.', pl: 'мн. ч.' }

/** Помета по-русски коротко: `gen.sg` → «родительный, ед. ч.» — для подписей и таблиц. */
export function formNameShort (grammar) {
  if (!grammar) return ''
  if (WHOLE[grammar]) return WHOLE[grammar][0]
  const [kind, number] = grammar.split('.')
  if (!CASE_RUSSIAN[kind]) return grammar
  return `${CASE_RUSSIAN[kind]}${NUMBER_SHORT[number] ? ', ' + NUMBER_SHORT[number] : ''}`
}

/**
 * Подпись формы в выбранном виде. Владелец всех подписей: карточка и справка по
 * правилу подписывают падежи одинаково, иначе одно и то же место называлось бы
 * в двух местах по-разному.
 *
 * @param {string} grammar помета вроде `gen.sg`
 * @param {string} mode `both` — по-сербски и по-русски, `russian`, `serbian`, `code`
 */
export function formLabel (grammar, mode = 'both') {
  if (!grammar) return 'форма'
  switch (mode) {
    case 'code': return grammar
    case 'russian': return formNameShort(grammar)
    case 'serbian': return formNameSerbian(grammar)
    default: {
      const serbian = formNameSerbian(grammar)
      const russian = formNameShort(grammar)
      return serbian === russian ? serbian : `${serbian} · ${russian}`
    }
  }
}

/** Подпись одного падежа, без числа: для первого столбца таблицы склонения. */
export function caseLabel (key, mode = 'both') {
  const serbian = CASE_SERBIAN[key] ?? key
  const russian = CASE_RUSSIAN[key] ?? key
  switch (mode) {
    case 'code': return key
    case 'russian': return russian
    case 'serbian': return serbian
    default: return `${serbian} · ${russian}`
  }
}
