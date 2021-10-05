import {defineStore} from 'pinia'
import {api} from 'boot/axios';

const SESSION_ID_STORAGE_KEY = 'session_id';

export interface EntityUserEmail {
  email: string,
  confirmed: boolean,
}

export interface EntityUserPhone {
  phone: string,
  confirmed: boolean,
}

export interface EntityUserImage {
  id: number,
  contentType: string|null,
}

export interface EntityUser {
  userId: number,
  firstName: string,
  lastName: string,
  nickName: string,
  primaryImage?: EntityUserImage,
  phones: Array<EntityUserPhone>,
  emails: Array<EntityUserEmail>,
  images: Array<EntityUserImage>,
}

const emptyUser: EntityUser = {
  userId: 0,
  firstName: '',
  lastName: '',
  nickName: '',
  phones: [],
  emails: [],
  images: [],
};

export const useStoreLogin = defineStore('login', {
  state: () => ({
    sessionId: '',
    user: {...emptyUser} as EntityUser,
    userFull: {...emptyUser} as EntityUser,
  }),

  getters: {
    isAuthenticated(state) {
      const b = state.sessionId.length > 0;
      console.log(`is authenticated: ${String(b)}`)
      return b;
    },

    fullName(state) {
      return `${state.user.firstName} ${state.user.lastName}`;
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

      this.user = (await api.get<EntityUser>('/login/user')).data;
      console.log('User received', this.user);
    },

    clearSession() {
      localStorage.removeItem(SESSION_ID_STORAGE_KEY);
    },

    async loadUserFull () {
      this.userFull = (await api.get<EntityUser>('/login/userFull')).data;
    }
  },
});
