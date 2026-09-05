<script setup>
import { computed } from 'vue'
import { RouterLink } from 'vue-router'
import Accented from './Accented.vue'

const props = defineProps({
  text: { type: String, default: '' },
  /** Ключ открытой статьи: на него ссылку не ставим. */
  excludeName: { type: String, default: '' }
})
const wordPattern = /^[\p{Script=Cyrillic}\p{M}]+$/u
const parts = computed(() => (props.text ?? '').split(/([\p{Script=Cyrillic}\p{M}]+)/gu).filter(Boolean))
const nameOf = (word) => word.normalize('NFD').replace(/\p{M}/gu, '').toLowerCase()
</script>

<template>
  <template v-for="(part, index) in parts" :key="index">
    <RouterLink v-if="wordPattern.test(part) && nameOf(part) !== excludeName" class="serbian-link" :to="{ name: 'word', params: { name: nameOf(part) } }">
      <Accented :text="part" />
    </RouterLink>
    <Accented v-else :text="part" />
  </template>
</template>
