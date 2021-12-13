import {defineStore} from 'pinia';
import {api} from 'boot/axios';
import {useStoreCrudUser} from 'src/store/store_crud_user';
import Router from 'src/router';
import {LoginProvider} from 'pages/login/login';

const SESSION_ID_STORAGE_KEY = 'session_id';

export type LoginUserType = 'newUser' | 'existing';

export interface LoginResult {
  sessionId: string;
  userType: LoginUserType;
}

const client = `quasar-${String(process.env.MODE)}-${String(process.env.NODE_ENV)}`;

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

    async login(loginProvider: LoginProvider, callbackParameters: string): Promise<LoginResult> {
      const loginResult = (await api.get(`/api/login/loginCallback/${loginProvider.name}?client=${client}&${callbackParameters}`)).data as LoginResult;
      await this.authenticate(loginResult.sessionId);
      return loginResult;
    },

    async link(loginProvider: LoginProvider, callbackParameters: string): Promise<LoginResult> {
      return (await api.get(`/api/login/linkCallback/${loginProvider.name}?client=${client}&${callbackParameters}`)).data as LoginResult;
    },

    clearSession() {
      localStorage.removeItem(SESSION_ID_STORAGE_KEY);
      this.sessionId = '';
      const storeUser = useStoreCrudUser();
      storeUser.clear();
    },

    async logout() {
      await api.get('/api/login/logout');
      this.clearSession();
      await Router.replace('/login');
    }
  },
});
