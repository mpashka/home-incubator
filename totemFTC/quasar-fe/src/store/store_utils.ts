import {defineStore} from "pinia";

export const useStoreUtils = defineStore('storeUtils', {
  state: () => ({
    loading: false,
  }),
});
