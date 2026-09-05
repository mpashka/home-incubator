// Слой доступа к данным словаря. Здесь же — подменные данные для работы без бэкенда.
//
// Наружу торчат только `searchWords` / `loadWord` и признак `WordNotFound`;
// страницы не знают, откуда пришёл ответ — с сервера или из заготовки.
//
// @tag:accent — заглавные слова в заготовках записаны кириллицей с комбинируемыми
// знаками ударения (U+0300, U+0301, U+030F, U+0311) и заударной долготой (U+0304).

/** Работать на заготовках: `VITE_MOCK=1 npm run dev`. */
const USE_MOCK = import.meta.env.VITE_MOCK === '1'

/** Слова нет в словаре — бэкенд ответил 404. */
export class WordNotFound extends Error {
  constructor (name) {
    super(`Слово «${name}» в словаре не найдено`)
    this.name = 'WordNotFound'
    this.word = name
  }
}

// ---------------------------------------------------------------------------
// Обращения к бэкенду
// ---------------------------------------------------------------------------

/**
 * Поиск слов по запросу.
 * @param {string} query строка поиска
 * @param {number} limit сколько записей вернуть
 * @param {AbortSignal} [signal] отмена устаревшего запроса
 * @returns {Promise<{items: Array, total: number}>}
 */
export async function searchWords (query, limit = 20, signal, alphabet = 'current') {
  const cleaned = (query ?? '').trim()
  if (!cleaned) return { items: [], total: 0 }

  if (USE_MOCK) return mockSearch(cleaned, limit)

  const url = `/api/words?q=${encodeURIComponent(cleaned)}&limit=${limit}&alphabet=${alphabet}`
  const response = await fetch(url, { signal, headers: { Accept: 'application/json' } })
  if (!response.ok) throw new Error(`Сервер ответил ошибкой ${response.status}`)
  const data = await response.json()
  // Бэкенд может отдать неполный ответ — приводим к предсказуемому виду.
  return {
    items: Array.isArray(data?.items) ? data.items : [],
    total: Number.isFinite(data?.total) ? data.total : 0
  }
}

/**
 * Полная карточка слова.
 * @param {string} name латиница без ударений — поле `name`
 * @param {AbortSignal} [signal] отмена устаревшего запроса
 * @returns {Promise<Object>}
 * @throws {WordNotFound} если слова в словаре нет
 */
export async function loadWord (name, signal) {
  if (USE_MOCK) return mockWord(name)

  const response = await fetch(`/api/words/${encodeURIComponent(name)}`, {
    signal,
    headers: { Accept: 'application/json' }
  })
  if (response.status === 404) throw new WordNotFound(name)
  if (!response.ok) throw new Error(`Сервер ответил ошибкой ${response.status}`)
  return await response.json()
}

// ---------------------------------------------------------------------------
// Подменные данные (только при VITE_MOCK=1)
// ---------------------------------------------------------------------------

// Знаки ударения записаны escape-последовательностями, а не готовыми символами:
// иначе их не видно при чтении кода и легко потерять при правке.
const GRAVE = '̀' //  ̀ краткое восходящее
const ACUTE = '́' //  ́ долгое восходящее
const DBL_GRAVE = '̏' //  ̏ краткое нисходящее
const INV_BREVE = '̑' //  ̑ долгое нисходящее
const MACRON = '̄' //  ̄ заударная долгота

// В латинской записи долгое нисходящее принято ставить «домиком» (U+0302),
// а не перевёрнутой дужкой, как в кириллице: ру̑ка → rûka.
const CIRCUMFLEX = '̂'

