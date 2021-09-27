import { boot } from 'quasar/wrappers';
import axios, { AxiosInstance } from 'axios';
import {useStoreLogin} from 'src/store/store_login';

declare module '@vue/runtime-core' {
  interface ComponentCustomProperties {
    $axios: AxiosInstance;
  }
}

// Be careful when using SSR for cross-request state pollution
// due to creating a Singleton instance here;
// If any client changes this (global) instance, it might be a
// good idea to move this instance creation inside of the
// "export default () => {}" function below (which runs individually
// for each client)
const api = axios.create({ baseURL: 'http://localhost:8080' });


export default boot(({ app }) => {
  // for use inside Vue files (Options API) through this.$axios and this.$api

  app.config.globalProperties.$axios = axios;
  // ^ ^ ^ this will allow you to use this.$axios (for Vue Options API form)
  //       so you won't necessarily have to import axios in each vue file

  app.config.globalProperties.$api = api;
  // ^ ^ ^ this will allow you to use this.$api (for Vue Options API form)
  //       so you can easily perform requests against your app's API

  const storeLogin = useStoreLogin();

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
    // , function (error) {
    //   // Do something with request error
    //   return Promise.reject(error);
    // }
  );
  // api.defaults.headers.common['Authorization'] = `Bearer ${sessionId}`;

});

export { api };
