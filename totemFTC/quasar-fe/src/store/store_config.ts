import {defineStore} from 'pinia';
import {api} from 'boot/axios';
import {clientId} from 'src/store/store_utils';

export interface EntityClientConfig {
  serverId: string,
  serverRunProfile: string,
  serverBuild: string,
  oidcClientIds: Map<string, string>,
}


export const useStoreClientConfig = defineStore('clientConfig', {
  state: () => ({
    clientConfigLoaded: false,
    clientConfig: {
      serverId: '',
      serverRunProfile: '',
      serverBuild: '',
      oidcClientIds: {}
    } as EntityClientConfig
  }),

  getters: {
    serverInfo(state) {
      return `${state.clientConfig.serverId} ${state.clientConfig.serverRunProfile} ${state.clientConfig.serverBuild}`;
    }
  },

  actions: {
    init() {
      this.loadClientConfig()
        .catch(e => console.log('Error loading client configuration. Backend was not started', e));
    },

    async loadClientConfig() {
      if (this.clientConfigLoaded) return;
      this.clientConfig = (await api.get<EntityClientConfig>(`/api/utils/clientConfig?clientId=${clientId}`)).data;
      this.clientConfig.oidcClientIds = new Map<string, string>(Object.entries(this.clientConfig.oidcClientIds));
      this.clientConfigLoaded = true;
    },
  },
});
