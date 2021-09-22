import { Module } from 'vuex';
import { StateInterface } from '../index';
import state, { StateInterfaceLogin } from './state';
import actions from './actions';
import getters from './getters';
import mutations from './mutations';

const moduleLogin: Module<StateInterfaceLogin, StateInterface> = {
  namespaced: true,
  actions,
  getters,
  mutations,
  state
};

export default moduleLogin;
