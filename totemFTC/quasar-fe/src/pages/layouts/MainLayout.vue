<template>
  <q-layout view="lhh lpr lff">
    <q-header elevated class="bg-primary text-white">
      <q-toolbar>
        <q-btn flat dense round icon="menu" aria-label="Menu" @click="toggleLeftDrawer"/>
        <q-toolbar-title>
          Totem FTC
        </q-toolbar-title>

        <div v-if="storeLogin.isAuthenticated">
          <q-icon name="fas fa-question" size="sm" v-if="storeUser.isGuest(storeUser.user)" />
          <q-icon name="person" size="sm" v-if="storeUser.isUser(storeUser.user)" />
          <q-icon name="sports" size="sm" v-if="storeUser.isTrainer(storeUser.user)" />
          <q-icon name="manage_accounts" size="sm" v-if="storeUser.isAdmin(storeUser.user)" />

          {{ storeUser.fullName }}

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

    <q-drawer v-model="leftDrawerOpen" show-if-above bordered side="left">
      <q-scroll-area class="fit">
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
              <q-icon name="schedule" />
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
              <q-icon name="mdi-dumbbell" />
            </q-item-section>

            <q-item-section>
              <q-item-label>Тренировки</q-item-label>
              <q-item-label caption>

              </q-item-label>
            </q-item-section>
          </q-item>
        </router-link>

        <router-link to="/users" v-if="storeUser.isAdmin(storeUser.user)">
          <q-item clickable>
            <q-item-section avatar>
              <q-icon name="mdi-account-multiple" />
            </q-item-section>

            <q-item-section>
              <q-item-label>Пользователи</q-item-label>
              <q-item-label caption>

              </q-item-label>
            </q-item-section>
          </q-item>
        </router-link>

        <router-link to="/visits" v-if="storeUser.isAdmin(storeUser.user)">
          <q-item clickable>
            <q-item-section avatar>
              <q-icon name="mdi-playlist-check" />
            </q-item-section>

            <q-item-section>
              <q-item-label>Посещения</q-item-label>
              <q-item-label caption>

              </q-item-label>
            </q-item-section>
          </q-item>
        </router-link>

        <router-link to="/finance" v-if="storeUser.isAdmin(storeUser.user)">
          <q-item clickable>
            <q-item-section avatar>
              <q-icon name="mdi-cash-multiple" />
            </q-item-section>

            <q-item-section>
              <q-item-label>Финансы</q-item-label>
              <q-item-label caption>

              </q-item-label>
            </q-item-section>
          </q-item>
        </router-link>

      </q-list>
      </q-scroll-area>
    </q-drawer>

    <q-page-container>
      <!--Suspense timeout="100">
        <template #default>
          <router-view />
        </template>
        <template #fallback>
          <q-spinner color="primary" size="3em"/>
          <div>Loading...</div>
        </template>
      </Suspense-->
      <router-view />
    </q-page-container>
  </q-layout>
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue'
import {useStoreLogin} from 'src/store/store_login';
import {useStoreCrudUser} from 'src/store/store_crud_user';


export default defineComponent({
  name: 'MainLayout',

  setup () {
    const leftDrawerOpen = ref(false)
    const storeLogin = useStoreLogin();
    const storeUser = useStoreCrudUser();

    return {
      storeLogin,
      storeUser,
      leftDrawerOpen,
      toggleLeftDrawer () {
        leftDrawerOpen.value = !leftDrawerOpen.value
      }
    }
  }
})

</script>
