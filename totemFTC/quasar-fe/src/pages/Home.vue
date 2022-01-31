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

        <p>Пожелания и найденные ошибки направляйте:</p>
        <ul>
          <li><a href="https://github.com/mpashka/home-incubator/tree/totemFTC_pinia/totemFTC" target="_blank">Исходный код</a></li>
          <li><a href="https://github.com/mpashka/home-incubator/issues" target="_blank">Ошибки и предложения</a></li>
          <li><a href="https://trello.com/b/dQ9YlBoq/todo" target="_blank">Планы</a></li>
          <li>Чатик в тележеньке с обсуждением - TBD</li>
        </ul>


        <p>Исполнено с использованием</p>
        <ul>
          <li><a href="https://vuejs.org/">Vuejs</a> web framework</li>
          <li><a href="https://quasar.dev/">Quasar</a> набор ui компоненты для Vue / vue based framework</li>
          <li><a href="https://www.typescriptlang.org/">TypeScript</a> JavaScript с поддержкой типизации</li>
          <li><a href="https://quarkus.io/">Quarkus</a> Java framework</li>
          <li><a href="https://www.docker.com/">Docker</a> контейнеры</li>
          <li><a href="https://www.postgresql.org/">PostgreSQL</a> база данных</li>
          <li><a href="https://flutter.dev/">Flutter</a> native app framework (iOS Android)</li>
          <li><a href="https://dart.dev/">Dart</a> язык для Flutter</li>
        </ul>

      </q-card-section>

      <q-card-section>
        <p>TODO List - Список доработок</p>
        <q-list>
          <q-item>
            <q-item-section>
              <q-item-label>
                <q-btn round icon="add" size="sm"/>
                Добавление
              </q-item-label>
              <q-item-label caption>
                При добавлении надо сохранять данные диалога. То есть если нажимаешь
                добавить, вводишь какие-то данные, потом жмешь Esc или Cancel, потом
                снова добавить - введенные данные не должны пропасть.
              </q-item-label>
            </q-item-section>
          </q-item>

          <q-item>
            <q-item-section>
              <q-item-label>
                Рефакторинг удаления/добавления
              </q-item-label>
              <q-item-label caption>
                Стоит проверить возможность переиспользования функционала по добавлению/удалению.
              </q-item-label>
            </q-item-section>
          </q-item>

        </q-list>
      </q-card-section>
    </q-card>
  </q-page>
</template>

<script lang="ts">
import { defineComponent } from 'vue';

export default defineComponent({
  name: 'PageHome',
  setup() {
    return {};
  },
});
</script>
