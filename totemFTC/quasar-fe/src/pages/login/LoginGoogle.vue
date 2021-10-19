<template>
  <q-layout view="lHh Lpr lFf">
    <q-header elevated>
    </q-header>
    <q-page-container>
      <q-page>
        <q-card>
          <q-card-section>
            Login through Google
          </q-card-section>
          <q-card-section>
            Интеграция с Google находится в тестовом режиме.
            Для того, чтобы заработал вход через Google необходимо сначала
            написать мне в <a href="https://telegram.me/M_pashka" target="_blank">телегу</a>. Я вас добавлю
            в тестовые пользователи и после этого можно будет пользоваться.
          </q-card-section>
          <q-separator/>
          <q-card-section class="q-gutter-x-md">
            <q-btn type="a" :href="proceedUrl" label="Меня уже добавили"/>
            <q-btn type="a" href="https://telegram.me/M_pashka" label="Попросить добавить"/>
            <q-btn @click="close()" label="Ни нада"/>
          </q-card-section>
        </q-card>
      </q-page>
    </q-page-container>
  </q-layout>
</template>

<script>
export default {
  name: 'LoginGoogle',
  setup() {
    return {
      close: function () {window.close();},
      proceedUrl: String(process.env.BackendFullUrl) + '/api/login/init/google' + window.location.search,
    }
  }
};
</script>

<style scoped>

</style>
