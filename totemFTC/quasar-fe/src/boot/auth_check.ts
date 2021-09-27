import {boot} from 'quasar/wrappers'
import {useStoreLogin} from 'src/store/store_login';

// "async" is optional;
// more info on params: https://v2.quasar.dev/quasar-cli/boot-files
export default boot(async ( { router } ) => {
  const storeLogin = useStoreLogin();
  await storeLogin.authenticate();
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
