<template>
  <q-layout view="lHh Lpr lFf">
    <q-header elevated>
    </q-header>
    <q-page-container>
      <q-page>
        <q-card>
          <q-card-section>
            Login through Twitter
          </q-card-section>
          <q-card-section>
            Интеграция с Twitter еще не готова. Twitter
            отклонил мой запрос, но я обязательно напишу им еще раз.
          </q-card-section>
          <q-separator/>
          <q-card-section class="q-gutter-x-md">
            <q-btn @click="close()" label="Ясно"/>
          </q-card-section>
        </q-card>
      </q-page>
    </q-page-container>
  </q-layout>
</template>

<script>
export default {
  name: 'LoginTwitter',
  setup() {
    return {
      close: function () {window.close();},
    }
  }
};
</script>

<style scoped>

</style>
