import {defineStore} from 'pinia';
import {api} from 'boot/axios';

export interface EntityClientConfig {
  oidcClientIds: Map<string, string>,
}


export const useStoreClientConfig = defineStore('clientConfig', {
  state: () => ({
    clientConfigLoaded: false,
    clientConfig: {
      oidcClientIds: {}
    } as EntityClientConfig
  }),

  actions: {
    init() {
      this.loadClientConfig()
        .catch(e => console.log('Error loading client configuration. Backend was not started', e));
    },

    async loadClientConfig() {
      if (this.clientConfigLoaded) return;
      this.clientConfig = (await api.get<EntityClientConfig>('/api/utils/clientConfig')).data;
      this.clientConfig.oidcClientIds = new Map<string, string>(Object.entries(this.clientConfig.oidcClientIds));
      this.clientConfigLoaded = true;
    },
  },
});
