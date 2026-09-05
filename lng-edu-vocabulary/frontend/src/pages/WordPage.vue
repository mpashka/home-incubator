<script setup>
// Карточка слова: заглавное слово с ударением, пометы, значения, обороты.
// Все поля кроме `name` и `headword` могут прийти пустыми или отсутствовать —
// каждое место показа это учитывает.
import { ref, computed, watch, onBeforeUnmount } from 'vue'
import { useRoute, RouterLink } from 'vue-router'
import { loadWord, loadWordById, WordNotFound } from '../api/dictionary.js'
import {
  partOfSpeechName, statusName, statusKey, statusNotable, formName, formNameSerbian,
  formLabel as labelOf
} from '../labels.js'
import Accented from '../components/Accented.vue'
import SerbianText from '../components/SerbianText.vue'
import Notice from '../components/Notice.vue'
import { formLabels, showExamples, showForms, showIdioms, showRoots } from '../settings.js'
import { openRule } from '../grammar-help.js'

const route = useRoute()

/** Ответ словаря: `{ matchedBy, words }`. */
const found = ref(null)
/** Состояние: 'loading', 'ready', 'missing' — слова нет, 'error' — сбой связи. */
const state = ref('loading')

let controller = null

/**
 * Загружает статью. Есть `id` — открываем ровно это слово; нет — ищем по написанию,
 * и тогда словарь вправе найти слово, у которого это написание лишь словоформа.
 */
async function load (name, id) {
  controller?.abort()
  controller = new AbortController()
  const own = controller
  state.value = 'loading'
  found.value = null

  try {
    const result = id ? await loadWordById(id, own.signal) : await loadWord(name, own.signal)
    if (own !== controller) return
    found.value = result
    state.value = 'ready'
  } catch (error) {
    if (own !== controller || error?.name === 'AbortError') return
    state.value = error instanceof WordNotFound ? 'missing' : 'error'
  }
}

watch(() => [route.params.name, route.query.id], ([name, id]) => {
  if (typeof name === 'string' && name) load(name, id)
}, { immediate: true })

onBeforeUnmount(() => controller?.abort())

// Под одним написанием бывает несколько самостоятельных слов (бити — «быть» и «бить»).
// Показываем все, иначе половина словаря окажется скрытой.
const homonyms = computed(() => found.value?.words ?? [])
/** Нашлось не по написанию, а по словоформе: в списке разные слова, а не омонимы. */
const foundByForm = computed(() => found.value?.matchedBy === 'form')
const manyHomonyms = computed(() => !foundByForm.value && homonyms.value.length > 1)
const roman = ['I', 'II', 'III', 'IV', 'V', 'VI']
/** Заголовок: написание, которое спросили; при точном попадании — его ударная запись. */
const headText = computed(() => foundByForm.value
  ? route.params.name
  : homonyms.value[0]?.headword || route.params.name)


/** Номера показываем, только когда значений больше одного. */

/** Устойчивые обороты; блок целиком прячется, если их нет. */

/** Грамматические пометы одной строкой. */
// Пометы, часть речи и состояние — свои у каждого омонима.
const grammarOf = (w) => (w?.grammar ?? []).filter(Boolean)
const grammarHint = { м: 'мужской род', ж: 'женский род', с: 'средний род', мн: 'множественное число' }
const grammarTitle = (mark) => grammarHint[mark] ?? 'Грамматическая помета'

const posName = (w) => partOfSpeechName(w?.partOfSpeech)
// Пометка состояния показывается, только когда несёт сведения: «импортировано» стоит
// почти у всех статей и не сообщает ничего.
const statusOf = (w) => (statusNotable(w?.status) ? statusName(w?.status) : '')
const statusClassOf = (w) => statusKey(w?.status)

/** Подпись формы в выбранном виде: по-русски, по-сербски, и так и так, либо кодом. */
const formLabel = (grammar) => labelOf(grammar, formLabels.value)
const caseExample = {
  nom: 'Это …', gen: 'Нет …', dat: 'Помогаю …', acc: 'Люблю …',
  voc: 'Эй, …!', ins: 'Играю с …', loc: 'Говорю о …'
}
const serbianPhrase = (form) => {
  const [caseName, number] = (form.grammar ?? '').split('.')
  const prefix = {
    nom: number === 'pl' ? 'Ovo su' : 'Ovo je', gen: 'Nema', dat: 'Prilazim',
    acc: 'Vidim', voc: 'Hej', ins: 'Igram se sa', loc: 'Govorim o'
  }[caseName]
  return prefix ? `${prefix}${caseName === 'voc' ? ',' : ''} ${form.form}.` : ''
}
const rootNames = {
  RUSSIAN: 'Русский корень', SERBIAN: 'Сербский корень', PROTO_SLAVIC: 'Праславянский корень',
  PROTO_INDO_EUROPEAN: 'Праиндоевропейский корень', OTHER: 'Другой родственный корень'
}

/** Возврат к поиску вместе с тем запросом, с которого пришли (параметр `from`). */
const backToSearch = computed(() => {
  const from = route.query.from
  return { name: 'search', query: typeof from === 'string' && from ? { q: from } : {} }
})
</script>

