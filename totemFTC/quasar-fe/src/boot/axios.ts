import {boot} from 'quasar/wrappers';
import axios, { AxiosInstance } from 'axios';
import {useStoreLogin} from 'src/store/store_login';
import {useStoreUtils} from "src/store/store_utils";

declare module '@vue/runtime-core' {
  interface ComponentCustomProperties {
    $axios: AxiosInstance;
    $api: AxiosInstance;
  }
}

// Be careful when using SSR for cross-request state pollution
// due to creating a Singleton instance here;
// If any client changes this (global) instance, it might be a
// good idea to move this instance creation inside of the
// "export default () => {}" function below (which runs individually
// for each client)
const api = axios.create({ baseURL: 'http://localhost:8080' });

export default boot(({ app, router }) => {
  // for use inside Vue files (Options API) through this.$axios and this.$api

  app.config.globalProperties.$axios = axios;
  // ^ ^ ^ this will allow you to use this.$axios (for Vue Options API form)
  //       so you won't necessarily have to import axios in each vue file

  app.config.globalProperties.$api = api;
  // ^ ^ ^ this will allow you to use this.$api (for Vue Options API form)
  //       so you can easily perform requests against your app's API

  const storeLogin = useStoreLogin();
  const storeUtils = useStoreUtils();

  // Add a request interceptor
  // https://stackoverflow.com/questions/43051291/attach-authorization-header-for-all-axios-requests
  api.interceptors.request.use(function (config) {
      const authenticated = storeLogin.isAuthenticated;
      console.log(`Check if request is authenticated ${String(authenticated)}. Session: ${storeLogin.sessionId}`)
      if (authenticated) {
        (config.headers as {[key: string]: string})['Authorization'] = `Bearer ${storeLogin.sessionId}`
      }
      return config;
    }
  );
  // api.defaults.headers.common['Authorization'] = `Bearer ${sessionId}`;

  api.interceptors.response.use(response => {return response},
    error => {
      // todo handle error here - show message, add to table, e.t.c.
      if (axios.isAxiosError(error) && error.response?.status === 403) {
        // This is ok - server reports there is no appropriate session - e.g. it was restarted
        storeLogin.clearSession();
        router.push({ name: 'login', params: { inProgress: 'true' } })
          .catch(e => console.log('Error push to login page', e));
      }
      return Promise.reject(error);
    });

  // todo [3] probably use config.url or even request to track parallel requests
  api.interceptors.request.use(config => {storeUtils.loading = true; return config;});
  api.interceptors.response.use(response => {storeUtils.loading = false; return response;}, error => {storeUtils.loading = false; return Promise.reject(error);});
});

export { api };
