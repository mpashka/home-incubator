<script setup>
// Короткое сообщение вместо содержимого: ожидание, «ничего не найдено», ошибка.
// При ошибке можно показать кнопку повтора — если родитель передал обработчик.
defineProps({
  /** Вид сообщения: 'info' или 'error'. */
  kind: { type: String, default: 'info' },
  /** Текст сообщения. */
  text: { type: String, required: true },
  /** Показывать ли кнопку «Повторить». */
  retryable: { type: Boolean, default: false }
})

defineEmits(['retry'])
</script>

<template>
  <p class="notice" :class="{ error: kind === 'error' }">
    {{ text }}
    <button v-if="retryable" class="notice-retry" type="button" @click="$emit('retry')">
      Повторить
    </button>
  </p>
</template>
