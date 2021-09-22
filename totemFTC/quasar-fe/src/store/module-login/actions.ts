import { ActionTree } from 'vuex';
import { StateInterface } from '../index';
import { StateInterfaceLogin } from './state';

const actions: ActionTree<StateInterfaceLogin, StateInterface> = {
  someAction (/* context */) {
    // your code
  }
};

export default actions;
