import {defineStore} from 'pinia'
import {api} from 'boot/axios';
import axios from 'axios';

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
    userId: -1,
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
      const SESSION_ID_STORAGE_KEY = 'session_id';
      if (sessionId === undefined) {
        const newSessionId = localStorage.getItem(SESSION_ID_STORAGE_KEY);
        if (newSessionId == null) {
          return;
        }
        sessionId = newSessionId;
      } else {
        localStorage.setItem(SESSION_ID_STORAGE_KEY, sessionId);
        this.sessionId = sessionId;
      }

      try {
        this.user = (await api.get('/login/user')).data as EntityUser;
      } catch (e) {
        // todo handle error here - show table, e.t.c.
        if (axios.isAxiosError(e)) {
          if (e.response?.status === 403) {
            // This is ok - server reports there is no appropriate session - e.g. it was restarted
            localStorage.removeItem(SESSION_ID_STORAGE_KEY)
          }
        } else {
          console.log('Unexpected Http Error', e);
          throw e;
        }
      }
    },

    async loadUserFull () {
      const user = (
        await api.get('/login/userFull')
      ).data as EntityUser;
      this.userFull = user;
    }
  },
});
