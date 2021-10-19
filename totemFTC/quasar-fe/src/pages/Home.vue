<template>
  <q-page>
    <q-card>
      <q-card-section>
        <div class="text-h6">Фитнес-студия
          функционального тренинга
          и кроссфита
          в старых Химках</div>
      </q-card-section>
      <q-card-section>
        <p>Данное веб приложение разрабатывается для учета посещений и абонементов
          <a href="https://totemftc.ru/" target="_blank">
            фитнес-студии функционального тренинга "Тотем"
          </a>.
        </p>

        <p>Исходный код на
          <a href="https://github.com/mpashka/home-incubator/tree/totemFTC_pinia/totemFTC" target="_blank">GitHub</a>
        </p>

        <p>Исполнено с использованием</p>
        <ul>
          <li><a href="https://vuejs.org/">Vuejs</a> web framework</li>
          <li><a href="https://quasar.dev/">Quasar</a> набор ui компоненты для Vue / vue based framework</li>
          <li><a href="https://www.typescriptlang.org/">TypeScript</a> JavaScript с поддержкой типизации</li>
          <li><a href="https://quarkus.io/">Quarkus</a> Java framework</li>
          <li><a href="https://www.docker.com/">Docker</a> контейнеры</li>
          <li><a href="https://www.docker.com/">Docker</a> контейнеры</li>
          <li><a href="https://www.postgresql.org/">PostgreSQL</a> база данных</li>
          <li><a href="https://flutter.dev/">Flutter</a> native app framework (iOS Android)</li>
        </ul>

        <p>Разработчики</p>
        <ul>
          <li><a href="https://telegram.me/M_pashka" target="_blank">Паша</a></li>
        </ul>

      </q-card-section>
    </q-card>
  </q-page>
</template>

<script lang="ts">
import { defineComponent } from 'vue';

export default defineComponent({
  name: 'PageIndex',
  setup() {
    return {};
  },
});
</script>
