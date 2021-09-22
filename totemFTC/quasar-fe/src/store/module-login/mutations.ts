import { MutationTree } from 'vuex';
import { StateInterfaceLogin } from './state';
import { UserLogin } from './model';


const mutation: MutationTree<StateInterfaceLogin> = {
  sessionId (state: StateInterfaceLogin, sessionId: string) {
    state.sessionId = sessionId;
  },

  authorize (state: StateInterfaceLogin, user: UserLogin) {
    state.userName = user.userName;
    state.authenticated = true;
  }
};

export default mutation;
