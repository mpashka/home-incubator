import { createRouter, createWebHistory } from 'vue-router'
import SearchPage from './pages/SearchPage.vue'
import WordPage from './pages/WordPage.vue'

// Две страницы: поиск и карточка слова.
const routes = [
  { path: '/', name: 'search', component: SearchPage },
  { path: '/word/:name', name: 'word', component: WordPage, props: true }
]

export const router = createRouter({
  history: createWebHistory(),
  routes
})
