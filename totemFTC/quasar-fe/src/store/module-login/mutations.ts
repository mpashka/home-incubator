import { MutationTree } from 'vuex';
import { StateInterfaceLogin } from './state';
import { EntityUser } from './model';


const mutation: MutationTree<StateInterfaceLogin> = {
  sessionId (state: StateInterfaceLogin, sessionId: string) {
    state.sessionId = sessionId;
  },

  setUser (state: StateInterfaceLogin, user: EntityUser) {
    state.user = user;
    state.userId = user.userId;
  },

  /**
   * Used for user profile editor
   * @param state
   * @param user
   */
  setUserFull (state: StateInterfaceLogin, user: EntityUser) {
    state.userFull = user;
  }
};

export default mutation;
