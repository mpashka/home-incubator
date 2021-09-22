export interface StateInterfaceLogin {
  authenticated: boolean;
  sessionId: string;
  userName: string;
}

function state(): StateInterfaceLogin {
  return {
    authenticated: false,
    sessionId: '',
    userName: ''
  }
}

export default state;
