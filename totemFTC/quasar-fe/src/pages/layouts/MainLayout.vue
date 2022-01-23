<template>
  <q-layout view="lHh Lpr lFf">
    <q-header elevated>
      <q-toolbar>
        <q-btn flat dense round icon="menu" aria-label="Menu" @click="toggleLeftDrawer"/>
        <q-toolbar-title>
          Totem FTC
        </q-toolbar-title>

        <div v-if="storeLogin.isAuthenticated">
          {{ storeLogin.fullName }}
          <q-icon name="more_vert" size="sm">
            <q-menu auto-close>
              <q-list>
                <q-item clickable to="/settings">
                  <q-item-section>Настройки</q-item-section>
                </q-item>
                <q-separator />
                <q-item clickable @click="storeLogin.logout()">
                  <q-item-section>Выход</q-item-section>
                </q-item>
              </q-list>
            </q-menu>
          </q-icon>
        </div>
      </q-toolbar>
    </q-header>

    <q-drawer v-model="leftDrawerOpen" show-if-above bordered>
      <q-list class="my-menu">
        <q-item-label header>
          Essential Links
        </q-item-label>

        <router-link to="/">
          <q-item clickable>
            <q-item-section avatar>
              <q-icon name="home" />
            </q-item-section>

            <q-item-section>
              <q-item-label>Home</q-item-label>
              <q-item-label caption>
                home
              </q-item-label>
            </q-item-section>
          </q-item>
        </router-link>

        <router-link to="/schedule">
          <q-item clickable>
            <q-item-section avatar>
              <q-icon name="clock" />
            </q-item-section>

            <q-item-section>
              <q-item-label>Расписание</q-item-label>
              <q-item-label caption>

              </q-item-label>
            </q-item-section>
          </q-item>
        </router-link>

        <router-link to="/trainings">
          <q-item clickable>
            <q-item-section avatar>
              <q-icon name="mdi-gym" />
            </q-item-section>

            <q-item-section>
              <q-item-label>Тренировки</q-item-label>
              <q-item-label caption>

              </q-item-label>
            </q-item-section>
          </q-item>
        </router-link>

        <router-link to="/tableUsers">
          <q-item clickable>
            <q-item-section avatar>
              <q-icon name="mdi-users" />
            </q-item-section>

            <q-item-section>
              <q-item-label>Пользователи</q-item-label>
              <q-item-label caption>

              </q-item-label>
            </q-item-section>
          </q-item>
        </router-link>

        <router-link to="/tableVisits">
          <q-item clickable>
            <q-item-section avatar>
              <q-icon name="tools" />
            </q-item-section>

            <q-item-section>
              <q-item-label>Посещения</q-item-label>
              <q-item-label caption>

              </q-item-label>
            </q-item-section>
          </q-item>
        </router-link>

      </q-list>
    </q-drawer>

    <q-page-container>
      <Suspense timeout="100">
        <template #default>
          <router-view />
        </template>
        <!--template #fallback>
          <q-spinner color="primary" size="3em"/>
          <div>Loading...</div>
        </template-->
      </Suspense>
    </q-page-container>
  </q-layout>
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue'
import {useStoreLogin} from 'src/store/store_login';


export default defineComponent({
  name: 'MainLayout',

  setup () {
    const leftDrawerOpen = ref(false)
    const storeLogin = useStoreLogin();

    return {
      storeLogin,
      leftDrawerOpen,
      toggleLeftDrawer () {
        leftDrawerOpen.value = !leftDrawerOpen.value
      }
    }
  }
})

</script>
