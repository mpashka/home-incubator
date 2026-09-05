import { createRouter, createWebHistory } from 'vue-router'
import SearchPage from './pages/SearchPage.vue'
import WordPage from './pages/WordPage.vue'

// Две страницы: поиск и карточка слова.
//
// Адрес статьи остаётся читаемым (`/word/вода`), а переход по щелчку добавляет
// `?id=` — тогда открывается ровно то слово, по которому щёлкнули, а не всё, что
// словарь может найти по этому написанию.
const routes = [
  { path: '/', name: 'search', component: SearchPage },
  { path: '/word/:name', name: 'word', component: WordPage, props: true }
]

export const router = createRouter({
  history: createWebHistory(),
  routes
})
