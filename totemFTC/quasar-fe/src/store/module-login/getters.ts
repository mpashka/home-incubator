import { GetterTree } from 'vuex';
import { StateInterface } from '../index';
import { StateInterfaceLogin } from './state';

const getters: GetterTree<StateInterfaceLogin, StateInterface> = {
  isAuthenticated(context) {
    return context.sessionId.length > 0 && context.userId >= 0;
  },

  fullName(context) {
    return context.user != null ? `${context.user.firstName} ${context.user.lastName}` : '';
  }
};

export default getters;
