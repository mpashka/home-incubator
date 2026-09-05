<script setup>
// Страница поиска: поле ввода и список найденного.
// Ищем по мере набора с задержкой ~250 мс; устаревшие запросы отменяем.
import { ref, watch, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter, RouterLink } from 'vue-router'
import { searchWords } from '../api/dictionary.js'
import { searchAlphabet } from '../settings.js'
import Accented from '../components/Accented.vue'
import Notice from '../components/Notice.vue'

const DEBOUNCE_MS = 250
const LIMIT = 20

const route = useRoute()
const router = useRouter()

const query = ref(typeof route.query.q === 'string' ? route.query.q : '')
const items = ref([])
const total = ref(0)
/** Состояние: 'empty' — запроса нет, 'loading', 'ready', 'error'. */
const state = ref('empty')
const errorText = ref('')
const field = ref(null)

let timer = null
let controller = null

/** Запускает поиск, отменив предыдущий незавершённый запрос. */
async function run (text) {
  controller?.abort()

  if (!text.trim()) {
    controller = null
    items.value = []
    total.value = 0
    state.value = 'empty'
    return
  }

  controller = new AbortController()
  const own = controller
  state.value = 'loading'
  errorText.value = ''

  try {
    const result = await searchWords(text, LIMIT, own.signal, searchAlphabet.value)
    // Ответ мог прийти после того, как запрос сменился — такой отбрасываем.
    if (own !== controller) return
    items.value = result.items
    total.value = result.total
    state.value = 'ready'
  } catch (error) {
    if (own !== controller || error?.name === 'AbortError') return
    items.value = []
    total.value = 0
    errorText.value = 'Не удалось связаться со словарём. Проверьте, запущен ли сервер.'
    state.value = 'error'
  }
}

/** Откладывает поиск, пока пользователь набирает. */
function schedule (text) {
  clearTimeout(timer)
  timer = setTimeout(() => run(text), DEBOUNCE_MS)
}

watch(query, text => {
  // Держим запрос в адресе страницы — тогда возврат с карточки слова
  // возвращает и набранное, и найденное.
  router.replace({ name: 'search', query: text ? { q: text } : {} })
  schedule(text)
})

watch(searchAlphabet, () => query.value && run(query.value))

onMounted(() => {
  field.value?.focus()
  if (query.value) run(query.value)
})

onBeforeUnmount(() => {
  clearTimeout(timer)
  controller?.abort()
})
</script>

<template>
  <section>
    <input
      ref="field"
      v-model="query"
      class="search-field"
      type="search"
      autocomplete="off"
      spellcheck="false"
      placeholder="Наберите слово — по-сербски или по-русски"
      aria-label="Поиск слова"
    />
    <p class="search-hint">
      Ударения набирать не нужно. Правило поиска выбирается в настройках.
    </p>

    <Notice v-if="state === 'empty'" text="Начните набирать слово." />

    <Notice v-else-if="state === 'loading'" text="Ищем…" />

    <Notice
      v-else-if="state === 'error'"
      kind="error"
      :text="errorText"
      retryable
      @retry="run(query)"
    />

    <Notice
      v-else-if="items.length === 0"
      :text="`По запросу «${query.trim()}» ничего не нашлось.`"
    />

    <template v-else>
      <ul class="results">
        <li v-for="word in items" :key="word.name">
          <!-- `from` — набранный запрос: по нему ссылка «к поиску» вернёт найденное. -->
          <RouterLink
            class="result-link"
            :to="{ name: 'word', params: { name: word.name }, query: { from: query.trim() } }"
          >
            <Accented class="result-headword" :text="word.headword || word.name" />
            <span class="result-translations">
              {{ (word.translations ?? []).join(', ') }}
            </span>
          </RouterLink>
        </li>
      </ul>
      <p v-if="total > items.length" class="results-total">
        Показано {{ items.length }} из {{ total }} — уточните запрос.
      </p>
    </template>
  </section>
</template>
