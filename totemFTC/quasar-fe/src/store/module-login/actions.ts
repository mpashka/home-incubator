import { ActionTree } from 'vuex';
import { StateInterface } from '../index';
import { StateInterfaceLogin } from './state';
import { api } from 'boot/axios';
import { EntityUser } from './model';

const actions: ActionTree<StateInterfaceLogin, StateInterface> = {
  async authenticate (context, sessionId: string | null = null) {
    if (sessionId === null) {
      const newSessionId = localStorage.getItem('session_id');
      if (newSessionId == null) {
        return;
      }
      sessionId = newSessionId;
    } else {
      localStorage.setItem('session_id', sessionId);
    }

    const user = (
      await api.get('/login/user')
    ).data as EntityUser;
    context.commit('setUser', user);
  },

  async loadUserFull (context) {
    const user = (
      await api.get('/login/userFull')
    ).data as EntityUser;
    context.commit('setUserFull', user);
  }
};

export default actions;