const MOCK_WORDS = {
  voda: {
    name: 'voda',
    headword: `во${GRAVE}да`,
    headwordLatin: `vo${GRAVE}da`,
    grammar: ['ж.'],
    partOfSpeech: 'NOUN',
    senses: [
      {
        number: 1,
        translations: ['вода́'],
        examples: [
          { serbian: `сла${ACUTE}тка во${GRAVE}да`, russian: 'пресная вода' },
          { serbian: `пи${INV_BREVE}ти во${GRAVE}ду`, russian: 'пить воду' },
          // Заударная долгота на последнем слоге: во̀де̄
          { serbian: `ча${INV_BREVE}ша во${GRAVE}де${MACRON}`, russian: 'стакан воды' }
        ]
      },
      {
        number: 2,
        translations: ['река́', 'водоём'],
        examples: [
          { serbian: `пло${DBL_GRAVE}вна во${GRAVE}да`, russian: 'судоходная река' }
        ]
      },
      {
        number: 3,
        translations: ['минера́льная вода́'],
        examples: []
      }
    ],
    idioms: [
      { serbian: `ба${ACUTE}цати дрва на ва${INV_BREVE}тру`, russian: 'подливать масла в огонь' },
      { serbian: `ти${INV_BREVE}ха во${GRAVE}да бре${INV_BREVE}г ро${GRAVE}нӣ`, russian: 'в тихом омуте черти водятся' },
      { serbian: `му${ACUTE}тити во${GRAVE}ду`, russian: 'мутить воду' }
    ],
    status: 'IMPORTED'
  },

  ruka: {
    name: 'ruka',
    headword: `ру${INV_BREVE}ка`,
    headwordLatin: `ru${CIRCUMFLEX}ka`,
    grammar: ['ж.', `мн. ру${INV_BREVE}ке${MACRON}`],
    partOfSpeech: 'NOUN',
    senses: [
      {
        number: 1,
        translations: ['рука́'],
        examples: [
          { serbian: `де${ACUTE}сна ру${INV_BREVE}ка`, russian: 'правая рука' },
          { serbian: `пру${ACUTE}жити ру${INV_BREVE}ку`, russian: 'протянуть руку' }
        ]
      },
      {
        number: 2,
        translations: ['по́черк'],
        examples: [
          { serbian: `чи${ACUTE}тка ру${INV_BREVE}ка`, russian: 'разборчивый почерк' }
        ]
      },
      {
        number: 3,
        translations: ['сторона́', 'направле́ние'],
        examples: [
          { serbian: `с ле${ACUTE}ве${MACRON} ру${INV_BREVE}ке${MACRON}`, russian: 'с левой стороны' }
        ]
      }
    ],
    idioms: [
      { serbian: `и${ACUTE}ћи на ру${INV_BREVE}ку`, russian: 'идти навстречу' },
      { serbian: `из пр${DBL_GRAVE}ве${MACRON} ру${INV_BREVE}ке${MACRON}`, russian: 'из первых рук' }
    ],
    status: 'COMPLETE'
  },

  // Намеренно бедная запись: проверяем, что интерфейс переживает пустые поля.
  vodopad: {
    name: 'vodopad',
    headword: `во${GRAVE}допа${MACRON}д`,
    headwordLatin: `vo${GRAVE}dopa${MACRON}d`,
    grammar: null,
    partOfSpeech: null,
    senses: [
      { number: null, translations: ['водопа́д'], examples: null }
    ],
    idioms: null,
    status: 'NO_TRANSLATION'
  }
}

/** Убирает комбинируемые знаки — чтобы искать по заготовкам без оглядки на ударение. */
function stripAccents (text) {
  return text.normalize('NFD').replace(/[̀-ͯ]/g, '').toLowerCase()
}

function mockSearch (query, limit) {
  const pattern = stripAccents(query)
  const found = Object.values(MOCK_WORDS).filter(word =>
    word.name.startsWith(pattern) ||
    stripAccents(word.headword).startsWith(pattern) ||
    (word.senses ?? []).some(sense =>
      (sense.translations ?? []).some(t => stripAccents(t).startsWith(pattern)))
  )
  const brief = found.map(word => ({
    name: word.name,
    headword: word.headword,
    translations: (word.senses ?? []).flatMap(sense => sense.translations ?? [])
  }))
  return delayed({ items: brief.slice(0, limit), total: brief.length })
}

function mockWord (name) {
  const word = MOCK_WORDS[name]
  if (!word) return delayed(Promise.reject(new WordNotFound(name)))
  return delayed(word)
}

/** Небольшая задержка — чтобы состояние ожидания было видно и на заготовках. */
function delayed (value, ms = 180) {
  return new Promise((resolve, reject) => {
    setTimeout(() => Promise.resolve(value).then(resolve, reject), ms)
  })
}
