import {defineStore} from 'pinia';
import {api} from 'boot/axios';
import {useStoreCrudUser} from 'src/store/store_crud_user';
import Router from 'src/router';

const SESSION_ID_STORAGE_KEY = 'session_id';

export const useStoreLogin = defineStore('login', {
  state: () => ({
    sessionId: '',
  }),

  getters: {
    isAuthenticated(state) {
      const b = state.sessionId.length > 0;
      console.log(`is authenticated: ${String(b)}`)
      return b;
    },

    fullName() {
      const storeUser = useStoreCrudUser();
      return `${storeUser.user.firstName} ${storeUser.user.lastName}`;
    }
  },

  actions: {
    async authenticate (sessionId?: string) {
      if (sessionId === undefined) {
        console.log('Authenticate from local storage')
        const newSessionId = localStorage.getItem(SESSION_ID_STORAGE_KEY);
        if (!newSessionId) {
          console.log('    Not found')
          return;
        }
        sessionId = newSessionId;
      } else {
        console.log(`Authenticate new ${sessionId}`)
        localStorage.setItem(SESSION_ID_STORAGE_KEY, sessionId);
      }

      this.sessionId = sessionId;
      const storeUser = useStoreCrudUser();
      await storeUser.loadUser();
    },

    clearSession() {
      localStorage.removeItem(SESSION_ID_STORAGE_KEY);
      this.sessionId = '';
      const storeUser = useStoreCrudUser();
      storeUser.clear();
    },


    async logout() {
      await api.get('/api/logout/');
      this.clearSession();
      await Router.replace('/login');
    }
  },
});
