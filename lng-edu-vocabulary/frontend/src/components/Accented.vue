<script setup>
// Сербский текст с ударениями.
//
// @tag:accent — единственная точка показа заглавных слов, примеров и оборотов.
// Всё сербское выводится через этот компонент, чтобы шрифт и правила отрисовки
// комбинируемых знаков задавались в одном месте (класс `.accented` в styles.css),
// а не расползались по страницам.
import { computed } from 'vue'
import { displayAlphabet } from '../settings.js'
import { toLatin } from '../serbian.js'

const props = defineProps({
  /** Сербский текст; знаки ударения — комбинируемые, отдельными код-точками. */
  text: { type: String, default: '' },
  /** Каким тэгом обернуть: по умолчанию строчный `span`. */
  tag: { type: String, default: 'span' }
})

/*
 * Приводим к нормальной форме NFC. Для кириллицы это почти ничего не меняет —
 * готовых символов «буква со знаком ударения» в Юникоде для неё нет, знаки так
 * и остаются отдельными. Зато для латинской записи (`headwordLatin`) слитные
 * символы вроде «ò» существуют, и с ними отрисовка надёжнее.
 */
const normalized = computed(() => {
  const text = displayAlphabet.value === 'latin' ? toLatin(props.text) : props.text
  return (text ?? '').normalize('NFC')
})
</script>

<template>
  <component :is="tag" class="accented">{{ normalized }}</component>
</template>
