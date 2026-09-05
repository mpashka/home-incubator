<script setup>
// Общая рамка: шапка со ссылкой на поиск и место под текущую страницу.
import { RouterLink, RouterView } from 'vue-router'
import {
  displayAlphabet, searchAlphabet, showExamples, showForms, showIdioms, showRoots, formLabels, theme
} from './settings.js'
import { openGrammarHelp } from './grammar-help.js'
import GrammarHelp from './components/GrammarHelp.vue'
</script>

<template>
  <div class="layout" :class="`theme-${theme}`">
    <header class="site-head">
      <h1><RouterLink to="/">Сербско-русский словарь</RouterLink></h1>
      <span class="subtitle">поиск по слову, ударения, значения</span>
      <details class="settings">
        <summary>Настройки</summary>
        <label>Показывать сербские слова
          <select v-model="displayAlphabet">
            <option value="cyrillic">кириллицей</option>
            <option value="latin">латиницей</option>
          </select>
        </label>
        <label>Тема
          <select v-model="theme">
            <option value="system">как в системе</option>
            <option value="light">светлая</option>
            <option value="dark">тёмная</option>
          </select>
        </label>
        <label>Поиск по алфавиту ввода
          <select v-model="searchAlphabet">
            <option value="separate">кириллица — русский, латиница — сербский</option>
            <option value="any">искать и русский, и сербский</option>
            <option value="current">как сейчас: кириллица — оба, латиница — сербский</option>
          </select>
        </label>
        <label><input v-model="showForms" type="checkbox" true-value="true" false-value="false"> Словоформы</label>
        <label v-if="showForms === 'true'">Подписи словоформ
          <select v-model="formLabels">
            <option value="serbian">названия по-сербски</option>
            <option value="code">грамматические коды</option>
          </select>
        </label>
      </details>
      <button class="grammar-button" type="button" @click="openGrammarHelp()">Грамматика</button>
    </header>
    <nav class="quick-toggles" aria-label="Разделы карточки">
      <button type="button" :class="{ active: showExamples === 'true' }" :aria-pressed="showExamples === 'true'" @click="showExamples = showExamples === 'true' ? 'false' : 'true'">Примеры</button>
      <button type="button" :class="{ active: showIdioms === 'true' }" :aria-pressed="showIdioms === 'true'" @click="showIdioms = showIdioms === 'true' ? 'false' : 'true'">Устойчивые обороты</button>
      <button type="button" :class="{ active: showRoots === 'true' }" :aria-pressed="showRoots === 'true'" @click="showRoots = showRoots === 'true' ? 'false' : 'true'">Связь с корнями</button>
    </nav>
    <RouterView />
    <GrammarHelp />
  </div>
</template>