<template>
  <section>
    <RouterLink class="back-link" :to="backToSearch">← к поиску</RouterLink>

    <Notice v-if="state === 'loading'" text="Загружаем статью…" />

    <Notice
      v-else-if="state === 'missing'"
      :text="`Слова «${route.params.name}» в словаре нет.`"
    />

    <Notice
      v-else-if="state === 'error'"
      kind="error"
      text="Не удалось загрузить статью. Проверьте, запущен ли сервер."
      retryable
      @retry="load(route.params.name)"
    />

    <template v-else-if="homonyms.length">
      <header class="word-head">
        <Accented tag="h2" class="word-headword" :text="headText" />
      </header>

      <p v-if="foundByForm" class="found-by-form">
        Заглавного слова «{{ route.params.name }}» в словаре нет.
        {{ homonyms.length > 1 ? 'Так выглядят формы других слов:' : 'Так выглядит форма другого слова:' }}
      </p>

      <section
        v-for="(homonym, hIndex) in homonyms"
        :key="hIndex"
        class="homonym"
      >
        <h3 v-if="manyHomonyms" class="homonym-title">
          {{ roman[hIndex] ?? hIndex + 1 }}
        </h3>

        <!-- Найдено по форме: у каждого слова своё написание, и надо сказать, чья это форма. -->
        <p v-if="foundByForm" class="found-by-form-word">
          <RouterLink :to="{ name: 'word', params: { name: homonym.name }, query: { id: homonym.id } }">
            <Accented :text="homonym.headword || homonym.name" />
          </RouterLink>
          <span v-if="homonym.matchedForm">
            — «{{ homonym.matchedForm.value }}» у него
            {{ formName(homonym.matchedForm.grammar) || formNameSerbian(homonym.matchedForm.grammar) }}
          </span>
        </p>

        <div class="word-marks">
          <span v-for="mark in grammarOf(homonym)" :key="mark" class="word-grammar" :title="grammarTitle(mark)">
            {{ mark }}
          </span>
          <span v-if="posName(homonym)" class="word-pos" title="Часть речи">{{ posName(homonym) }}</span>
          <span v-if="statusOf(homonym)" class="status-mark" :class="statusClassOf(homonym)" title="Состояние статьи">
            {{ statusOf(homonym) }}
          </span>
        </div>

        <!-- Предупреждение висит, только пока есть что делать: языковой пробел
             остался у 356 статей из 47 019. -->
        <p v-if="homonym.needsLanguageReview" class="review-warning">
          В этой статье осталось незакрытое: {{ homonym.reviewReason }}.
          Показанное берите с оговоркой — пробел закрывается внешним источником.
        </p>

        <ol v-if="homonym.senses?.length" class="senses">
          <li v-for="(sense, index) in homonym.senses" :key="index" class="sense">
            <span v-if="homonym.senses.length > 1" class="sense-number">
              {{ sense.number ?? index + 1 }}.
            </span>
            <div class="sense-body">
              <p class="sense-translations">
                {{ (sense.translations ?? []).join(', ') || '—' }}
              </p>
              <ul v-if="showExamples === 'true' && sense.examples?.length" class="examples">
                <li
                  v-for="(example, order) in sense.examples"
                  :key="order"
                  class="example"
                >
                  <SerbianText class="example-serbian" :text="example.serbian" :exclude-name="homonym.name" />
                  <span class="example-russian">{{ example.russian }}</span>
                </li>
              </ul>
            </div>
          </li>
        </ol>

        <Notice v-else text="У этой статьи пока нет разобранных значений." />

        <section v-if="showForms === 'true' && homonym.forms?.length">
          <h4 class="idioms-title">Словоформы</h4>
          <ul class="forms">
            <li v-for="(form, index) in homonym.forms" :key="index">
              <span class="form-label">{{ formLabel(form.grammar) }}</span>
              <Accented :text="form.form" />
              <small v-if="form.grammar" class="form-example">{{ caseExample[form.grammar.split('.')[0]] }}</small>
              <Accented v-if="serbianPhrase(form)" class="form-serbian-example" :text="serbianPhrase(form)" />
              <button
                v-if="form.rule"
                class="form-source"
                type="button"
                title="Показать правило целиком: окончания, примеры, исключения"
                @click="openRule(form, homonym)"
              >по правилу</button>
            </li>
          </ul>
        </section>

        <section v-if="showIdioms === 'true' && homonym.idioms?.length">
          <h4 class="idioms-title">Устойчивые обороты</h4>
          <ul class="idioms">
            <li v-for="(idiom, index) in homonym.idioms" :key="index" class="idiom">
              <SerbianText class="idiom-serbian" :text="idiom.serbian" :exclude-name="homonym.name" />
              <span class="idiom-russian">{{ idiom.russian }}</span>
            </li>
          </ul>
        </section>

        <section v-if="showRoots === 'true' && homonym.roots?.length">
          <h4 class="idioms-title">Связь с корнями</h4>
          <ul class="roots">
            <li v-for="(root, index) in homonym.roots" :key="index">
              <span class="root-kind">{{ rootNames[root.kind] ?? root.kind }}</span>
              <span>{{ root.value }}</span>
              <small v-if="root.note">{{ root.note }}</small>
            </li>
          </ul>
        </section>
      </section>

    </template>
  </section>
</template>
