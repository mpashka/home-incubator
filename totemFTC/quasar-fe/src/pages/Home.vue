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

          <q-item>
            <q-item-section>
              <q-item-label>
                Переименовать переменные в store.select
              </q-item-label>
              <q-item-label caption>
                Надо чтобы название переменной в store отражало ее временное значение и отличалось
                от обычных, постоянных значений. Например storeVisit.training ⇨ selectedTraining.
              </q-item-label>
            </q-item-section>
          </q-item>

          <q-item>
            <q-item-section>
              <q-item-label>
                Стандартизировать Edit objects
              </q-item-label>
              <q-item-label caption>
                Для сущностей у которых id генерируется можно различить add/edit по id.
                Для других сущностей необходим Edit объект с дополнительными свойствами.
                Стоит подумать над стандартизацией таких вещей - может стоит внести
                эти свойства в основной объект и всегда удалять их перед отправкой на сервер.
                Например можно все локальные свойства именовать как local или localProperty и использовать
                json.stringify с функцией фильтрации по ключу. И добавить для этого свой axios.interceptor.
                Таким образом можно сохранять промежуточный state и не использовать null для объектов во
                время редактирования.
                Check Reflect.defineMetadata - https://stackoverflow.com/questions/51170130/check-if-property-is-decorated-with-specific-annotation-typescript
              </q-item-label>
            </q-item-section>
          </q-item>

          <q-item>
            <q-item-section>
              <q-item-label>
                Переименовать функции
              </q-item-label>
              <q-item-label caption>
                Добавить постфикс Fn для функций, чтоб было понятно - например userStore.isAdmin ⇨ isAdminFn
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
