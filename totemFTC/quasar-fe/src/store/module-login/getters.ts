import { GetterTree } from 'vuex';
import { StateInterface } from '../index';
import { StateInterfaceLogin } from './state';

const getters: GetterTree<StateInterfaceLogin, StateInterface> = {
  authenticated(context) {
    return context.authenticated
  }
};

export default getters;
