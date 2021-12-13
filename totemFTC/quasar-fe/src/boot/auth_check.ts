import {boot} from 'quasar/wrappers'
import {useStoreLogin} from 'src/store/store_login';
import axios from 'axios';

// "async" is optional;
// more info on params: https://v2.quasar.dev/quasar-cli/boot-files
export default boot(async ( { router } ) => {
  console.log("Init auth. Current route", router.currentRoute);

  const storeLogin = useStoreLogin();
  try {
    await storeLogin.authenticate();
  } catch (e) {
    console.log('Error during authenticate', e);
    if (axios.isAxiosError(e) && e.response?.status === 403) {
      // This is ok - server reports there is no appropriate session - e.g. backend was cleaned up
      storeLogin.clearSession();
    } else {
      storeLogin.sessionId = '';
    }
  }

  const currentPath = router.currentRoute.value.path;
  if (!storeLogin.isAuthenticated) {
    console.log('Redirect to login');
    try {
      if (currentPath === '/index.html' || currentPath === '/quasar.html') {
        await router.replace({name: 'login'});
      } else {
        await router.push({name: 'login', params: {inProgress: 'true'}});
      }
    } catch (e) {
      console.log('Error push to login page', e);
    }
  } else if (currentPath === '/quasar.html') {
    await router.replace({name: '/'});
  }

  router.beforeEach((to) => {
    console.log('Before route', to);
    const requiresAuth = to.matched.some(record => record.meta.requiresAuth)

    if (requiresAuth && !storeLogin.isAuthenticated) {
      return '/login'
    }
  })

})
