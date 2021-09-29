<template>
  <q-layout view="lHh Lpr lFf">
    <q-header elevated>
      <q-toolbar>
        <q-btn flat dense round icon="menu" aria-label="Menu" @click="toggleLeftDrawer"/>
        <q-toolbar-title>
          Totem FTC
        </q-toolbar-title>

        <div v-if="storeLogin.isAuthenticated">{{ storeLogin.fullName }}</div>
      </q-toolbar>
    </q-header>

    <q-drawer v-model="leftDrawerOpen" show-if-above bordered>
      <q-list>
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

        <router-link to="/tableTrainers">
          <q-item clickable>
            <q-item-section avatar>
              <q-icon name="tools" />
            </q-item-section>

            <q-item-section>
              <q-item-label>Тренеры</q-item-label>
              <q-item-label caption>

              </q-item-label>
            </q-item-section>
          </q-item>
        </router-link>

        <router-link to="/settings">
          <q-item clickable>
            <q-item-section avatar>
              <q-icon name="tools" />
            </q-item-section>

            <q-item-section>
              <q-item-label>Настройки</q-item-label>
              <q-item-label caption>
                Настройки
              </q-item-label>
            </q-item-section>
          </q-item>
        </router-link>
      </q-list>
    </q-drawer>

    <q-page-container>
      <router-view />
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
