import {boot} from 'quasar/wrappers'
import {useStoreLogin} from 'src/store/store_login';
import axios from 'axios';

// "async" is optional;
// more info on params: https://v2.quasar.dev/quasar-cli/boot-files
export default boot(async ( { router } ) => {
  const storeLogin = useStoreLogin();
  try {
    await storeLogin.authenticate();
  } catch (e) {
    console.log('Error during authenticate', e);
    if (axios.isAxiosError(e) && e.response?.status === 403) {
      // This is ok - server reports there is no appropriate session - e.g. it was restarted
      storeLogin.clearSession();
      router.push({ name: 'login', params: { inProgress: 'true' } })
        .catch(e => console.log('Error push to login page', e));
    }
  }

/*
  if (!storeLogin.isAuthenticated) {
    redirect('/login');
  }
*/

  router.beforeEach((to) => {
    const requiresAuth = to.matched.some(record => record.meta.requiresAuth)

    if (requiresAuth && !storeLogin.isAuthenticated) {
      return '/login'
    }
  })

})
