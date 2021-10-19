<template>
  <q-layout view="lHh Lpr lFf">
    <q-header elevated>
    </q-header>
    <q-page-container>
      <q-page>
        <q-card>
          <q-card-section>
            Login through Apple
          </q-card-section>
          <q-card-section>
            Apple просит $99 в год за интеграцию. Данное приложение бесплатное
            и пока не принесло никому никакого профита. Так что пока не вижу возможности
            делать вход через Apple.
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
  name: 'LoginApple',
  setup() {
    return {
      close: function () {window.close();},
    }
  }
};
</script>

<style scoped>

</style>
