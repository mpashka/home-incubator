import {EntityUser} from 'src/store/module-login/model';

export interface StateInterfaceLogin {
  sessionId: string;
  userId: number;
  user: EntityUser | null;
  userFull: EntityUser | null;
}

function state(): StateInterfaceLogin {
  return {
    sessionId: '',
    userId: -1,
    user: null,
    userFull: null,
  }
}

export default state;
